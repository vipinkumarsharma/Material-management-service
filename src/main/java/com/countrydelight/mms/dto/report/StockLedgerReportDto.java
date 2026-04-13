package com.countrydelight.mms.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockLedgerReportDto {
    private Long ledgerId;
    private LocalDate txnDate;
    private String branchId;
    private String branchName;
    private String itemId;
    private String itemDesc;
    private String locationId;
    private String locationName;
    private String txnType;
    private Long refId;
    private BigDecimal qtyIn;
    private BigDecimal qtyOut;
    private BigDecimal rate;
    private BigDecimal balanceQty;
    private LocalDateTime createdOn;
}
