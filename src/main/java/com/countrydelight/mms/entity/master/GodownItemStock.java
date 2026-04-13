package com.countrydelight.mms.entity.master;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "godown_item_stock")
@IdClass(GodownItemStockId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GodownItemStock {

    @Id
    @Column(name = "godown_id", nullable = false)
    private Long godownId;

    @Id
    @Column(name = "item_id", nullable = false, length = 20)
    private String itemId;

    @Column(name = "qty", nullable = false, precision = 15, scale = 4)
    private BigDecimal qty;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
