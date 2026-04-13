package com.countrydelight.mms.entity.stock;

import com.countrydelight.mms.entity.master.BranchMaster;
import com.countrydelight.mms.entity.master.ItemMaster;
import com.countrydelight.mms.entity.master.LocationMaster;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Branch Material Stock - Summary View
 *
 * This table is a denormalized summary derived from MaterialStockLedger.
 * It is updated whenever stock movements occur (GRN posting, Issue posting, etc.)
 */
@Entity
@Table(name = "branch_material_stock")
@IdClass(BranchMaterialStockId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchMaterialStock {

    @Id
    @Column(name = "branch_id", length = 20)
    private String branchId;

    @Id
    @Column(name = "item_id", length = 20)
    private String itemId;

    @Id
    @Column(name = "location_id", length = 20)
    private String locationId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", insertable = false, updatable = false)
    private BranchMaster branch;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", insertable = false, updatable = false)
    private ItemMaster item;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", insertable = false, updatable = false)
    private LocationMaster location;

    @Column(name = "qty_on_hand", nullable = false, precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal qtyOnHand = BigDecimal.ZERO;

    @Column(name = "avg_cost", nullable = false, precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal avgCost = BigDecimal.ZERO;

    @Column(name = "last_updated", insertable = false, updatable = false)
    private LocalDateTime lastUpdated;
}
