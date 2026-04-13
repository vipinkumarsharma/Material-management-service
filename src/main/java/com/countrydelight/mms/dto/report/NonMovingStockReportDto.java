package com.countrydelight.mms.dto.report;

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
public class NonMovingStockReportDto {
    private String branchId;
    private String branchName;
    private String itemId;
    private String itemDesc;
    private String locationId;
    private String locationName;
    private LocalDate lastMovementDate;
    private Integer daysSinceLastMovement;
    private BigDecimal qty;
    private BigDecimal avgCost;
    private BigDecimal value;
    private String movementCategory; // NON_MOVING / SLOW_MOVING
}
