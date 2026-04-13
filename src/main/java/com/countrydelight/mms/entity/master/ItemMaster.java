package com.countrydelight.mms.entity.master;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
// import jakarta.persistence.JoinColumns; // Sub-group temporarily disabled
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "item_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ItemMaster {

    @Id
    @Column(name = "item_id", length = 20)
    private String itemId;

    @Column(name = "item_desc", nullable = false, length = 200)
    private String itemDesc;

    @Column(name = "group_id", length = 20)
    private String groupId;

    @Column(name = "sub_group_id", length = 20)
    private String subGroupId;

    // Sub-group relationship temporarily disabled
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumns({
    //     @JoinColumn(name = "group_id", referencedColumnName = "group_id", insertable = false, updatable = false),
    //     @JoinColumn(name = "sub_group_id", referencedColumnName = "sub_group_id", insertable = false, updatable = false)
    // })
    // private SubGroupMaster subGroup;

    @Column(name = "supp_id", length = 20)
    private String suppId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supp_id", insertable = false, updatable = false)
    private SupplierMaster supplier;

    @Column(name = "unit_id", length = 20)
    private String unitId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", insertable = false, updatable = false)
    private UnitMaster unit;

    @Column(name = "gst_perc", precision = 5, scale = 2)
    private BigDecimal gstPerc = BigDecimal.ZERO;

    // Reference cost price - actual price is derived from last GRN in ledger
    @Column(name = "cost_price", precision = 15, scale = 4)
    private BigDecimal costPrice = BigDecimal.ZERO;

    @Column(name = "mrp", precision = 15, scale = 4)
    private BigDecimal mrp = BigDecimal.ZERO;

    @Column(name = "hsn_code", length = 20)
    private String hsnCode;

    @Column(name = "cess_perc", precision = 5, scale = 2)
    private BigDecimal cessPerc = BigDecimal.ZERO;

    @Column(name = "company_id", length = 20)
    private String companyId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", insertable = false, updatable = false)
    private CompanyMaster company;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
