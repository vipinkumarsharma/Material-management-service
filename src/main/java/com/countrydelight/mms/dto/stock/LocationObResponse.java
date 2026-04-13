package com.countrydelight.mms.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationObResponse {

    private Long obId;
    private String branchId;
    private String locationId;
    private LocalDate cutoffDate;
    private LocalDateTime createdAt;
    private boolean locked;
    private int totalEntries;
    private BigDecimal totalQty;
    private BigDecimal totalValue;
    private List<ItemObLine> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemObLine {
        private String itemId;
        private BigDecimal qty;
        private BigDecimal rate;
        private BigDecimal totalValue;
    }
}
