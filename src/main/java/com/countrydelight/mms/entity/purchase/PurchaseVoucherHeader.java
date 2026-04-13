package com.countrydelight.mms.entity.purchase;

import com.countrydelight.mms.entity.master.BranchMaster;
import com.countrydelight.mms.entity.master.SupplierGodownMap;
import com.countrydelight.mms.entity.master.SupplierMaster;
import com.countrydelight.mms.entity.master.VoucherTypeMaster;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_voucher_header")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseVoucherHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pv_id")
    private Long pvId;

    @Column(name = "voucher_number", length = 50)
    private String voucherNumber;

    @Column(name = "voucher_type_id", length = 20)
    private String voucherTypeId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_type_id", insertable = false, updatable = false)
    private VoucherTypeMaster voucherType;

    @Column(name = "branch_id", nullable = false, length = 20)
    private String branchId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", insertable = false, updatable = false)
    private BranchMaster branch;

    @Column(name = "from_branch_id", length = 20)
    private String fromBranchId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_branch_id", insertable = false, updatable = false)
    private BranchMaster fromBranch;

    @Column(name = "from_entity_type", length = 20)
    private String fromEntityType;

    @Column(name = "supp_id", length = 20)
    private String supplierFromId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supp_id", insertable = false, updatable = false)
    private SupplierMaster supplier;

    @Column(name = "supplier_to_id", length = 20)
    private String supplierToId;

    @Column(name = "consignee_type", length = 20)
    private String consigneeType;

    @Column(name = "consignee_id", length = 20)
    private String consigneeId;

    @Column(name = "consignee_entity_type", length = 20)
    private String consigneeEntityType;

    @Column(name = "is_third_party")
    @Builder.Default
    private boolean isThirdParty = false;

    @Column(name = "voucher_category", length = 50)
    private String voucherCategory;

    @Column(name = "pv_date", nullable = false)
    private LocalDate pvDate;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "invoice_id")
    private Long supplierInvoiceId;

    @Column(name = "supplier_invoice_no", length = 100)
    private String supplierInvoiceNo;

    @Column(name = "supplier_invoice_date")
    private LocalDate supplierInvoiceDate;

    @Column(name = "challan_no", length = 100)
    private String challanNo;

    @Column(name = "challan_date")
    private LocalDate challanDate;

    @Column(name = "linked_po_id", length = 100)
    private String linkedPoId;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "DRAFT";

    @Column(name = "is_optional")
    @Builder.Default
    private boolean optional = false;

    @Column(name = "gross_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal grossAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "gst_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal gstAmount = BigDecimal.ZERO;

    @Column(name = "cess_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal cessAmount = BigDecimal.ZERO;

    @Column(name = "net_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal netAmount = BigDecimal.ZERO;

    @Column(name = "round_off_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal roundOffAmount = BigDecimal.ZERO;

    @Column(name = "po_amount_break_point", precision = 15, scale = 2)
    private BigDecimal poAmountBreakPoint;

    @Column(name = "mode_of_payment", length = 100)
    private String modeOfPayment;

    @Column(name = "other_references", length = 255)
    private String otherReferences;

    @Column(name = "terms_of_delivery", length = 255)
    private String termsOfDelivery;

    @Column(name = "dispatch_through", length = 255)
    private String dispatchThrough;

    @Column(name = "destination", length = 255)
    private String destination;

    @Column(name = "carrier_name_agent", length = 255)
    private String carrierNameAgent;

    @Column(name = "bill_of_lading_no", length = 100)
    private String billOfLadingNo;

    @Column(name = "motor_vehicle_no", length = 100)
    private String motorVehicleNo;

    @Column(name = "dispatch_doc_no", length = 100)
    private String dispatchDocNo;

    @Column(name = "tracking_no", length = 100)
    private String trackingNo;

    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "process_description", columnDefinition = "TEXT")
    private String processDescription;

    @Column(name = "source_godown_id")
    private Long sourceGodownId;

    @Column(name = "destination_godown_id")
    private Long destinationGodownId;

    @Column(name = "transfer_reason", columnDefinition = "TEXT")
    private String transferReason;

    @Column(name = "reference_no", length = 100)
    private String referenceNo;

    @Column(name = "purpose_of_issue", columnDefinition = "TEXT")
    private String purposeOfIssue;

    @Column(name = "narration", columnDefinition = "TEXT")
    private String narration;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "dept_id", length = 20)
    private String deptId;

    @OneToMany(mappedBy = "header", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PurchaseVoucherDetail> details = new ArrayList<>();

    @OneToMany(mappedBy = "header", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PurchaseVoucherDetailTo> detailsTo = new ArrayList<>();

    /** Not persisted — populated by service after godown upsert */
    @Transient
    private SupplierGodownMap supGodown;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public void addDetail(PurchaseVoucherDetail detail) {
        details.add(detail);
        detail.setHeader(this);
    }

    public void addDetailTo(PurchaseVoucherDetailTo detail) {
        detailsTo.add(detail);
        detail.setHeader(this);
    }
}
