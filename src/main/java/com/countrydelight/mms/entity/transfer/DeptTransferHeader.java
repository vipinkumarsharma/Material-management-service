package com.countrydelight.mms.entity.transfer;

import com.countrydelight.mms.entity.master.BranchMaster;
import com.countrydelight.mms.entity.master.DepartmentMaster;
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
@Table(name = "dept_transfer_header")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeptTransferHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dept_transfer_id")
    private Long deptTransferId;

    @Column(name = "voucher_number", length = 50)
    private String voucherNumber;

    @Column(name = "voucher_type_id", length = 20)
    private String voucherTypeId;

    @Column(name = "from_branch_id", nullable = false, length = 20)
    private String fromBranchId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_branch_id", insertable = false, updatable = false)
    private BranchMaster fromBranch;

    @Column(name = "to_branch_id", nullable = false, length = 20)
    private String toBranchId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_branch_id", insertable = false, updatable = false)
    private BranchMaster toBranch;

    @Column(name = "from_dept_id", nullable = false)
    private Integer fromDeptId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_dept_id", insertable = false, updatable = false)
    private DepartmentMaster fromDepartment;

    @Column(name = "to_dept_id", nullable = false)
    private Integer toDeptId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_dept_id", insertable = false, updatable = false)
    private DepartmentMaster toDepartment;

    @Column(name = "transfer_date", nullable = false)
    private LocalDate transferDate;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "POSTED";

    @Column(name = "transfer_category", length = 50)
    private String transferCategory;

    @Column(name = "transfer_type", length = 20)
    private String transferType;

    // Null for OUT transfers; references dept_transfer_id of the corresponding OUT record for IN transfers
    @Column(name = "transfer_out_id")
    private Long transferOutId;

    // True once a transfer-in has been created against this transfer-out; always false on IN records
    @Column(name = "is_received", nullable = false)
    @Builder.Default
    private boolean received = false;

    @Column(name = "transfer_mode", length = 50)
    private String transferMode;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "items_count", precision = 15, scale = 4)
    private BigDecimal itemsCount;

    @Column(name = "total_qty", precision = 15, scale = 4)
    private BigDecimal totalQty;

    @Column(name = "gross_amount", precision = 15, scale = 4)
    private BigDecimal grossAmount;

    @Column(name = "net_amount", precision = 15, scale = 4)
    private BigDecimal netAmount;

    @Column(name = "round_off_amount", precision = 15, scale = 4)
    private BigDecimal roundingAmount;

    @Column(name = "rounding_type", length = 20)
    private String roundingType;

    @Column(name = "third_party_supplier_id", length = 20)
    private String thirdPartySupplierId;

    @OneToMany(mappedBy = "header", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DeptTransferDetail> details = new ArrayList<>();

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public void addDetail(DeptTransferDetail detail) {
        details.add(detail);
        detail.setHeader(this);
    }
}
