package com.countrydelight.mms.entity.purchase;

import com.countrydelight.mms.entity.master.ItemMaster;
import com.countrydelight.mms.entity.master.LocationMaster;
import com.countrydelight.mms.entity.master.UnitMaster;
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

@Entity
@Table(name = "purchase_voucher_detail_to")
@IdClass(PurchaseVoucherDetailToId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseVoucherDetailTo {

    @Id
    @Column(name = "pv_id")
    private Long pvId;

    @Id
    @Column(name = "item_id", length = 20)
    private String itemId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pv_id", insertable = false, updatable = false)
    private PurchaseVoucherHeader header;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", insertable = false, updatable = false)
    private ItemMaster item;

    @Column(name = "unit_id", length = 20)
    private String unitId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", insertable = false, updatable = false)
    private UnitMaster unit;

    @Column(name = "location_id", length = 20)
    private String locationId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", insertable = false, updatable = false)
    private LocationMaster location;

    @Column(name = "qty", precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal qty = BigDecimal.ZERO;

    @Column(name = "rate", precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal rate = BigDecimal.ZERO;

    @Column(name = "gross_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal grossAmount = BigDecimal.ZERO;

    @Column(name = "discount_perc", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountPerc = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "gst_perc", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal gstPerc = BigDecimal.ZERO;

    @Column(name = "gst_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal gstAmount = BigDecimal.ZERO;

    @Column(name = "cess_perc", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal cessPerc = BigDecimal.ZERO;

    @Column(name = "cess_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal cessAmount = BigDecimal.ZERO;

    @Column(name = "net_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal netAmount = BigDecimal.ZERO;

    @Column(name = "ordered_qty", precision = 15, scale = 4)
    private BigDecimal orderedQty;

    @Column(name = "line_narration", columnDefinition = "TEXT")
    private String lineNarration;
}
