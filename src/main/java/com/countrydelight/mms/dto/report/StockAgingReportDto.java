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
public class StockAgingReportDto {
    private String branchId;
    private String branchName;
    private String itemId;
    private String itemDesc;
    private String locationId;
    private String locationName;
    private String agingBucket; // 0-30, 31-60, 61-90, 90+
    private BigDecimal qty;
    private BigDecimal rate;
    private BigDecimal value;
    private Integer ageDays;
}
