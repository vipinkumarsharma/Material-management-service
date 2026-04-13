package com.countrydelight.mms.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrentStockReportDto {
    private String branchId;
    private String branchName;
    private String itemId;
    private String itemDesc;
    private String locationId;
    private String locationName;
    private BigDecimal qtyOnHand;
    private BigDecimal avgCost;
    private BigDecimal stockValue; // qtyOnHand * avgCost
}
