package com.countrydelight.mms.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpeningBalanceSummary {
    private int totalEntries;
    private BigDecimal totalQty;
    private BigDecimal totalValue;
    private LocalDate cutoffDate;
}
