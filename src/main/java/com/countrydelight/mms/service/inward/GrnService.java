package com.countrydelight.mms.service.inward;

import com.countrydelight.mms.dto.inward.GrnCreateRequest;
import com.countrydelight.mms.dto.inward.GrnDetailRequest;
import com.countrydelight.mms.dto.inward.PriceSuggestionResponse;
import com.countrydelight.mms.dto.inward.PriceVarianceInfo;
import com.countrydelight.mms.entity.inward.GrnDetail;
import com.countrydelight.mms.entity.inward.GrnHeader;
import com.countrydelight.mms.entity.master.ItemMaster;
import com.countrydelight.mms.entity.purchase.PurchaseVoucherHeader;
import com.countrydelight.mms.exception.ApprovalRequiredException;
import com.countrydelight.mms.exception.MmsException;
import com.countrydelight.mms.repository.inward.GrnDetailRepository;
import com.countrydelight.mms.repository.inward.GrnHeaderRepository;
import com.countrydelight.mms.repository.master.BranchItemPriceRepository;
import com.countrydelight.mms.repository.master.ItemMasterRepository;
import com.countrydelight.mms.repository.purchase.PurchaseVoucherHeaderRepository;
import com.countrydelight.mms.service.approval.ApprovalService;
import com.countrydelight.mms.service.audit.VoucherEditLogService;
import com.countrydelight.mms.service.master.VoucherNumberService;
import com.countrydelight.mms.service.stock.StockLedgerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * GRN Service - Handles Goods Receipt Notes.
 *
 * GRN is the ONLY way to increase stock in the system.
 *
 * PRICING LOGIC:
 * 1. During GRN entry, system auto-fetches last GRN rate from ledger
 * 2. User can modify price based on supplier invoice
 * 3. Price variance is calculated against last GRN rate
 * 4. Approval rules are enforced for variance beyond threshold
 * 5. Final accepted price is stored in GRN and ledger
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GrnService {

    private final GrnHeaderRepository grnHeaderRepository;
    private final GrnDetailRepository grnDetailRepository;
    private final ItemMasterRepository itemMasterRepository;
    private final PurchaseVoucherHeaderRepository pvHeaderRepository;
    private final StockLedgerService stockLedgerService;
    private final ApprovalService approvalService;
    private final BranchItemPriceRepository branchItemPriceRepository;
    private final VoucherEditLogService editLogService;
    private final VoucherNumberService voucherNumberService;

    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_PENDING_APPROVAL = "PENDING_APPROVAL";
    public static final String STATUS_POSTED = "POSTED";

    /**
     * Get price suggestion for an item at a branch.
     * Returns the last GRN rate from ledger (primary) or item master rate (fallback).
     */
    public PriceSuggestionResponse getPriceSuggestion(String branchId, String itemId) {
        ItemMaster item = itemMasterRepository.findById(itemId)
                .orElseThrow(() -> new MmsException("Item not found: " + itemId));

        // Try to get last GRN rate from ledger
        BigDecimal lastGrnRate = stockLedgerService.getLastGrnRate(branchId, itemId);
        GrnDetail lastGrn = grnDetailRepository.findLastGrnForItem(branchId, itemId);

        // Get branch-level price if available
        BigDecimal branchCostPrice = branchItemPriceRepository
                .findByBranchIdAndItemId(branchId, itemId)
                .map(bp -> bp.getCostPrice())
                .orElse(null);

        PriceSuggestionResponse.PriceSource source;
        if (lastGrnRate != null && lastGrnRate.compareTo(BigDecimal.ZERO) > 0) {
            source = PriceSuggestionResponse.PriceSource.LAST_GRN;
        } else if (branchCostPrice != null && branchCostPrice.compareTo(BigDecimal.ZERO) > 0) {
            source = PriceSuggestionResponse.PriceSource.BRANCH_PRICE;
            lastGrnRate = branchCostPrice;
        } else if (item.getCostPrice() != null && item.getCostPrice().compareTo(BigDecimal.ZERO) > 0) {
            source = PriceSuggestionResponse.PriceSource.ITEM_MASTER;
            lastGrnRate = item.getCostPrice();
        } else {
            source = PriceSuggestionResponse.PriceSource.NO_HISTORY;
        }

        return PriceSuggestionResponse.builder()
                .itemId(itemId)
                .itemDesc(item.getItemDesc())
                .branchId(branchId)
                .lastGrnRate(lastGrnRate)
                .lastGrnDate(lastGrn != null ? lastGrn.getGrnHeader().getGrnDate() : null)
                .lastGrnId(lastGrn != null ? lastGrn.getGrnId() : null)
                .branchCostPrice(branchCostPrice)
                .itemMasterCostPrice(item.getCostPrice())
                .priceSource(source)
                .build();
    }

    /**
     * Create a new GRN in DRAFT status.
     */
    @Transactional
    public GrnHeader createGrn(GrnCreateRequest request) {
        // Create GRN header
        GrnHeader grn = GrnHeader.builder()
                .branchId(request.getBranchId())
                .deptId(request.getDeptId())
                .suppId(request.getSuppId())
                .pvId(request.getPoId())
                .invoiceId(request.getInvoiceId())
                .challanNo(request.getChallanNo())
                .challanDate(request.getChallanDate())
                .invoiceDate(request.getInvoiceDate())
                .grnDate(request.getGrnDate())
                .status(STATUS_DRAFT)
                .remarks(request.getRemarks())
                .itemsCount(request.getItemsCount())
                .totalQty(request.getTotalQty())
                .grossAmount(request.getGrossAmount())
                .netAmount(request.getNetAmount())
                .roundOffAmount(request.getRoundOffAmount())
                .createdBy(request.getCreatedBy())
                .voucherNumber(request.getVoucherNumber())
                .voucherTypeId(request.getVoucherTypeId())
                .build();

        try {
            grn = grnHeaderRepository.save(grn);
            Long grnId = grn.getGrnId();

            // Create GRN details
            for (GrnDetailRequest detailReq : request.getDetails()) {
                ItemMaster item = itemMasterRepository.findById(detailReq.getItemId())
                        .orElseThrow(() -> new MmsException("Item not found: " + detailReq.getItemId()));

                BigDecimal gstPerc = detailReq.getGstPerc() != null ? detailReq.getGstPerc() : item.getGstPerc();
                BigDecimal grossAmount = detailReq.getGrossAmount() != null
                        ? detailReq.getGrossAmount()
                        : detailReq.getQtyReceived().multiply(detailReq.getRate());

                GrnDetail detail = GrnDetail.builder()
                        .grnId(grnId)
                        .itemId(detailReq.getItemId())
                        .unitId(detailReq.getUnitId())
                        .locationId(detailReq.getLocationId())
                        .qtyReceived(detailReq.getQtyReceived())
                        .rate(detailReq.getRate())
                        .grossAmount(grossAmount)
                        .gstPerc(gstPerc)
                        .gstAmount(detailReq.getGstAmount())
                        .discountPerc(detailReq.getDiscountPerc())
                        .discountAmount(detailReq.getDiscountAmount())
                        .cessPerc(detailReq.getCessPerc())
                        .cessAmount(detailReq.getCessAmount())
                        .netAmount(detailReq.getNetAmount())
                        .qtyRemaining(detailReq.getQtyReceived()) // Initialize for FIFO
                        .build();

                grnDetailRepository.save(detail);
            }

            // Auto-post immediately — no DRAFT/approval step
            return postGrn(grnId, request.getCreatedBy());
        } catch (RuntimeException e) {
            voucherNumberService.returnVoucherNumber(request.getVoucherTypeId(), request.getVoucherNumber(), request.getBranchId());
            throw e;
        }
    }

    /**
     * Submit GRN for approval. Checks price variance and determines if approval is needed.
     */
    @Transactional
    public GrnHeader submitForApproval(Long grnId, String submittedBy) {
        GrnHeader grn = grnHeaderRepository.findById(grnId)
                .orElseThrow(() -> new MmsException("GRN not found: " + grnId));

        if (!STATUS_DRAFT.equals(grn.getStatus())) {
            throw new MmsException("GRN is not in DRAFT status");
        }

        List<GrnDetail> details = grnDetailRepository.findByGrnId(grnId);
        List<PriceVarianceInfo> variances = new ArrayList<>();

        // Check price variance for each item
        for (GrnDetail detail : details) {
            ItemMaster item = itemMasterRepository.findById(detail.getItemId()).orElse(null);
            BigDecimal lastRate = stockLedgerService.getLastGrnRate(grn.getBranchId(), detail.getItemId());

            PriceVarianceInfo variance = approvalService.checkPriceVariance(
                    "GRN",
                    lastRate,
                    detail.getRate(),
                    detail.getItemId(),
                    item != null ? item.getItemDesc() : "Unknown"
            );

            if (variance.isRequiresApproval()) {
                variances.add(variance);
            }
        }

        // If any variance requires approval, set status to PENDING_APPROVAL
        if (!variances.isEmpty()) {
            grn.setStatus(STATUS_PENDING_APPROVAL);
            grnHeaderRepository.save(grn);
            editLogService.logStatusChange("GRN", grnId, grn.getVoucherNumber(), grn.getVoucherTypeId(),
                    STATUS_DRAFT, STATUS_PENDING_APPROVAL, submittedBy);
            log.info("GRN {} submitted for approval due to price variance", grnId);
            throw new ApprovalRequiredException("GRN", variances);
        }

        // No approval needed, auto-post
        return postGrn(grnId, submittedBy);
    }

    /**
     * Approve and post the GRN. Updates stock ledger and PO received quantities.
     */
    @Transactional
    public GrnHeader approveAndPost(Long grnId, String approvedBy) {
        GrnHeader grn = grnHeaderRepository.findById(grnId)
                .orElseThrow(() -> new MmsException("GRN not found: " + grnId));

        if (!STATUS_PENDING_APPROVAL.equals(grn.getStatus())) {
            throw new MmsException("GRN is not pending approval");
        }

        // Verify approver has authority
        List<GrnDetail> details = grnDetailRepository.findByGrnId(grnId);
        List<PriceVarianceInfo> variances = new ArrayList<>();

        for (GrnDetail detail : details) {
            ItemMaster item = itemMasterRepository.findById(detail.getItemId()).orElse(null);
            BigDecimal lastRate = stockLedgerService.getLastGrnRate(grn.getBranchId(), detail.getItemId());

            PriceVarianceInfo variance = approvalService.checkPriceVariance(
                    "GRN", lastRate, detail.getRate(), detail.getItemId(),
                    item != null ? item.getItemDesc() : "Unknown");

            if (variance.isRequiresApproval()) {
                variances.add(variance);
            }
        }

        if (!approvalService.canApprove(approvedBy, variances)) {
            throw new MmsException("User does not have authority to approve this GRN");
        }

        return postGrn(grnId, approvedBy);
    }

    /**
     * Post the GRN - updates stock ledger and marks GRN as POSTED.
     */
    private GrnHeader postGrn(Long grnId, String approvedBy) {
        GrnHeader grn = grnHeaderRepository.findById(grnId)
                .orElseThrow(() -> new MmsException("GRN not found: " + grnId));

        List<GrnDetail> details = grnDetailRepository.findByGrnId(grnId);

        // Post each item to stock ledger
        for (GrnDetail detail : details) {
            stockLedgerService.recordStockIn(
                    grn.getBranchId(),
                    detail.getItemId(),
                    detail.getLocationId(),
                    grn.getGrnDate(),
                    StockLedgerService.TXN_GRN,
                    grnId,
                    detail.getQtyReceived(),
                    detail.getRate(),
                    grn.getDeptId()
            );

        }

        // Update GRN status
        String prevStatus = grn.getStatus();
        grn.setStatus(STATUS_POSTED);
        grn.setApprovedBy(approvedBy);
        grn.setApprovedAt(java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Kolkata")));
        grn = grnHeaderRepository.save(grn);

        // Update PV receipt status AFTER GRN is saved as POSTED so findByPvIdAndStatus finds it
        if (grn.getPvId() != null) {
            updatePvReceiptStatus(grn.getPvId());
        }

        editLogService.logStatusChange("GRN", grnId, grn.getVoucherNumber(), grn.getVoucherTypeId(),
                prevStatus, STATUS_POSTED, approvedBy);
        if (!STATUS_DRAFT.equals(prevStatus)) {
            editLogService.logApprove("GRN", grnId, grn.getVoucherNumber(), grn.getVoucherTypeId(), approvedBy);
        }
        log.info("GRN {} posted successfully by {}", grnId, approvedBy);
        return grn;
    }

    /**
     * Update PV receipt status (PARTIAL / COMPLETED) after a GRN is posted.
     */
    private void updatePvReceiptStatus(Long pvId) {
        PurchaseVoucherHeader pv = pvHeaderRepository.findById(pvId).orElse(null);
        if (pv == null) {
            return;
        }

        List<GrnHeader> postedGrns = grnHeaderRepository.findByPvIdAndStatus(pvId, STATUS_POSTED);

        // Sum received qty per item across all posted GRNs for this PV
        java.util.Map<String, BigDecimal> receivedQtyByItem = new java.util.HashMap<>();
        for (GrnHeader g : postedGrns) {
            List<com.countrydelight.mms.entity.inward.GrnDetail> grnDetails =
                    grnDetailRepository.findByGrnId(g.getGrnId());
            for (com.countrydelight.mms.entity.inward.GrnDetail d : grnDetails) {
                receivedQtyByItem.merge(d.getItemId(), d.getQtyReceived(), BigDecimal::add);
            }
        }

        boolean allReceived = true;
        boolean anyReceived = false;

        for (com.countrydelight.mms.entity.purchase.PurchaseVoucherDetail pvDetail : pv.getDetails()) {
            BigDecimal received = receivedQtyByItem.getOrDefault(pvDetail.getItemId(), BigDecimal.ZERO);
            if (received.compareTo(BigDecimal.ZERO) > 0) {
                anyReceived = true;
            }
            if (received.compareTo(pvDetail.getQty()) < 0) {
                allReceived = false;
            }
        }

        if (allReceived && anyReceived) {
            pv.setStatus("COMPLETED");
        } else if (anyReceived) {
            pv.setStatus("PARTIAL");
        }

        pvHeaderRepository.save(pv);
    }

    /**
     * Get GRNs by optional filters.
     */
    @Transactional(readOnly = true)
    public Page<GrnHeader> getGrns(Long grnId, String branchId, Long pvId, int page, int size) {
        return grnHeaderRepository.findByFilters(grnId, branchId, pvId,
                PageRequest.of(page - 1, size, Sort.by("grnId").descending()));
    }

    /**
     * Get GRNs by branch.
     */
    public Page<GrnHeader> getGrnsByBranch(String branchId, int page, int size) {
        return grnHeaderRepository.findByBranchId(branchId,
                PageRequest.of(page - 1, size, Sort.by("grnId").descending()));
    }

    /**
     * Get pending approval GRNs.
     */
    public Page<GrnHeader> getPendingApprovalGrns(int page, int size) {
        return grnHeaderRepository.findPendingApproval(
                PageRequest.of(page - 1, size));
    }
}
