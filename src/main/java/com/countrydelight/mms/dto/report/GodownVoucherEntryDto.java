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
public class GodownVoucherEntryDto {
    private Long ledgerId;
    private LocalDate txnDate;
    private String particulars;
    private String vchType;
    private String vchNo;
    private BigDecimal inwardsQty;
    private BigDecimal inwardsRate;
    private BigDecimal inwardsValue;
    private BigDecimal outwardsQty;
    private BigDecimal outwardsRate;
    private BigDecimal outwardsValue;
    private BigDecimal closingQty;
    private BigDecimal closingRate;
    private BigDecimal closingValue;
}
