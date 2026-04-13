package com.countrydelight.mms.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierGodownVoucherResponse {
    private String suppId;
    private String suppName;
    private String itemId;
    private String itemDesc;
    private LocalDate fromDate;
    private LocalDate toDate;
    private BigDecimal openingQty;
    private BigDecimal openingRate;
    private BigDecimal openingValue;
    private List<GodownVoucherEntryDto> entries;
    private BigDecimal totalInwardsQty;
    private BigDecimal totalInwardsValue;
    private BigDecimal totalOutwardsQty;
    private BigDecimal totalOutwardsValue;
    private BigDecimal closingQty;
    private BigDecimal closingRate;
    private BigDecimal closingValue;
}
