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
public class ItemReportDetailDto {
    private String itemId;
    private String itemDesc;
    private String txnType;
    private Long txnId;
    private LocalDate txnDate;
    private String branchId;
    private String branchName;
    private BigDecimal qtyIn;
    private BigDecimal qtyOut;
    private BigDecimal rate;
    private BigDecimal value;
    private String counterparty;
    private String refNo;
}
