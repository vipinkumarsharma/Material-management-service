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
public class StockStatementDto {
    private String branchId;
    private String branchName;
    private String itemId;
    private String itemDesc;
    private String locationId;
    private String locationName;
    private BigDecimal openingQty;
    private BigDecimal openingValue;
    private BigDecimal inwardQty;
    private BigDecimal inwardValue;
    private BigDecimal outwardQty;
    private BigDecimal outwardValue;
    private BigDecimal closingQty;
    private BigDecimal closingValue;
}
