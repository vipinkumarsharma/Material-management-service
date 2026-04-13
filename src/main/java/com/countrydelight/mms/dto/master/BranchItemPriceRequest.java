package com.countrydelight.mms.dto.master;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchItemPriceRequest {

    @NotNull(message = "Cost price is required")
    private BigDecimal costPrice;

    private BigDecimal mrp;
}
