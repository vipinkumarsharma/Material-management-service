package com.countrydelight.mms.entity.outward;

import com.countrydelight.mms.entity.master.BranchMaster;
import com.countrydelight.mms.entity.master.DepartmentMaster;
import com.countrydelight.mms.entity.master.SupplierMaster;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "issue_header")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "issue_id")
    private Long issueId;

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

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "issued_to", nullable = false, length = 100)
    private String issuedTo;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "DRAFT"; // DRAFT / PENDING_APPROVAL / POSTED

    @Column(name = "issue_type", nullable = false, length = 20)
    @Builder.Default
    private String issueType = "REGULAR"; // REGULAR / JOB_WORK

    @Column(name = "supp_id", length = 20)
    private String suppId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supp_id", insertable = false, updatable = false)
    private SupplierMaster supplier;

    @Column(name = "expected_return_date")
    private LocalDate expectedReturnDate;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "approved_by", length = 50)
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @OneToMany(mappedBy = "issueHeader", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<IssueDetail> details = new ArrayList<>();

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public void addDetail(IssueDetail detail) {
        details.add(detail);
        detail.setIssueHeader(this);
    }
}
