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
public class SupplierReportSummaryDto {
    private String suppId;
    private String suppName;
    private String itemId;
    private String itemDesc;
    private BigDecimal totalQtyReceived;
    private BigDecimal totalValue;
    private BigDecimal avgRate;
    private Long grnCount;
}
