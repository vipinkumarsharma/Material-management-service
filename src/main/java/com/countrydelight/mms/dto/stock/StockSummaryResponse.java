package com.countrydelight.mms.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockSummaryResponse {
    private String branchId;
    private String branchName;
    private String itemId;
    private String itemDesc;
    private String locationId;
    private String locationName;
    private BigDecimal qtyOnHand;
    private BigDecimal avgCost;
    private BigDecimal totalValue;
    private LocalDateTime lastUpdated;
}
