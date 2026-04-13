package com.countrydelight.mms.service.outward;

import com.countrydelight.mms.dto.outward.IssueCreateRequest;
import com.countrydelight.mms.dto.outward.IssueDetailRequest;
import com.countrydelight.mms.entity.outward.IssueDetail;
import com.countrydelight.mms.entity.outward.IssueHeader;
import com.countrydelight.mms.exception.InsufficientStockException;
import com.countrydelight.mms.exception.MmsException;
import com.countrydelight.mms.repository.outward.IssueDetailRepository;
import com.countrydelight.mms.repository.outward.IssueHeaderRepository;
import com.countrydelight.mms.service.audit.VoucherEditLogService;
import com.countrydelight.mms.service.master.VoucherNumberService;
import com.countrydelight.mms.service.stock.FifoService;
import com.countrydelight.mms.service.stock.StockLedgerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Issue Service - Handles material issues (outward).
 *
 * FIFO Logic:
 * 1. When issuing stock, consume from oldest GRN first
 * 2. Rate is derived from FIFO weighted average (never user-entered)
 * 3. Stock ledger is updated with ISSUE transaction
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IssueService {

    private final IssueHeaderRepository issueHeaderRepository;
    private final IssueDetailRepository issueDetailRepository;
    private final FifoService fifoService;
    private final StockLedgerService stockLedgerService;
    private final VoucherEditLogService editLogService;
    private final VoucherNumberService voucherNumberService;

    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_PENDING_APPROVAL = "PENDING_APPROVAL";
    public static final String STATUS_POSTED = "POSTED";

    /**
     * Create a new issue in DRAFT status.
     */
    @Transactional
    public IssueHeader createIssue(IssueCreateRequest request) {
        // Validate stock availability for all items first
        for (IssueDetailRequest detailReq : request.getDetails()) {
            BigDecimal available = fifoService.getAvailableStock(
                    request.getBranchId(), detailReq.getItemId(), detailReq.getLocationId());

            if (available.compareTo(detailReq.getQtyIssued()) < 0) {
                throw new InsufficientStockException(
                        detailReq.getItemId(), detailReq.getLocationId(),
                        detailReq.getQtyIssued(), available);
            }
        }

        // Validate JOB_WORK requires suppId
        String issueType = request.getIssueType() != null ? request.getIssueType() : "REGULAR";
        if ("JOB_WORK".equals(issueType) && (request.getSuppId() == null || request.getSuppId().isBlank())) {
            throw new MmsException("Supplier ID is required for JOB_WORK issue type");
        }

        // Create Issue header
        IssueHeader issue = IssueHeader.builder()
                .branchId(request.getBranchId())
                .deptId(request.getDeptId())
                .issueDate(request.getIssueDate())
                .issuedTo(request.getIssuedTo())
                .status(STATUS_DRAFT)
                .issueType(issueType)
                .suppId(request.getSuppId())
                .expectedReturnDate(request.getExpectedReturnDate())
                .remarks(request.getRemarks())
                .createdBy(request.getCreatedBy())
                .voucherNumber(request.getVoucherNumber())
                .voucherTypeId(request.getVoucherTypeId())
                .build();

        try {
            issue = issueHeaderRepository.save(issue);
            Long issueId = issue.getIssueId();

            // Create Issue details (rate will be calculated during posting)
            for (IssueDetailRequest detailReq : request.getDetails()) {
                IssueDetail detail = IssueDetail.builder()
                        .issueId(issueId)
                        .itemId(detailReq.getItemId())
                        .locationId(detailReq.getLocationId())
                        .qtyIssued(detailReq.getQtyIssued())
                        .rate(BigDecimal.ZERO) // Will be calculated from FIFO during posting
                        .build();

                issueDetailRepository.save(detail);
            }

            IssueHeader saved = issueHeaderRepository.findById(issueId).orElse(issue);
            editLogService.logCreate("ISSUE", issueId, saved.getVoucherNumber(), saved.getVoucherTypeId(),
                    saved.getCreatedBy(), "Issue created for branch=" + saved.getBranchId());
            log.info("Issue created: ID={}, Branch={}, IssuedTo={}",
                    issueId, request.getBranchId(), request.getIssuedTo());

            return saved;
        } catch (RuntimeException e) {
            voucherNumberService.returnVoucherNumber(request.getVoucherTypeId(), request.getVoucherNumber(), request.getBranchId());
            throw e;
        }
    }

    /**
     * Post the issue - consumes stock using FIFO and updates ledger.
     */
    @Transactional
    public IssueHeader postIssue(Long issueId, String postedBy) {
        IssueHeader issue = issueHeaderRepository.findById(issueId)
                .orElseThrow(() -> new MmsException("Issue not found: " + issueId));

        if (STATUS_POSTED.equals(issue.getStatus())) {
            throw new MmsException("Issue is already posted");
        }

        List<IssueDetail> details = issueDetailRepository.findByIssueId(issueId);

        // Process each item using FIFO
        for (IssueDetail detail : details) {
            // Consume stock using FIFO
            FifoService.FifoConsumptionResult result = fifoService.consumeStock(
                    issue.getBranchId(),
                    detail.getItemId(),
                    detail.getLocationId(),
                    issueId,
                    detail.getQtyIssued()
            );

            // Update detail with FIFO weighted average rate
            detail.setRate(result.weightedAverageRate());
            issueDetailRepository.save(detail);

            // Record in stock ledger
            stockLedgerService.recordStockOut(
                    issue.getBranchId(),
                    detail.getItemId(),
                    detail.getLocationId(),
                    issue.getIssueDate(),
                    StockLedgerService.TXN_ISSUE,
                    issueId,
                    detail.getQtyIssued(),
                    result.weightedAverageRate(),
                    issue.getDeptId()
            );

            log.debug("Issue detail posted: Item={}, Qty={}, FifoRate={}",
                    detail.getItemId(), detail.getQtyIssued(), result.weightedAverageRate());
        }

        // Update issue status
        String prevStatus = issue.getStatus();
        issue.setStatus(STATUS_POSTED);
        issue.setApprovedBy(postedBy);
        issue.setApprovedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        issue = issueHeaderRepository.save(issue);

        editLogService.logStatusChange("ISSUE", issueId, issue.getVoucherNumber(), issue.getVoucherTypeId(),
                prevStatus, STATUS_POSTED, postedBy);
        log.info("Issue {} posted successfully by {}", issueId, postedBy);
        return issue;
    }

    /**
     * Get issue by ID.
     */
    public IssueHeader getIssue(Long issueId) {
        return issueHeaderRepository.findById(issueId)
                .orElseThrow(() -> new MmsException("Issue not found: " + issueId));
    }

    /**
     * Get issues by branch.
     */
    public Page<IssueHeader> getIssuesByBranch(String branchId, int page, int size) {
        return issueHeaderRepository.findByBranchId(branchId,
                PageRequest.of(page - 1, size, Sort.by("issueId").descending()));
    }

    /**
     * Check stock availability for an issue.
     */
    public boolean checkStockAvailability(String branchId, String itemId, String locationId, BigDecimal qty) {
        return fifoService.hasAvailableStock(branchId, itemId, locationId, qty);
    }

}
