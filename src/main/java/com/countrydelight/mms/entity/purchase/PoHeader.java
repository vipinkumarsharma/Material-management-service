package com.countrydelight.mms.entity.purchase;

import com.countrydelight.mms.entity.master.BranchMaster;
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
@Table(name = "po_header")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PoHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "po_id")
    private Long poId;

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

    @Column(name = "supp_id", nullable = false, length = 20)
    private String suppId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supp_id", insertable = false, updatable = false)
    private SupplierMaster supplier;

    @Column(name = "po_date", nullable = false)
    private LocalDate poDate;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "OPEN"; // OPEN / PARTIAL / CLOSED

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @OneToMany(mappedBy = "poHeader", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PoDetail> details = new ArrayList<>();

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public void addDetail(PoDetail detail) {
        details.add(detail);
        detail.setPoHeader(this);
    }
}
