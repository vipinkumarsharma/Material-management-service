package com.countrydelight.mms.entity.transfer;

import com.countrydelight.mms.entity.inward.GrnHeader;
import com.countrydelight.mms.entity.master.BranchMaster;
import com.countrydelight.mms.entity.master.DepartmentMaster;
import com.countrydelight.mms.entity.outward.IssueHeader;
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
@Table(name = "stock_transfer_header")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTransferHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transfer_id")
    private Long transferId;

    @Column(name = "voucher_number", length = 50)
    private String voucherNumber;

    @Column(name = "voucher_type_id", length = 20)
    private String voucherTypeId;

    @Column(name = "from_branch", nullable = false, length = 20)
    private String fromBranch;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_branch", insertable = false, updatable = false)
    private BranchMaster sourceBranch;

    @Column(name = "to_branch", nullable = false, length = 20)
    private String toBranch;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_branch", insertable = false, updatable = false)
    private BranchMaster destinationBranch;

    @Column(name = "dept_id")
    private Integer deptId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id", insertable = false, updatable = false)
    private DepartmentMaster department;

    @Column(name = "transfer_date", nullable = false)
    private LocalDate transferDate;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "CREATED"; // CREATED / IN_TRANSIT / RECEIVED

    @Column(name = "sender_issue_id")
    private Long senderIssueId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_issue_id", insertable = false, updatable = false)
    private IssueHeader senderIssue;

    @Column(name = "receiver_grn_id")
    private Long receiverGrnId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_grn_id", insertable = false, updatable = false)
    private GrnHeader receiverGrn;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "approved_by", length = 50)
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @OneToMany(mappedBy = "transferHeader", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StockTransferDetail> details = new ArrayList<>();

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public void addDetail(StockTransferDetail detail) {
        details.add(detail);
        detail.setTransferHeader(this);
    }
}
