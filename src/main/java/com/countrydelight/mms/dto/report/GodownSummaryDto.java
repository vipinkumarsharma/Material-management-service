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
public class GodownSummaryDto {
    private String itemId;
    private String itemDesc;
    private String locationId;
    private String locationName;
    private BigDecimal openingQty;
    private BigDecimal openingRate;
    private BigDecimal openingValue;
    private BigDecimal inwardQty;
    private BigDecimal inwardRate;
    private BigDecimal inwardValue;
    private BigDecimal outwardQty;
    private BigDecimal outwardRate;
    private BigDecimal outwardValue;
    private BigDecimal closingQty;
    private BigDecimal closingRate;
    private BigDecimal closingValue;
}
