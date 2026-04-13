package com.countrydelight.mms.entity.transfer;

import com.countrydelight.mms.entity.master.ItemMaster;
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
@Table(name = "stock_transfer_detail")
@IdClass(StockTransferDetailId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTransferDetail {

    @Id
    @Column(name = "transfer_id")
    private Long transferId;

    @Id
    @Column(name = "item_id", length = 20)
    private String itemId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id", insertable = false, updatable = false)
    private StockTransferHeader transferHeader;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", insertable = false, updatable = false)
    private ItemMaster item;

    @Column(name = "qty_sent", nullable = false, precision = 15, scale = 4)
    private BigDecimal qtySent;

    @Column(name = "rate", nullable = false, precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal rate = BigDecimal.ZERO;

    @Column(name = "qty_received", precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal qtyReceived = BigDecimal.ZERO;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
