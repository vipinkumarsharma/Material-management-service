package com.countrydelight.mms.entity.transfer;

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
@Table(name = "dept_transfer_detail")
@IdClass(DeptTransferDetailId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeptTransferDetail {

    @Id
    @Column(name = "dept_transfer_id")
    private Long deptTransferId;

    @Id
    @Column(name = "item_id", length = 20)
    private String itemId;

    @Column(name = "location_id", nullable = false, length = 20)
    private String locationId;

    @Column(name = "unit_id", length = 20)
    private String unitId;

    @Column(name = "qty_transferred", nullable = false, precision = 15, scale = 4)
    private BigDecimal qtyTransferred;

    @Column(name = "rate", nullable = false, precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal rate = BigDecimal.ZERO;

    @Column(name = "gross_amount", precision = 15, scale = 4)
    private BigDecimal grossAmount;

    @Column(name = "discount_perc", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountPerc = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "gst_perc", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal gstPerc = BigDecimal.ZERO;

    @Column(name = "gst_amount", precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal gstAmount = BigDecimal.ZERO;

    @Column(name = "cess_perc", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal cessPerc = BigDecimal.ZERO;

    @Column(name = "cess_amount", precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal cessAmount = BigDecimal.ZERO;

    @Column(name = "net_amount", precision = 15, scale = 4)
    private BigDecimal netAmount;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_transfer_id", insertable = false, updatable = false)
    private DeptTransferHeader header;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", insertable = false, updatable = false)
    private ItemMaster item;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", insertable = false, updatable = false)
    private LocationMaster location;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", insertable = false, updatable = false)
    private UnitMaster unit;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
