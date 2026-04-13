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
public class ItemReportSummaryDto {
    private String itemId;
    private String itemDesc;
    private String groupId;
    private String subGroupId;
    private BigDecimal currentStockQty;
    private BigDecimal currentStockValue;
    private BigDecimal totalGrnQty;
    private BigDecimal totalGrnValue;
    private BigDecimal totalIssueQty;
    private BigDecimal totalIssueValue;
    private Long supplierCount;
    private LocalDate lastGrnDate;
    private LocalDate lastIssueDate;
}
