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
public class PoVsGrnReportDto {
    private Long poId;
    private LocalDate poDate;
    private String branchId;
    private String branchName;
    private String suppId;
    private String suppName;
    private String itemId;
    private String itemDesc;
    private BigDecimal qtyOrdered;
    private BigDecimal qtyReceived;
    private BigDecimal pendingQty;
    private BigDecimal poRate;
    private String poStatus;
    private BigDecimal fulfillmentPercent;
}
