package com.countrydelight.mms.dto.inward;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
public class GrnDetailRequest {

    @NotBlank(message = "Item ID is required")
    private String itemId;

    @NotBlank(message = "Unit ID is required")
    private String unitId;

    @NotBlank(message = "Location ID is required")
    private String locationId;

    @NotNull(message = "Quantity received is required")
    @DecimalMin(value = "0.0001", message = "Quantity must be greater than 0")
    private BigDecimal qtyReceived;


    @NotNull(message = "Rate is required")
    @DecimalMin(value = "0", message = "Rate cannot be negative")
    private BigDecimal rate;

    private BigDecimal grossAmount;
    private BigDecimal discountPerc;
    private BigDecimal discountAmount;
    private BigDecimal gstPerc;
    private BigDecimal gstAmount;
    private BigDecimal cessPerc;
    private BigDecimal cessAmount;
    private BigDecimal netAmount;
}
