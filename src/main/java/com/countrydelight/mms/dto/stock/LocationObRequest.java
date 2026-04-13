package com.countrydelight.mms.dto.stock;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class LocationObRequest {

    @NotBlank
    private String branchId;

    @NotBlank
    private String locationId;

    @NotNull
    private LocalDate cutoffDate;

    @NotEmpty
    @Valid
    private List<ItemObEntry> items;

    @Getter
    @Setter
    public static class ItemObEntry {

        @NotBlank
        private String itemId;

        @NotNull
        private BigDecimal qty;
    }
}
