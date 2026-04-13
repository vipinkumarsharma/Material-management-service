package com.countrydelight.mms.entity.inward;

import com.countrydelight.mms.entity.master.BranchMaster;
import com.countrydelight.mms.entity.master.DepartmentMaster;
import com.countrydelight.mms.entity.master.SupplierMaster;
import com.countrydelight.mms.entity.purchase.SupplierInvoice;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@Table(name = "grn_header")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class GrnHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grn_id")
    private Long grnId;

    @Column(name = "voucher_number", length = 50)
    private String voucherNumber;

    @Column(name = "voucher_type_id", length = 20)
    private String voucherTypeId;

    @Column(name = "branch_id", nullable = false, length = 20)
    private String branchId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", insertable = false, updatable = false)
    private BranchMaster branch;

    @Column(name = "dept_id")
    private Integer deptId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id", insertable = false, updatable = false)
    private DepartmentMaster department;

    @Column(name = "supp_id", nullable = false, length = 20)
    private String suppId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supp_id", insertable = false, updatable = false)
    private SupplierMaster supplier;

    @Column(name = "pv_id")
    private Long pvId;

    @Column(name = "invoice_id")
    private Long invoiceId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", insertable = false, updatable = false)
    private SupplierInvoice invoice;

    @Column(name = "challan_no", length = 50)
    private String challanNo;

    @Column(name = "challan_date")
    private LocalDate challanDate;

    @Column(name = "invoice_date")
    private LocalDate invoiceDate;

    @Column(name = "grn_date", nullable = false)
    private LocalDate grnDate;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "DRAFT"; // DRAFT / PENDING_APPROVAL / POSTED

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "approved_by", length = 50)
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "items_count", precision = 15, scale = 4)
    private BigDecimal itemsCount;

    @Column(name = "total_qty", precision = 15, scale = 4)
    private BigDecimal totalQty;

    @Column(name = "gross_amount", precision = 15, scale = 4)
    private BigDecimal grossAmount;

    @Column(name = "net_amount", precision = 15, scale = 4)
    private BigDecimal netAmount;

    @Column(name = "round_off_amount", precision = 15, scale = 4)
    private BigDecimal roundOffAmount;

    @OneToMany(mappedBy = "grnHeader", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GrnDetail> details = new ArrayList<>();

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public void addDetail(GrnDetail detail) {
        details.add(detail);
        detail.setGrnHeader(this);
    }
}
