package com.countrydelight.mms.entity.stock;

import com.countrydelight.mms.entity.master.BranchMaster;
import com.countrydelight.mms.entity.master.DepartmentMaster;
import com.countrydelight.mms.entity.master.ItemMaster;
import com.countrydelight.mms.entity.master.LocationMaster;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Material Stock Ledger - SINGLE SOURCE OF TRUTH
 *
 * IMPORTANT RULES:
 * 1. NO UPDATE operations allowed on this table
 * 2. NO DELETE operations allowed on this table
 * 3. Only INSERT operations are permitted
 * 4. All stock movements must be recorded here
 */
@Entity
@Table(name = "material_stock_ledger")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialStockLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ledger_id")
    private Long ledgerId;

    @Column(name = "branch_id", nullable = false, length = 20)
    private String branchId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", insertable = false, updatable = false)
    private BranchMaster branch;

    @Column(name = "item_id", nullable = false, length = 20)
    private String itemId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", insertable = false, updatable = false)
    private ItemMaster item;

    @Column(name = "location_id", nullable = false, length = 20)
    private String locationId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", insertable = false, updatable = false)
    private LocationMaster location;

    @Column(name = "dept_id")
    private Integer deptId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id", insertable = false, updatable = false)
    private DepartmentMaster department;

    @Column(name = "txn_date", nullable = false)
    private LocalDate txnDate;

    @Column(name = "txn_type", nullable = false, length = 30)
    private String txnType;

    @Column(name = "ref_id")
    private Long refId; // Reference to GRN/Issue/Transfer ID (null for OPENING_BALANCE)

    @Column(name = "qty_in", nullable = false, precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal qtyIn = BigDecimal.ZERO;

    @Column(name = "qty_out", nullable = false, precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal qtyOut = BigDecimal.ZERO;

    @Column(name = "rate", nullable = false, precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal rate = BigDecimal.ZERO;

    @Column(name = "balance_qty", nullable = false, precision = 15, scale = 4)
    private BigDecimal balanceQty;

    @Column(name = "remarks", length = 255)
    private String remarks;

    @Column(name = "created_on", insertable = false, updatable = false)
    private LocalDateTime createdOn;
}
