package com.countrydelight.mms.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockCorrectionResponse {
    private Long originalLedgerId;
    private Long reversalLedgerId;
    private Long correctionLedgerId;
    private String txnType;
    private BigDecimal oldQty;
    private BigDecimal newQty;
    private BigDecimal oldRate;
    private BigDecimal newRate;
    private int balancesRecalculated;
}
