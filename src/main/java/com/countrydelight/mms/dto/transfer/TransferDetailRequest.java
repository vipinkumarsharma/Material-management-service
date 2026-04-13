package com.countrydelight.mms.dto.transfer;

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
public class TransferDetailRequest {

    @NotBlank(message = "Item ID is required")
    private String itemId;

    @NotBlank(message = "Source location ID is required")
    private String sourceLocationId;

    @NotBlank(message = "Destination location ID is required")
    private String destLocationId;

    @NotNull(message = "Quantity to send is required")
    @DecimalMin(value = "0.0001", message = "Quantity must be greater than 0")
    private BigDecimal qtySent;
}
