package com.countrydelight.mms.service.transfer;

import com.countrydelight.mms.dto.transfer.DeptTransferCreateRequest;
import com.countrydelight.mms.dto.transfer.DeptTransferDetailRequest;
import com.countrydelight.mms.dto.transfer.ThirdPartySupplierRequest;
import com.countrydelight.mms.entity.master.SupplierMaster;
import com.countrydelight.mms.entity.stock.BranchMaterialStock;
import com.countrydelight.mms.entity.transfer.DeptTransferDetail;
import com.countrydelight.mms.entity.transfer.DeptTransferHeader;
import com.countrydelight.mms.exception.MmsException;
import com.countrydelight.mms.repository.master.BranchDepartmentMapRepository;
import com.countrydelight.mms.repository.master.SupplierMasterRepository;
import com.countrydelight.mms.repository.stock.BranchMaterialStockRepository;
import com.countrydelight.mms.repository.transfer.DeptTransferDetailRepository;
import com.countrydelight.mms.repository.transfer.DeptTransferHeaderRepository;
import com.countrydelight.mms.service.audit.VoucherEditLogService;
import com.countrydelight.mms.service.master.VoucherNumberService;
import com.countrydelight.mms.service.stock.StockLedgerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
/**
 * Department Transfer Service - Handles intra-branch department transfers.
 *
 * This is a lightweight transfer: physical stock doesn't move,
 * only departmental cost attribution changes via ledger entries.
 * No approval flow required. Auto-posted on creation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeptTransferService {

    private final DeptTransferHeaderRepository headerRepository;
    private final DeptTransferDetailRepository detailRepository;
    private final BranchDepartmentMapRepository branchDeptMapRepository;
    private final BranchMaterialStockRepository branchStockRepository;
    private final SupplierMasterRepository supplierMasterRepository;
    private final StockLedgerService stockLedgerService;
    private final VoucherEditLogService editLogService;
    private final VoucherNumberService voucherNumberService;

    /**
     * Create and auto-post a department transfer.
     */
    @Transactional
    public DeptTransferHeader createAndPost(DeptTransferCreateRequest request) {
        // Default toBranchId to fromBranchId when not provided (intra-branch transfer)
        if (request.getToBranchId() == null || request.getToBranchId().isBlank()) {
            request.setToBranchId(request.getFromBranchId());
        }

        // Validate source and destination are not identical
        if (request.getToDeptId() != null
                && request.getFromBranchId().equals(request.getToBranchId())
                && request.getFromDeptId().equals(request.getToDeptId())) {
            throw new MmsException("Source and destination branch-department combination cannot be the same");
        }

        // Validate each department is mapped to its respective branch
        if (!branchDeptMapRepository.existsByBranchIdAndDeptId(request.getFromBranchId(), request.getFromDeptId())) {
            throw new MmsException("Department " + request.getFromDeptId() + " is not mapped to branch " + request.getFromBranchId());
        }
        if (request.getToDeptId() != null
                && !branchDeptMapRepository.existsByBranchIdAndDeptId(request.getToBranchId(), request.getToDeptId())) {
            throw new MmsException("Department " + request.getToDeptId() + " is not mapped to branch " + request.getToBranchId());
        }

        // For transfer-in: validate the referenced transfer-out exists and hasn't been received yet, then mark it received
        if ("IN".equals(request.getTransferType()) && request.getTransferOutId() != null) {
            DeptTransferHeader transferOut = headerRepository.findById(request.getTransferOutId())
                    .orElseThrow(() -> new MmsException("Transfer out not found: " + request.getTransferOutId()));
            if (transferOut.isReceived()) {
                throw new MmsException("Transfer out " + request.getTransferOutId() + " has already been received");
            }
            transferOut.setReceived(true);
            headerRepository.save(transferOut);
        }

        // Upsert third-party supplier when mode is THIRD_PARTY
        String resolvedSupplierId = request.getThirdPartySupplierId();
        if ("THIRD_PARTY".equals(request.getTransferMode()) && request.getSupplierData() != null) {
            ThirdPartySupplierRequest sd = request.getSupplierData();
            supplierMasterRepository.save(SupplierMaster.builder()
                    .suppId(sd.getSuppId())
                    .suppName(sd.getSuppName())
                    .address(sd.getAddress())
                    .mobNo(sd.getMobNo())
                    .email(sd.getEmail())
                    .gstin(sd.getGstin())
                    .type(sd.getType())
                    .build());
            resolvedSupplierId = sd.getSuppId();
        }

        // Create header (auto-posted)
        DeptTransferHeader header = DeptTransferHeader.builder()
                .fromBranchId(request.getFromBranchId())
                .toBranchId(request.getToBranchId())
                .transferCategory(request.getTransferCategory())
                .transferType(request.getTransferType())
                .transferOutId(request.getTransferOutId())
                .transferMode(request.getTransferMode())
                .fromDeptId(request.getFromDeptId())
                .toDeptId(request.getToDeptId())
                .transferDate(request.getTransferDate())
                .status("POSTED")
                .remarks(request.getRemarks())
                .createdBy(request.getCreatedBy())
                .itemsCount(request.getItemsCount())
                .totalQty(request.getTotalQty())
                .grossAmount(request.getGrossAmount())
                .netAmount(request.getNetAmount())
                .roundingAmount(request.getRoundingAmount())
                .roundingType(request.getRoundingType())
                .thirdPartySupplierId(resolvedSupplierId)
                .voucherNumber(request.getVoucherNumber())
                .voucherTypeId(request.getVoucherTypeId())
                .build();

        try {
        header = headerRepository.save(header);
        Long headerId = header.getDeptTransferId();

        // Process each detail
        for (DeptTransferDetailRequest detailReq : request.getDetails()) {
            // Use client-provided rate if present, else fall back to avg_cost from BranchMaterialStock
            BigDecimal rate = (detailReq.getRate() != null && detailReq.getRate().compareTo(BigDecimal.ZERO) > 0)
                    ? detailReq.getRate()
                    : branchStockRepository
                            .findStockForUpdate(
                                    request.getFromBranchId(), detailReq.getItemId(), detailReq.getLocationId())
                            .map(BranchMaterialStock::getAvgCost)
                            .orElse(BigDecimal.ZERO);

            DeptTransferDetail detail = DeptTransferDetail.builder()
                    .deptTransferId(headerId)
                    .itemId(detailReq.getItemId())
                    .locationId(detailReq.getLocationId())
                    .unitId(detailReq.getUnitId())
                    .qtyTransferred(detailReq.getQtyTransferred())
                    .rate(rate)
                    .grossAmount(detailReq.getGrossAmount())
                    .discountPerc(detailReq.getDiscountPerc() != null ? detailReq.getDiscountPerc() : BigDecimal.ZERO)
                    .discountAmount(detailReq.getDiscountAmount() != null ? detailReq.getDiscountAmount() : BigDecimal.ZERO)
                    .gstPerc(detailReq.getGstPerc() != null ? detailReq.getGstPerc() : BigDecimal.ZERO)
                    .gstAmount(detailReq.getGstAmount() != null ? detailReq.getGstAmount() : BigDecimal.ZERO)
                    .cessPerc(detailReq.getCessPerc() != null ? detailReq.getCessPerc() : BigDecimal.ZERO)
                    .cessAmount(detailReq.getCessAmount() != null ? detailReq.getCessAmount() : BigDecimal.ZERO)
                    .netAmount(detailReq.getNetAmount())
                    .build();

            detailRepository.save(detail);

            // Record paired ledger entries for departmental cost attribution
            stockLedgerService.recordStockOut(
                    request.getFromBranchId(),
                    detailReq.getItemId(),
                    detailReq.getLocationId(),
                    request.getTransferDate(),
                    StockLedgerService.TXN_DEPT_TRANSFER_OUT,
                    headerId,
                    detailReq.getQtyTransferred(),
                    rate,
                    request.getFromDeptId()
            );

            stockLedgerService.recordStockIn(
                    request.getToBranchId(),
                    detailReq.getItemId(),
                    detailReq.getLocationId(),
                    request.getTransferDate(),
                    StockLedgerService.TXN_DEPT_TRANSFER_IN,
                    headerId,
                    detailReq.getQtyTransferred(),
                    rate,
                    request.getToDeptId()
            );
        }

        DeptTransferHeader saved = headerRepository.findByIdWithDetails(headerId).orElse(header);
        editLogService.logCreate("DEPT_TRANSFER", headerId, saved.getVoucherNumber(), saved.getVoucherTypeId(),
                saved.getCreatedBy(), "Dept transfer from " + request.getFromBranchId() + " to " + request.getToBranchId());
        log.info("Dept transfer created and posted: ID={}, FromBranch={}, ToBranch={}, FromDept={}, ToDept={}",
                headerId, request.getFromBranchId(), request.getToBranchId(), request.getFromDeptId(), request.getToDeptId());

        return saved;
        } catch (RuntimeException e) {
            voucherNumberService.returnVoucherNumber(request.getVoucherTypeId(), request.getVoucherNumber(), request.getFromBranchId());
            throw e;
        }
    }


    @Transactional(readOnly = true)
    public DeptTransferHeader getById(Long id) {
        return headerRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new MmsException("Department transfer not found: " + id));
    }

    public Page<DeptTransferHeader> getByBranch(String branchId, Integer deptId, int page, int size) {
        return headerRepository.findByFiltersPaged(
                branchId, deptId,
                PageRequest.of(page - 1, size, Sort.by("deptTransferId").descending()));
    }
}
