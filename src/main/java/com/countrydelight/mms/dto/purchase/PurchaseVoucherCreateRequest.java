package com.countrydelight.mms.dto.purchase;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class PurchaseVoucherCreateRequest {

    private String voucherTypeId;

    @NotBlank
    private String branchId;

    private String fromBranchId;

    @JsonAlias("suppId")
    private String supplierFromId;

    private String supplierToId;

    private String consigneeType;

    private String consigneeId;

    private String consigneeEntityType;

    private String fromEntityType;

    private boolean isThirdParty = false;

    private String voucherCategory;

    @NotNull
    private LocalDate pvDate;

    private LocalDate effectiveDate;
    @JsonAlias("invoiceId")
    private Long supplierInvoiceId;
    private String supplierInvoiceNo;
    private LocalDate supplierInvoiceDate;
    private String challanNo;
    private LocalDate challanDate;
    private String linkedPoId;
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
    private Long createdBy;

    private BigDecimal grossAmount = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal gstAmount = BigDecimal.ZERO;
    private BigDecimal cessAmount = BigDecimal.ZERO;
    private BigDecimal netAmount = BigDecimal.ZERO;
    private BigDecimal roundOffAmount = BigDecimal.ZERO;
    private BigDecimal poAmountBreakPoint;

    private String voucherNumber;   // if provided, skip auto-generation

    private ThirdPartySupplierRequest thirdPartySupplier;  // required when isThirdParty = true

    private SupplierGodownRequest supGodown;  // if present, upserts supplier-godown mapping

    private String fromDepartmentId;

    private List<PurchaseVoucherDetailRequest> details;

    private List<PurchaseVoucherDetailRequest> detailsTo;
}
