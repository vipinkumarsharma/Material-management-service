package com.countrydelight.mms.entity.inward;

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
import java.time.LocalDateTime;

@Entity
@Table(name = "grn_detail")
@IdClass(GrnDetailId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrnDetail {

    @Id
    @Column(name = "grn_id")
    private Long grnId;

    @Id
    @Column(name = "item_id", length = 20)
    private String itemId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grn_id", insertable = false, updatable = false)
    private GrnHeader grnHeader;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", insertable = false, updatable = false)
    private ItemMaster item;

    @Column(name = "unit_id", nullable = false, length = 20)
    private String unitId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", insertable = false, updatable = false)
    private UnitMaster unit;

    @Column(name = "location_id", nullable = false, length = 20)
    private String locationId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", insertable = false, updatable = false)
    private LocationMaster location;

    @Column(name = "qty_received", nullable = false, precision = 15, scale = 4)
    private BigDecimal qtyReceived;

    @Column(name = "rate", nullable = false, precision = 15, scale = 4)
    private BigDecimal rate;

    @Column(name = "gross_amount", nullable = false, precision = 15, scale = 4)
    private BigDecimal grossAmount;

    @Column(name = "gst_perc", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal gstPerc = BigDecimal.ZERO;

    @Column(name = "gst_amount", precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal gstAmount = BigDecimal.ZERO;

    @Column(name = "discount_perc", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountPerc = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "cess_perc", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal cessPerc = BigDecimal.ZERO;

    @Column(name = "cess_amount", precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal cessAmount = BigDecimal.ZERO;

    @Column(name = "net_amount", precision = 15, scale = 4)
    private BigDecimal netAmount;

    @Column(name = "qty_remaining", nullable = false, precision = 15, scale = 4)
    private BigDecimal qtyRemaining; // For FIFO tracking

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
