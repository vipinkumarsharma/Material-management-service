package com.countrydelight.mms.dto.outward;

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
public class IssueDetailRequest {

    @NotBlank(message = "Item ID is required")
    private String itemId;

    @NotBlank(message = "Location ID is required")
    private String locationId;

    @NotNull(message = "Quantity to issue is required")
    @DecimalMin(value = "0.0001", message = "Quantity must be greater than 0")
    private BigDecimal qtyIssued;
}
