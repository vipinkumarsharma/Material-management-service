package com.countrydelight.mms.service.purchase;

import com.countrydelight.mms.dto.purchase.PurchaseVoucherCreateRequest;
import com.countrydelight.mms.dto.purchase.PurchaseVoucherDetailRequest;
import com.countrydelight.mms.dto.purchase.ThirdPartySupplierRequest;
import com.countrydelight.mms.entity.inward.GrnDetail;
import com.countrydelight.mms.entity.inward.GrnHeader;
import com.countrydelight.mms.dto.purchase.SupplierGodownRequest;
import com.countrydelight.mms.entity.master.SupplierGodownMap;
import com.countrydelight.mms.entity.master.SupplierMaster;
import com.countrydelight.mms.entity.master.VoucherTypeMaster;
import com.countrydelight.mms.entity.purchase.PurchaseVoucherDetail;
import com.countrydelight.mms.entity.purchase.PurchaseVoucherDetailTo;
import com.countrydelight.mms.entity.purchase.PurchaseVoucherHeader;
import com.countrydelight.mms.exception.MmsException;
import com.countrydelight.mms.repository.inward.GrnDetailRepository;
import com.countrydelight.mms.repository.inward.GrnHeaderRepository;
import com.countrydelight.mms.repository.master.SupplierGodownMapRepository;
import com.countrydelight.mms.repository.master.SupplierMasterRepository;
import com.countrydelight.mms.repository.master.VoucherTypeMasterRepository;
import com.countrydelight.mms.repository.purchase.PurchaseVoucherHeaderRepository;
import com.countrydelight.mms.service.audit.VoucherEditLogService;
import com.countrydelight.mms.service.master.GodownItemStockService;
import com.countrydelight.mms.service.master.VoucherNumberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.countrydelight.mms.dto.purchase.PurchaseVoucherRegisterRow;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseVoucherService {

    private static final String ENTITY_TYPE = "PURCHASE_VOUCHER";
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_PENDING_APPROVAL = "PENDING_APPROVAL";
    private static final String STATUS_POSTED = "POSTED";
    public static final String STATUS_PRE_CLOSED = "PRE_CLOSED";

    private final PurchaseVoucherHeaderRepository pvHeaderRepository;
    private final GrnHeaderRepository grnHeaderRepository;
    private final GrnDetailRepository grnDetailRepository;
    private final VoucherTypeMasterRepository voucherTypeRepository;
    private final SupplierMasterRepository supplierMasterRepository;
    private final SupplierGodownMapRepository supplierGodownMapRepository;
    private final VoucherEditLogService editLogService;
    private final VoucherNumberService voucherNumberService;
    private final PurchaseVoucherStockService pvStockService;
    private final GodownItemStockService godownItemStockService;

    /**
     * Create a Purchase Voucher from a posted GRN (TallyPrime 3-step flow).
     */
    @Transactional
    public PurchaseVoucherHeader createFromGrn(Long grnId, PurchaseVoucherCreateRequest request, boolean preClose) {
        GrnHeader grn = grnHeaderRepository.findById(grnId)
                .orElseThrow(() -> new MmsException("GRN not found: " + grnId));

        if (!"POSTED".equals(grn.getStatus())) {
            throw new MmsException("GRN must be in POSTED status to create a Purchase Voucher");
        }

        if (grn.getPvId() != null) {
            throw new MmsException("A Purchase Voucher already exists for GRN: " + grnId);
        }

        String voucherTypeId = resolveVoucherTypeId(request.getVoucherTypeId(), grn.getBranchId());
        VoucherTypeMaster voucherType = voucherTypeId != null
                ? voucherTypeRepository.findById(voucherTypeId).orElse(null)
                : null;

        if (voucherType != null && voucherType.isUseEffectiveDates() && request.getEffectiveDate() == null) {
            throw new MmsException("Effective date is required for this voucher type");
        }

        String voucherNumber = (request.getVoucherNumber() != null && !request.getVoucherNumber().isBlank())
                ? request.getVoucherNumber()
                : (voucherTypeId != null ? voucherNumberService.generateVoucherNumber(voucherTypeId, grn.getBranchId()) : null);
        try {
            PurchaseVoucherHeader pv = PurchaseVoucherHeader.builder()
                    .voucherTypeId(voucherTypeId)
                    .branchId(grn.getBranchId())
                    .fromBranchId(request.getFromBranchId())
                    .fromEntityType(request.getFromEntityType())
                    .supplierFromId(grn.getSuppId())
                    .consigneeType(request.getConsigneeType())
                    .consigneeId(request.getConsigneeId())
                    .consigneeEntityType(request.getConsigneeEntityType())
                    .pvDate(request.getPvDate() != null ? request.getPvDate() : grn.getGrnDate())
                    .effectiveDate(request.getEffectiveDate())
                    .supplierInvoiceId(grn.getInvoiceId())
                    .supplierInvoiceNo(request.getSupplierInvoiceNo())
                    .supplierInvoiceDate(request.getSupplierInvoiceDate())
                    .modeOfPayment(request.getModeOfPayment())
                    .otherReferences(request.getOtherReferences())
                    .termsOfDelivery(request.getTermsOfDelivery())
                    .dispatchThrough(request.getDispatchThrough())
                    .destination(request.getDestination())
                    .carrierNameAgent(request.getCarrierNameAgent())
                    .billOfLadingNo(request.getBillOfLadingNo())
                    .motorVehicleNo(request.getMotorVehicleNo())
                    .dispatchDocNo(request.getDispatchDocNo())
                    .trackingNo(request.getTrackingNo())
                    .deliveryAddress(request.getDeliveryAddress())
                    .dueDate(request.getDueDate())
                    .processDescription(request.getProcessDescription())
                    .sourceGodownId(request.getSourceGodownId())
                    .destinationGodownId(request.getDestinationGodownId())
                    .transferReason(request.getTransferReason())
                    .referenceNo(request.getReferenceNo())
                    .purposeOfIssue(request.getPurposeOfIssue())
                    .narration(request.getNarration())
                    .challanNo(request.getChallanNo())
                    .challanDate(request.getChallanDate())
                    .linkedPoId(request.getLinkedPoId())
                    .voucherNumber(voucherNumber)
                    .createdBy(request.getCreatedBy() != null ? String.valueOf(request.getCreatedBy()) : null)
                    .grossAmount(request.getGrossAmount())
                    .discountAmount(request.getDiscountAmount())
                    .gstAmount(request.getGstAmount())
                    .cessAmount(request.getCessAmount())
                    .netAmount(request.getNetAmount())
                    .roundOffAmount(request.getRoundOffAmount())
                    .poAmountBreakPoint(request.getPoAmountBreakPoint())
                    .deptId(request.getFromDepartmentId())
                    .status(STATUS_POSTED)
                    .approvedBy(request.getCreatedBy() != null ? String.valueOf(request.getCreatedBy()) : null)
                    .approvedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")))
                    .build();

            pv = pvHeaderRepository.save(pv);
            Long pvId = pv.getPvId();

            List<GrnDetail> grnDetails = grnDetailRepository.findByGrnId(grnId);
            for (GrnDetail gd : grnDetails) {
                PurchaseVoucherDetail detail = PurchaseVoucherDetail.builder()
                        .pvId(pvId)
                        .itemId(gd.getItemId())
                        .unitId(gd.getUnitId())
                        .locationId(gd.getLocationId())
                        .qty(gd.getQtyReceived())
                        .orderedQty(gd.getQtyReceived())
                        .rate(gd.getRate())
                        .grossAmount(gd.getGrossAmount().setScale(2, java.math.RoundingMode.HALF_UP))
                        .discountPerc(gd.getDiscountPerc())
                        .discountAmount(gd.getDiscountAmount().setScale(2, java.math.RoundingMode.HALF_UP))
                        .gstPerc(gd.getGstPerc())
                        .gstAmount(gd.getGstAmount().setScale(2, java.math.RoundingMode.HALF_UP))
                        .cessPerc(gd.getCessPerc())
                        .cessAmount(gd.getCessAmount().setScale(2, java.math.RoundingMode.HALF_UP))
                        .netAmount(gd.getNetAmount().setScale(2, java.math.RoundingMode.HALF_UP))
                        .build();
                pv.addDetail(detail);
            }
            if (request.getDetailsTo() != null) {
                for (PurchaseVoucherDetailRequest dr : request.getDetailsTo()) {
                    PurchaseVoucherDetailTo detail = PurchaseVoucherDetailTo.builder()
                            .pvId(pvId)
                            .itemId(dr.getItemId())
                            .unitId(dr.getUnitId())
                            .locationId(dr.getLocationId())
                            .qty(dr.getQty())
                            .rate(dr.getRate())
                            .grossAmount(dr.getGrossAmount())
                            .discountPerc(dr.getDiscountPerc())
                            .discountAmount(dr.getDiscountAmount())
                            .gstPerc(dr.getGstPerc())
                            .gstAmount(dr.getGstAmount())
                            .cessPerc(dr.getCessPerc())
                            .cessAmount(dr.getCessAmount())
                            .netAmount(dr.getNetAmount())
                            .orderedQty(dr.getQty())
                            .lineNarration(dr.getLineNarration())
                            .build();
                    pv.addDetailTo(detail);
                }
            }

            pv = pvHeaderRepository.save(pv);
            decreaseLinkedPoQty(pv);
            if (preClose && pv.getLinkedPoId() != null && !pv.getLinkedPoId().isBlank()) {
                preClosePo(pv.getLinkedPoId());
            }

            // Back-link GRN to this PV
            grn.setPvId(pv.getPvId());
            grnHeaderRepository.save(grn);

            editLogService.logCreate(ENTITY_TYPE, pvId, voucherNumber, voucherTypeId,
                    request.getCreatedBy() != null ? String.valueOf(request.getCreatedBy()) : null,
                    "Created from GRN#" + grnId);

            SupplierGodownMap savedGodown = upsertSupplierGodown(request.getSupGodown(), grn.getSuppId());
            applyGodownIdToPv(pv, savedGodown);
            pv.setSupGodown(savedGodown);
            pvStockService.applyStock(pv, false);
            if (savedGodown != null) {
                godownItemStockService.applyGodownStock(
                    savedGodown.getId(), pv.getVoucherCategory(), pv.getDetails(), false);
            }
            log.info("Purchase Voucher {} created from GRN {}", pvId, grnId);
            return pv;
        } catch (RuntimeException e) {
            voucherNumberService.returnVoucherNumber(voucherTypeId, voucherNumber, grn.getBranchId());
            throw e;
        }
    }

    /**
     * Create a Purchase Voucher manually (without GRN link).
     */
    @Transactional
    public PurchaseVoucherHeader createManual(PurchaseVoucherCreateRequest request, boolean preClose) {
        // If third-party, resolve/create supplier and override supplierFromId
        if (request.isThirdParty() && request.getThirdPartySupplier() != null) {
            String resolvedSuppId = resolveThirdPartySupplier(request.getThirdPartySupplier());
            request.setSupplierToId(resolvedSuppId);
        }

        String voucherTypeId = resolveVoucherTypeId(request.getVoucherTypeId(), request.getBranchId());
        String voucherNumber = (request.getVoucherNumber() != null && !request.getVoucherNumber().isBlank())
                ? request.getVoucherNumber()
                : (voucherTypeId != null ? voucherNumberService.generateVoucherNumber(voucherTypeId, request.getBranchId()) : null);
        try {
            PurchaseVoucherHeader pv = PurchaseVoucherHeader.builder()
                    .voucherTypeId(voucherTypeId)
                    .branchId(request.getBranchId())
                    .fromBranchId(request.getFromBranchId())
                    .fromEntityType(request.getFromEntityType())
                    .supplierFromId(request.getSupplierFromId())
                    .supplierToId(request.getSupplierToId())
                    .consigneeType(request.getConsigneeType())
                    .consigneeId(request.getConsigneeId())
                    .consigneeEntityType(request.getConsigneeEntityType())
                    .isThirdParty(request.isThirdParty())
                    .voucherCategory(request.getVoucherCategory())
                    .pvDate(request.getPvDate())
                    .effectiveDate(request.getEffectiveDate())
                    .supplierInvoiceId(request.getSupplierInvoiceId())
                    .supplierInvoiceNo(request.getSupplierInvoiceNo())
                    .supplierInvoiceDate(request.getSupplierInvoiceDate())
                    .modeOfPayment(request.getModeOfPayment())
                    .otherReferences(request.getOtherReferences())
                    .termsOfDelivery(request.getTermsOfDelivery())
                    .dispatchThrough(request.getDispatchThrough())
                    .destination(request.getDestination())
                    .carrierNameAgent(request.getCarrierNameAgent())
                    .billOfLadingNo(request.getBillOfLadingNo())
                    .motorVehicleNo(request.getMotorVehicleNo())
                    .dispatchDocNo(request.getDispatchDocNo())
                    .trackingNo(request.getTrackingNo())
                    .deliveryAddress(request.getDeliveryAddress())
                    .dueDate(request.getDueDate())
                    .processDescription(request.getProcessDescription())
                    .sourceGodownId(request.getSourceGodownId())
                    .destinationGodownId(request.getDestinationGodownId())
                    .transferReason(request.getTransferReason())
                    .referenceNo(request.getReferenceNo())
                    .purposeOfIssue(request.getPurposeOfIssue())
                    .narration(request.getNarration())
                    .challanNo(request.getChallanNo())
                    .challanDate(request.getChallanDate())
                    .linkedPoId(request.getLinkedPoId())
                    .voucherNumber(voucherNumber)
                    .createdBy(request.getCreatedBy() != null ? String.valueOf(request.getCreatedBy()) : null)
                    .grossAmount(request.getGrossAmount())
                    .discountAmount(request.getDiscountAmount())
                    .gstAmount(request.getGstAmount())
                    .cessAmount(request.getCessAmount())
                    .netAmount(request.getNetAmount())
                    .roundOffAmount(request.getRoundOffAmount())
                    .poAmountBreakPoint(request.getPoAmountBreakPoint())
                    .deptId(request.getFromDepartmentId())
                    .status(STATUS_POSTED)
                    .approvedBy(request.getCreatedBy() != null ? String.valueOf(request.getCreatedBy()) : null)
                    .approvedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")))
                    .build();

            pv = pvHeaderRepository.save(pv);
            Long pvId = pv.getPvId();

            if (request.getDetails() != null) {
                for (PurchaseVoucherDetailRequest dr : request.getDetails()) {
                    PurchaseVoucherDetail detail = PurchaseVoucherDetail.builder()
                            .pvId(pvId)
                            .itemId(dr.getItemId())
                            .unitId(dr.getUnitId())
                            .locationId(dr.getLocationId())
                            .qty(dr.getQty())
                            .rate(dr.getRate())
                            .grossAmount(dr.getGrossAmount())
                            .discountPerc(dr.getDiscountPerc())
                            .discountAmount(dr.getDiscountAmount())
                            .gstPerc(dr.getGstPerc())
                            .gstAmount(dr.getGstAmount())
                            .cessPerc(dr.getCessPerc())
                            .cessAmount(dr.getCessAmount())
                            .netAmount(dr.getNetAmount())
                            .orderedQty(dr.getQty())
                            .lineNarration(dr.getLineNarration())
                            .build();
                    pv.addDetail(detail);
                }
            }

            if (request.getDetailsTo() != null) {
                for (PurchaseVoucherDetailRequest dr : request.getDetailsTo()) {
                    PurchaseVoucherDetailTo detail = PurchaseVoucherDetailTo.builder()
                            .pvId(pvId)
                            .itemId(dr.getItemId())
                            .unitId(dr.getUnitId())
                            .locationId(dr.getLocationId())
                            .qty(dr.getQty())
                            .rate(dr.getRate())
                            .grossAmount(dr.getGrossAmount())
                            .discountPerc(dr.getDiscountPerc())
                            .discountAmount(dr.getDiscountAmount())
                            .gstPerc(dr.getGstPerc())
                            .gstAmount(dr.getGstAmount())
                            .cessPerc(dr.getCessPerc())
                            .cessAmount(dr.getCessAmount())
                            .netAmount(dr.getNetAmount())
                            .orderedQty(dr.getQty())
                            .lineNarration(dr.getLineNarration())
                            .build();
                    pv.addDetailTo(detail);
                }
            }

            pv = pvHeaderRepository.save(pv);
            decreaseLinkedPoQty(pv);
            if (preClose && pv.getLinkedPoId() != null && !pv.getLinkedPoId().isBlank()) {
                preClosePo(pv.getLinkedPoId());
            }
            editLogService.logCreate(ENTITY_TYPE, pvId, voucherNumber, voucherTypeId,
                    request.getCreatedBy() != null ? String.valueOf(request.getCreatedBy()) : null,
                    "Manual Purchase Voucher created");

            SupplierGodownMap savedGodown = upsertSupplierGodown(request.getSupGodown(), request.getSupplierFromId());
            applyGodownIdToPv(pv, savedGodown);
            pv.setSupGodown(savedGodown);
            pvStockService.applyStock(pv, false);
            if (savedGodown != null) {
                godownItemStockService.applyGodownStock(
                    savedGodown.getId(), pv.getVoucherCategory(), pv.getDetails(), false);
            }
            log.info("Purchase Voucher {} created manually", pvId);
            return pv;
        } catch (RuntimeException e) {
            voucherNumberService.returnVoucherNumber(voucherTypeId, voucherNumber, request.getBranchId());
            throw e;
        }
    }

    /**
     * Submit for approval: DRAFT → PENDING_APPROVAL
     */
    @Transactional
    public PurchaseVoucherHeader submitForApproval(Long pvId, String submittedBy) {
        PurchaseVoucherHeader pv = pvHeaderRepository.findById(pvId)
                .orElseThrow(() -> new MmsException("Purchase Voucher not found: " + pvId));

        if (!STATUS_DRAFT.equals(pv.getStatus())) {
            throw new MmsException("Purchase Voucher is not in DRAFT status");
        }

        String oldStatus = pv.getStatus();
        pv.setStatus(STATUS_PENDING_APPROVAL);
        pv = pvHeaderRepository.save(pv);

        editLogService.logStatusChange(ENTITY_TYPE, pvId, pv.getVoucherNumber(), pv.getVoucherTypeId(),
                oldStatus, STATUS_PENDING_APPROVAL, submittedBy);

        return pv;
    }

    /**
     * Approve and post: PENDING_APPROVAL → POSTED
     */
    @Transactional
    public PurchaseVoucherHeader approveAndPost(Long pvId, String approvedBy) {
        PurchaseVoucherHeader pv = pvHeaderRepository.findById(pvId)
                .orElseThrow(() -> new MmsException("Purchase Voucher not found: " + pvId));

        if (!STATUS_PENDING_APPROVAL.equals(pv.getStatus())) {
            throw new MmsException("Purchase Voucher is not pending approval");
        }

        String oldStatus = pv.getStatus();
        pv.setStatus(STATUS_POSTED);
        pv.setApprovedBy(approvedBy);
        pv.setApprovedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        pv = pvHeaderRepository.save(pv);

        editLogService.logApprove(ENTITY_TYPE, pvId, pv.getVoucherNumber(), pv.getVoucherTypeId(), approvedBy);
        editLogService.logStatusChange(ENTITY_TYPE, pvId, pv.getVoucherNumber(), pv.getVoucherTypeId(),
                oldStatus, STATUS_POSTED, approvedBy);

        log.info("Purchase Voucher {} posted by {}", pvId, approvedBy);
        return pv;
    }

    @Transactional(readOnly = true)
    public PurchaseVoucherHeader getById(Long pvId) {
        return pvHeaderRepository.findById(pvId)
                .orElseThrow(() -> new MmsException("Purchase Voucher not found: " + pvId));
    }

    @Transactional(readOnly = true)
    public Page<PurchaseVoucherHeader> list(String branchId, String supplierFromId, String status,
                                            String voucherCategory, String voucherTypeId,
                                            int page, int size) {
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "pvId"));
        return pvHeaderRepository.findByFilters(branchId, supplierFromId, status, voucherCategory,
                voucherTypeId, pageable);
    }

    @Transactional(readOnly = true)
    public List<PurchaseVoucherRegisterRow> listRegister(String branchId, String voucherTypeId,
                                                         LocalDate fromDate, LocalDate toDate,
                                                         int page, int size) {
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "pvId"));
        Page<PurchaseVoucherHeader> pvPage = pvHeaderRepository.findForRegister(
                branchId, voucherTypeId, fromDate, toDate, pageable);

        List<PurchaseVoucherRegisterRow> rows = new ArrayList<>();
        for (PurchaseVoucherHeader h : pvPage.getContent()) {
            String supplierName = h.getSupplier() != null ? h.getSupplier().getSuppName() : h.getSupplierFromId();
            String vtName = h.getVoucherType() != null ? h.getVoucherType().getVoucherTypeName() : h.getVoucherTypeId();
            for (PurchaseVoucherDetail d : h.getDetails()) {
                String itemDesc = d.getItem() != null ? d.getItem().getItemDesc() : d.getItemId();
                rows.add(PurchaseVoucherRegisterRow.builder()
                        .pvId(h.getPvId())
                        .date(h.getPvDate())
                        .itemId(d.getItemId())
                        .particulars(itemDesc)
                        .suppId(h.getSupplierFromId())
                        .supplier(supplierName)
                        .voucherTypeId(h.getVoucherTypeId())
                        .voucherType(vtName)
                        .voucherNo(h.getVoucherNumber())
                        .qty(d.getQty())
                        .unitId(d.getUnitId())
                        .rate(d.getRate())
                        .value(d.getGrossAmount())
                        .grossTotal(h.getGrossAmount())
                        .voucherCategory(h.getVoucherCategory())
                        .status(h.getStatus())
                        .fromBranchId(h.getFromBranchId())
                        .fromBranchName(h.getFromBranch() != null ? h.getFromBranch().getBranchName() : null)
                        .consigneeId(h.getConsigneeId())
                        .consigneeType(h.getConsigneeType())
                        .consigneeEntityType(h.getConsigneeEntityType())
                        .fromEntityType(h.getFromEntityType())
                        .build());
            }
        }
        return rows;
    }

    @Transactional
    public PurchaseVoucherHeader update(Long pvId, PurchaseVoucherCreateRequest request) {
        PurchaseVoucherHeader pv = pvHeaderRepository.findById(pvId)
                .orElseThrow(() -> new MmsException("Purchase Voucher not found: " + pvId));

        // Reverse existing stock entries before applying new ones
        SupplierGodownMap existingGodown = pv.getSourceGodownId() != null
                ? supplierGodownMapRepository.findById(pv.getSourceGodownId()).orElse(null) : null;
        pvStockService.applyStock(pv, true);
        if (existingGodown != null) {
            godownItemStockService.applyGodownStock(
                existingGodown.getId(), pv.getVoucherCategory(), pv.getDetails(), true);
        }

        pv.setVoucherTypeId(request.getVoucherTypeId());
        pv.setBranchId(request.getBranchId());
        pv.setSupplierFromId(request.getSupplierFromId());
        pv.setSupplierToId(request.getSupplierToId());
        pv.setFromBranchId(request.getFromBranchId());
        pv.setFromEntityType(request.getFromEntityType());
        pv.setConsigneeType(request.getConsigneeType());
        pv.setConsigneeId(request.getConsigneeId());
        pv.setConsigneeEntityType(request.getConsigneeEntityType());
        pv.setThirdParty(request.isThirdParty());
        pv.setVoucherCategory(request.getVoucherCategory());
        pv.setPvDate(request.getPvDate());
        pv.setEffectiveDate(request.getEffectiveDate());
        pv.setSupplierInvoiceId(request.getSupplierInvoiceId());
        pv.setSupplierInvoiceNo(request.getSupplierInvoiceNo());
        pv.setSupplierInvoiceDate(request.getSupplierInvoiceDate());
        pv.setChallanNo(request.getChallanNo());
        pv.setChallanDate(request.getChallanDate());
        pv.setLinkedPoId(request.getLinkedPoId());
        pv.setModeOfPayment(request.getModeOfPayment());
        pv.setOtherReferences(request.getOtherReferences());
        pv.setTermsOfDelivery(request.getTermsOfDelivery());
        pv.setDispatchThrough(request.getDispatchThrough());
        pv.setDestination(request.getDestination());
        pv.setCarrierNameAgent(request.getCarrierNameAgent());
        pv.setBillOfLadingNo(request.getBillOfLadingNo());
        pv.setMotorVehicleNo(request.getMotorVehicleNo());
        pv.setDispatchDocNo(request.getDispatchDocNo());
        pv.setTrackingNo(request.getTrackingNo());
        pv.setDeliveryAddress(request.getDeliveryAddress());
        pv.setDueDate(request.getDueDate());
        pv.setProcessDescription(request.getProcessDescription());
        pv.setSourceGodownId(request.getSourceGodownId());
        pv.setDestinationGodownId(request.getDestinationGodownId());
        pv.setTransferReason(request.getTransferReason());
        pv.setReferenceNo(request.getReferenceNo());
        pv.setPurposeOfIssue(request.getPurposeOfIssue());
        pv.setNarration(request.getNarration());
        pv.setGrossAmount(request.getGrossAmount());
        pv.setDiscountAmount(request.getDiscountAmount());
        pv.setGstAmount(request.getGstAmount());
        pv.setCessAmount(request.getCessAmount());
        pv.setNetAmount(request.getNetAmount());
        pv.setRoundOffAmount(request.getRoundOffAmount());
        pv.setPoAmountBreakPoint(request.getPoAmountBreakPoint());
        pv.setDeptId(request.getFromDepartmentId());

        // Replace details
        pv.getDetails().clear();
        if (request.getDetails() != null) {
            for (PurchaseVoucherDetailRequest dr : request.getDetails()) {
                PurchaseVoucherDetail detail = PurchaseVoucherDetail.builder()
                        .pvId(pv.getPvId())
                        .itemId(dr.getItemId())
                        .unitId(dr.getUnitId())
                        .locationId(dr.getLocationId())
                        .qty(dr.getQty())
                        .rate(dr.getRate())
                        .grossAmount(dr.getGrossAmount())
                        .discountPerc(dr.getDiscountPerc())
                        .discountAmount(dr.getDiscountAmount())
                        .gstPerc(dr.getGstPerc())
                        .gstAmount(dr.getGstAmount())
                        .cessPerc(dr.getCessPerc())
                        .cessAmount(dr.getCessAmount())
                        .netAmount(dr.getNetAmount())
                        .orderedQty(dr.getOrderedQty())
                        .lineNarration(dr.getLineNarration())
                        .build();
                pv.addDetail(detail);
            }
        }

        // Replace detailsTo
        pv.getDetailsTo().clear();
        if (request.getDetailsTo() != null) {
            for (PurchaseVoucherDetailRequest dr : request.getDetailsTo()) {
                PurchaseVoucherDetailTo detail = PurchaseVoucherDetailTo.builder()
                        .pvId(pv.getPvId())
                        .itemId(dr.getItemId())
                        .unitId(dr.getUnitId())
                        .locationId(dr.getLocationId())
                        .qty(dr.getQty())
                        .rate(dr.getRate())
                        .grossAmount(dr.getGrossAmount())
                        .discountPerc(dr.getDiscountPerc())
                        .discountAmount(dr.getDiscountAmount())
                        .gstPerc(dr.getGstPerc())
                        .gstAmount(dr.getGstAmount())
                        .cessPerc(dr.getCessPerc())
                        .cessAmount(dr.getCessAmount())
                        .netAmount(dr.getNetAmount())
                        .orderedQty(dr.getOrderedQty())
                        .lineNarration(dr.getLineNarration())
                        .build();
                pv.addDetailTo(detail);
            }
        }

        pv = pvHeaderRepository.save(pv);
        editLogService.logCreate(ENTITY_TYPE, pv.getPvId(), pv.getVoucherNumber(), pv.getVoucherTypeId(),
                request.getCreatedBy() != null ? String.valueOf(request.getCreatedBy()) : null,
                "Purchase Voucher updated");

        SupplierGodownMap savedGodown = upsertSupplierGodown(request.getSupGodown(), request.getSupplierFromId());
        applyGodownIdToPv(pv, savedGodown);
        pv.setSupGodown(savedGodown);
        pvStockService.applyStock(pv, false);
        if (savedGodown != null) {
            godownItemStockService.applyGodownStock(
                savedGodown.getId(), pv.getVoucherCategory(), pv.getDetails(), false);
        }
        log.info("Purchase Voucher {} updated", pvId);
        return pv;
    }

    private void decreaseLinkedPoQty(PurchaseVoucherHeader pv) {
        if (pv.getLinkedPoId() == null || pv.getLinkedPoId().isBlank()) {
            return;
        }

        Long linkedPvId;
        try {
            linkedPvId = Long.parseLong(pv.getLinkedPoId().trim());
        } catch (NumberFormatException e) {
            return;
        }

        PurchaseVoucherHeader linkedPv = pvHeaderRepository.findById(linkedPvId).orElse(null);
        if (linkedPv == null) {
            return;
        }

        Map<String, PurchaseVoucherDetail> linkedDetailByItem = linkedPv.getDetails().stream()
                .collect(Collectors.toMap(PurchaseVoucherDetail::getItemId, d -> d));

        boolean anyReceived = false;
        for (PurchaseVoucherDetail pvd : pv.getDetails()) {
            PurchaseVoucherDetail linked = linkedDetailByItem.get(pvd.getItemId());
            if (linked == null || pvd.getQty() == null) {
                continue;
            }
            BigDecimal remaining = linked.getQty() != null ? linked.getQty() : BigDecimal.ZERO;
            linked.setQty(remaining.subtract(pvd.getQty()));
            if (pvd.getQty().compareTo(BigDecimal.ZERO) > 0) {
                anyReceived = true;
            }
        }

        boolean allReceived = true;
        for (PurchaseVoucherDetail d : linkedPv.getDetails()) {
            BigDecimal remaining = d.getQty() != null ? d.getQty() : BigDecimal.ZERO;
            if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                allReceived = false;
            }
        }
        if (allReceived && anyReceived) {
            linkedPv.setStatus("COMPLETED");
        } else if (anyReceived) {
            linkedPv.setStatus("PARTIAL");
        }

        pvHeaderRepository.save(linkedPv);
    }

    private void preClosePo(String linkedPoId) {
        Long poId;
        try {
            poId = Long.parseLong(linkedPoId.trim());
        } catch (NumberFormatException e) {
            return;
        }
        PurchaseVoucherHeader po = pvHeaderRepository.findById(poId).orElse(null);
        if (po == null || !Set.of(STATUS_POSTED, "PARTIAL").contains(po.getStatus())) {
            return;
        }
        for (PurchaseVoucherDetail detail : po.getDetails()) {
            BigDecimal remaining = detail.getQty() != null ? detail.getQty() : BigDecimal.ZERO;
            detail.setPreCloseQty(remaining);
            detail.setQty(BigDecimal.ZERO);
        }
        po.setStatus(STATUS_PRE_CLOSED);
        pvHeaderRepository.save(po);
    }

    /**
     * Resolve or create a third-party supplier.
     * If suppId provided and already exists → reuse it.
     * Otherwise auto-generate suppId and create a new supplier with type=THIRD_PARTY.
     */
    private String resolveThirdPartySupplier(ThirdPartySupplierRequest tp) {
        if (tp.getSuppId() != null && !tp.getSuppId().isBlank()) {
            if (supplierMasterRepository.existsById(tp.getSuppId())) {
                return tp.getSuppId();
            }
        }
        String suppId = tp.getSuppId() != null && !tp.getSuppId().isBlank()
                ? tp.getSuppId()
                : "TP-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase(java.util.Locale.ROOT);

        SupplierMaster supplier = SupplierMaster.builder()
                .suppId(suppId)
                .suppName(tp.getSuppName())
                .mobNo(tp.getMobNo())
                .email(tp.getEmail())
                .gstin(tp.getGstin())
                .address(tp.getAddress1())
                .type("THIRD_PARTY")
                .build();
        supplierMasterRepository.save(supplier);
        log.info("Created third-party supplier: {}", suppId);
        return suppId;
    }

    private String resolveVoucherTypeId(String requestedTypeId, String branchId) {
        if (requestedTypeId != null && !requestedTypeId.isBlank()) {
            return requestedTypeId;
        }
        // Find first active PURCHASE_VOUCHER type
        return voucherTypeRepository.findByVoucherCategoryAndActiveTrue("PURCHASE_VOUCHER")
                .stream().findFirst()
                .map(vt -> vt.getVoucherTypeId())
                .orElse(null);
    }

    private void applyGodownIdToPv(PurchaseVoucherHeader pv, SupplierGodownMap savedGodown) {
        if (savedGodown == null) {
            return;
        }
        Set<String> outCategories = Set.of("Delivery Note", "Material Out", "Rejections Out");
        if (outCategories.contains(pv.getVoucherCategory())) {
            pv.setDestinationGodownId(savedGodown.getId());
        } else {
            pv.setSourceGodownId(savedGodown.getId());
        }
        pvHeaderRepository.save(pv);
    }

    /**
     * Creates or updates a supplier godown mapping.
     * - suppId defaults to fallbackSuppId when not provided in the request.
     * - godownId null  → create a new record; the auto-generated PK (id) is the godown identifier.
     * - godownId given → find existing record by id and update name/item.
     */
    private SupplierGodownMap upsertSupplierGodown(SupplierGodownRequest sg, String fallbackSuppId) {
        if (sg == null) {
            return null;
        }
        String suppId = sg.getSuppId() != null ? sg.getSuppId() : fallbackSuppId;
        if (suppId == null) {
            return null;
        }

        if (sg.getId() == null) {
            SupplierGodownMap map = SupplierGodownMap.builder()
                    .suppId(suppId)
                    .itemId(sg.getItemId())
                    .godownName(sg.getGodownName())
                    .build();
            map = supplierGodownMapRepository.save(map);
            log.debug("Created new godown: suppId={}, id={}", suppId, map.getId());
            return map;
        } else {
            SupplierGodownMap map = supplierGodownMapRepository.findById(sg.getId())
                    .orElseGet(() -> SupplierGodownMap.builder()
                            .suppId(suppId)
                            .build());
            map.setItemId(sg.getItemId());
            map.setGodownName(sg.getGodownName());
            map = supplierGodownMapRepository.save(map);
            log.debug("Updated godown: id={}", map.getId());
            return map;
        }
    }

}
