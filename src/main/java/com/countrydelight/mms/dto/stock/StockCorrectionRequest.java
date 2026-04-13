package com.countrydelight.mms.dto.stock;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockCorrectionRequest {

    @NotNull
    private Long ledgerId;

    @NotNull
    private BigDecimal correctedQty;

    private BigDecimal correctedRate;

    @NotBlank
    private String reason;
}
