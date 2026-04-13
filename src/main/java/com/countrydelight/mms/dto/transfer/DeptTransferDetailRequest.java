package com.countrydelight.mms.dto.transfer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class DeptTransferDetailRequest {

    @NotBlank(message = "Item ID is required")
    private String itemId;

    @NotBlank(message = "Location ID is required")
    private String locationId;

    @NotBlank(message = "Unit ID is required")
    private String unitId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private BigDecimal qtyTransferred;

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
