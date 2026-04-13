package com.countrydelight.mms.service.transfer;

import com.countrydelight.mms.dto.transfer.TransferCreateRequest;
import com.countrydelight.mms.dto.transfer.TransferDetailRequest;
import com.countrydelight.mms.entity.inward.GrnDetail;
import com.countrydelight.mms.entity.inward.GrnHeader;
import com.countrydelight.mms.entity.outward.IssueDetail;
import com.countrydelight.mms.entity.outward.IssueHeader;
import com.countrydelight.mms.entity.transfer.StockTransferDetail;
import com.countrydelight.mms.entity.transfer.StockTransferHeader;
import com.countrydelight.mms.exception.InsufficientStockException;
import com.countrydelight.mms.exception.MmsException;
import com.countrydelight.mms.repository.inward.GrnDetailRepository;
import com.countrydelight.mms.repository.inward.GrnHeaderRepository;
import com.countrydelight.mms.repository.outward.IssueDetailRepository;
import com.countrydelight.mms.repository.outward.IssueHeaderRepository;
import com.countrydelight.mms.repository.transfer.StockTransferDetailRepository;
import com.countrydelight.mms.repository.transfer.StockTransferHeaderRepository;
import com.countrydelight.mms.service.audit.VoucherEditLogService;
import com.countrydelight.mms.service.master.VoucherNumberService;
import com.countrydelight.mms.service.stock.FifoService;
import com.countrydelight.mms.service.stock.StockLedgerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Transfer Service - Handles inter-branch transfers.
 *
 * Transfer Flow:
 * 1. CREATE: Transfer is created in CREATED status
 * 2. DISPATCH: Sender creates an Issue (TRANSFER_OUT), status becomes IN_TRANSIT
 * 3. RECEIVE: Receiver creates a GRN (TRANSFER_IN), status becomes RECEIVED
 *
 * IMPORTANT: Inter-branch transfer requires:
 * - Sender Issue (TRANSFER_OUT in ledger)
 * - Receiver GRN (TRANSFER_IN in ledger)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    private final StockTransferHeaderRepository transferHeaderRepository;
    private final StockTransferDetailRepository transferDetailRepository;
    private final IssueHeaderRepository issueHeaderRepository;
    private final IssueDetailRepository issueDetailRepository;
    private final GrnHeaderRepository grnHeaderRepository;
    private final GrnDetailRepository grnDetailRepository;
    private final FifoService fifoService;
    private final StockLedgerService stockLedgerService;
    private final VoucherEditLogService editLogService;
    private final VoucherNumberService voucherNumberService;

    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_IN_TRANSIT = "IN_TRANSIT";
    public static final String STATUS_RECEIVED = "RECEIVED";

    /**
     * Create a new transfer request.
     */
    @Transactional
    public StockTransferHeader createTransfer(TransferCreateRequest request) {
        if (request.getFromBranch().equals(request.getToBranch())) {
            throw new MmsException("Source and destination branches cannot be the same");
        }

        // Validate stock availability at source
        for (TransferDetailRequest detailReq : request.getDetails()) {
            BigDecimal available = fifoService.getAvailableStock(
                    request.getFromBranch(), detailReq.getItemId(), detailReq.getSourceLocationId());

            if (available.compareTo(detailReq.getQtySent()) < 0) {
                throw new InsufficientStockException(
                        detailReq.getItemId(), detailReq.getSourceLocationId(),
                        detailReq.getQtySent(), available);
            }
        }

        // Create transfer header
        StockTransferHeader transfer = StockTransferHeader.builder()
                .fromBranch(request.getFromBranch())
                .toBranch(request.getToBranch())
                .deptId(request.getDeptId())
                .transferDate(request.getTransferDate())
                .status(STATUS_CREATED)
                .remarks(request.getRemarks())
                .createdBy(request.getCreatedBy())
                .voucherNumber(request.getVoucherNumber())
                .voucherTypeId(request.getVoucherTypeId())
                .build();

        try {
            transfer = transferHeaderRepository.save(transfer);
            Long transferId = transfer.getTransferId();

            // Create transfer details
            for (TransferDetailRequest detailReq : request.getDetails()) {
                StockTransferDetail detail = StockTransferDetail.builder()
                        .transferId(transferId)
                        .itemId(detailReq.getItemId())
                        .qtySent(detailReq.getQtySent())
                        .rate(BigDecimal.ZERO) // Will be calculated from FIFO during dispatch
                        .qtyReceived(BigDecimal.ZERO)
                        .build();

                transferDetailRepository.save(detail);
            }

            StockTransferHeader saved = transferHeaderRepository.findById(transferId).orElse(transfer);
            editLogService.logCreate("STOCK_TRANSFER", transferId, saved.getVoucherNumber(), saved.getVoucherTypeId(),
                    saved.getCreatedBy(), "Stock transfer from " + request.getFromBranch() + " to " + request.getToBranch());
            log.info("Transfer created: ID={}, From={}, To={}",
                    transferId, request.getFromBranch(), request.getToBranch());

            return saved;
        } catch (RuntimeException e) {
            voucherNumberService.returnVoucherNumber(request.getVoucherTypeId(), request.getVoucherNumber(), request.getFromBranch());
            throw e;
        }
    }

    /**
     * Dispatch transfer from sender branch.
     * Creates an Issue at sender branch and updates ledger with TRANSFER_OUT.
     */
    @Transactional
    public StockTransferHeader dispatchTransfer(Long transferId, String sourceLocationId, String dispatchedBy) {
        StockTransferHeader transfer = transferHeaderRepository.findById(transferId)
                .orElseThrow(() -> new MmsException("Transfer not found: " + transferId));

        if (!STATUS_CREATED.equals(transfer.getStatus())) {
            throw new MmsException("Transfer is not in CREATED status");
        }

        List<StockTransferDetail> details = transferDetailRepository.findByTransferId(transferId);

        // Create Issue at sender branch
        IssueHeader issue = IssueHeader.builder()
                .branchId(transfer.getFromBranch())
                .issueDate(transfer.getTransferDate())
                .issuedTo("TRANSFER-" + transferId + " to " + transfer.getToBranch())
                .status("POSTED")
                .remarks("Inter-branch transfer")
                .createdBy(dispatchedBy)
                .approvedBy(dispatchedBy)
                .approvedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")))
                .build();

        issue = issueHeaderRepository.save(issue);

        // Process each item
        for (StockTransferDetail detail : details) {
            // FIFO consumption at sender
            FifoService.FifoConsumptionResult result = fifoService.consumeStock(
                    transfer.getFromBranch(),
                    detail.getItemId(),
                    sourceLocationId,
                    issue.getIssueId(),
                    detail.getQtySent()
            );

            // Update transfer detail with FIFO rate
            detail.setRate(result.weightedAverageRate());
            transferDetailRepository.save(detail);

            // Create issue detail
            IssueDetail issueDetail = IssueDetail.builder()
                    .issueId(issue.getIssueId())
                    .itemId(detail.getItemId())
                    .locationId(sourceLocationId)
                    .qtyIssued(detail.getQtySent())
                    .rate(result.weightedAverageRate())
                    .build();
            issueDetailRepository.save(issueDetail);

            // Record TRANSFER_OUT in ledger
            stockLedgerService.recordStockOut(
                    transfer.getFromBranch(),
                    detail.getItemId(),
                    sourceLocationId,
                    transfer.getTransferDate(),
                    StockLedgerService.TXN_TRANSFER_OUT,
                    transferId,
                    detail.getQtySent(),
                    result.weightedAverageRate(),
                    transfer.getDeptId()
            );
        }

        // Update transfer status
        transfer.setSenderIssueId(issue.getIssueId());
        transfer.setStatus(STATUS_IN_TRANSIT);
        transfer = transferHeaderRepository.save(transfer);

        log.info("Transfer {} dispatched from branch {}", transferId, transfer.getFromBranch());
        return transfer;
    }

    /**
     * Receive transfer at destination branch.
     * Creates a GRN at receiver branch and updates ledger with TRANSFER_IN.
     */
    @Transactional
    public StockTransferHeader receiveTransfer(Long transferId, String destLocationId, String receivedBy) {
        StockTransferHeader transfer = transferHeaderRepository.findById(transferId)
                .orElseThrow(() -> new MmsException("Transfer not found: " + transferId));

        if (!STATUS_IN_TRANSIT.equals(transfer.getStatus())) {
            throw new MmsException("Transfer is not in IN_TRANSIT status");
        }

        List<StockTransferDetail> details = transferDetailRepository.findByTransferId(transferId);

        // Create GRN at receiver branch
        GrnHeader grn = GrnHeader.builder()
                .branchId(transfer.getToBranch())
                .suppId("TRANSFER") // Internal transfer
                .grnDate(transfer.getTransferDate())
                .challanNo("TRANSFER-" + transferId)
                .status("POSTED")
                .remarks("Inter-branch transfer from " + transfer.getFromBranch())
                .createdBy(receivedBy)
                .approvedBy(receivedBy)
                .approvedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")))
                .build();

        grn = grnHeaderRepository.save(grn);

        // Process each item
        for (StockTransferDetail detail : details) {
            // Use same rate as dispatch (transfer rate)
            BigDecimal transferRate = detail.getRate();
            BigDecimal qtyReceived = detail.getQtySent(); // Assume full receipt for now

            // Create GRN detail
            GrnDetail grnDetail = GrnDetail.builder()
                    .grnId(grn.getGrnId())
                    .itemId(detail.getItemId())
                    .unitId("PCS") // Default unit
                    .locationId(destLocationId)
                    .qtyReceived(qtyReceived)
                    .rate(transferRate)
                    .gstPerc(BigDecimal.ZERO)
                    .grossAmount(qtyReceived.multiply(transferRate))
                    .qtyRemaining(qtyReceived) // For FIFO
                    .build();
            grnDetailRepository.save(grnDetail);

            // Update transfer detail received qty
            detail.setQtyReceived(qtyReceived);
            transferDetailRepository.save(detail);

            // Record TRANSFER_IN in ledger
            stockLedgerService.recordStockIn(
                    transfer.getToBranch(),
                    detail.getItemId(),
                    destLocationId,
                    transfer.getTransferDate(),
                    StockLedgerService.TXN_TRANSFER_IN,
                    transferId,
                    qtyReceived,
                    transferRate,
                    transfer.getDeptId()
            );
        }

        // Update transfer status
        transfer.setReceiverGrnId(grn.getGrnId());
        transfer.setStatus(STATUS_RECEIVED);
        transfer.setApprovedBy(receivedBy);
        transfer.setApprovedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        transfer = transferHeaderRepository.save(transfer);

        log.info("Transfer {} received at branch {}", transferId, transfer.getToBranch());
        return transfer;
    }

    /**
     * Get transfer by ID.
     */
    public StockTransferHeader getTransfer(Long transferId) {
        return transferHeaderRepository.findById(transferId)
                .orElseThrow(() -> new MmsException("Transfer not found: " + transferId));
    }

    /**
     * Get pending dispatch transfers for a branch.
     */
    public Page<StockTransferHeader> getPendingDispatch(String branchId, int page, int size) {
        return transferHeaderRepository.findPendingDispatchForBranch(branchId,
                PageRequest.of(page - 1, size));
    }

    /**
     * Get pending receipt transfers for a branch.
     */
    public Page<StockTransferHeader> getPendingReceipt(String branchId, int page, int size) {
        return transferHeaderRepository.findPendingReceiptForBranch(branchId,
                PageRequest.of(page - 1, size));
    }

}
