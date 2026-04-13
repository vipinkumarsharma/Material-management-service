package com.countrydelight.mms.dto.purchase;

import com.countrydelight.mms.entity.master.SupplierGodownMap;
import com.countrydelight.mms.entity.purchase.PurchaseVoucherDetail;
import com.countrydelight.mms.entity.purchase.PurchaseVoucherDetailTo;
import com.countrydelight.mms.entity.purchase.PurchaseVoucherHeader;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PurchaseVoucherResponse {

    private Long pvId;
    private String voucherNumber;
    private String voucherTypeId;
    private String branchId;
    private String fromBranchId;
    private String fromBranchName;
    private String supplierFromId;
    private String supplierToId;
    private String consigneeType;
    private String consigneeId;
    private String consigneeEntityType;
    private String fromEntityType;
    private boolean isThirdParty;
    private String voucherCategory;
    private LocalDate pvDate;
    private LocalDate effectiveDate;
    private Long supplierInvoiceId;
    private String supplierInvoiceNo;
    private LocalDate supplierInvoiceDate;
    private String challanNo;
    private LocalDate challanDate;
    private String linkedPoId;
    private String status;
    private boolean optional;
    private BigDecimal grossAmount;
    private BigDecimal discountAmount;
    private BigDecimal gstAmount;
    private BigDecimal cessAmount;
    private BigDecimal netAmount;
    private BigDecimal roundOffAmount;
    private BigDecimal poAmountBreakPoint;
    private String modeOfPayment;
    private String otherReferences;
    private String termsOfDelivery;
    private String dispatchThrough;
    private String destination;
    private String carrierNameAgent;
    private String billOfLadingNo;
    private String motorVehicleNo;
    private String dispatchDocNo;
    private String trackingNo;
    private String deliveryAddress;
    private LocalDate dueDate;
    private String processDescription;
    private Long sourceGodownId;
    private Long destinationGodownId;
    private String transferReason;
    private String referenceNo;
    private String purposeOfIssue;
    private String narration;
    private String createdBy;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String fromDepartmentId;
    private List<PurchaseVoucherDetail> details;
    private List<PurchaseVoucherDetailTo> detailsTo;
    private SupplierGodownMap supGodown;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PurchaseVoucherResponse from(PurchaseVoucherHeader e) {
        return PurchaseVoucherResponse.builder()
                .pvId(e.getPvId())
                .voucherNumber(e.getVoucherNumber())
                .voucherTypeId(e.getVoucherTypeId())
                .branchId(e.getBranchId())
                .fromBranchId(e.getFromBranchId())
                .fromBranchName(e.getFromBranch() != null ? e.getFromBranch().getBranchName() : null)
                .supplierFromId(e.getSupplierFromId())
                .supplierToId(e.getSupplierToId())
                .consigneeType(e.getConsigneeType())
                .consigneeId(e.getConsigneeId())
                .consigneeEntityType(e.getConsigneeEntityType())
                .fromEntityType(e.getFromEntityType())
                .isThirdParty(e.isThirdParty())
                .voucherCategory(e.getVoucherCategory())
                .pvDate(e.getPvDate())
                .effectiveDate(e.getEffectiveDate())
                .supplierInvoiceId(e.getSupplierInvoiceId())
                .supplierInvoiceNo(e.getSupplierInvoiceNo())
                .supplierInvoiceDate(e.getSupplierInvoiceDate())
                .challanNo(e.getChallanNo())
                .challanDate(e.getChallanDate())
                .linkedPoId(e.getLinkedPoId())
                .status(e.getStatus())
                .optional(e.isOptional())
                .grossAmount(e.getGrossAmount())
                .discountAmount(e.getDiscountAmount())
                .gstAmount(e.getGstAmount())
                .cessAmount(e.getCessAmount())
                .netAmount(e.getNetAmount())
                .roundOffAmount(e.getRoundOffAmount())
                .poAmountBreakPoint(e.getPoAmountBreakPoint())
                .modeOfPayment(e.getModeOfPayment())
                .otherReferences(e.getOtherReferences())
                .termsOfDelivery(e.getTermsOfDelivery())
                .dispatchThrough(e.getDispatchThrough())
                .destination(e.getDestination())
                .carrierNameAgent(e.getCarrierNameAgent())
                .billOfLadingNo(e.getBillOfLadingNo())
                .motorVehicleNo(e.getMotorVehicleNo())
                .dispatchDocNo(e.getDispatchDocNo())
                .trackingNo(e.getTrackingNo())
                .deliveryAddress(e.getDeliveryAddress())
                .dueDate(e.getDueDate())
                .processDescription(e.getProcessDescription())
                .sourceGodownId(e.getSourceGodownId())
                .destinationGodownId(e.getDestinationGodownId())
                .transferReason(e.getTransferReason())
                .referenceNo(e.getReferenceNo())
                .purposeOfIssue(e.getPurposeOfIssue())
                .narration(e.getNarration())
                .createdBy(e.getCreatedBy())
                .approvedBy(e.getApprovedBy())
                .approvedAt(e.getApprovedAt())
                .fromDepartmentId(e.getDeptId())
                .details(e.getDetails().stream().peek(d -> {
                    if (d.getItem() != null) {
                        d.setItemDesc(d.getItem().getItemDesc());
                    }
                }).toList())
                .detailsTo(e.getDetailsTo())
                .supGodown(e.getSupGodown())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
