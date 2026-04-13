package com.countrydelight.mms.dto.stock;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class OpeningBalanceRequest {

    @NotNull
    private LocalDate cutoffDate;

    @NotEmpty
    @Valid
    private List<OpeningBalanceEntry> entries;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OpeningBalanceEntry {
        @NotBlank
        private String branchId;

        @NotBlank
        private String itemId;

        private String locationId;

        private Integer deptId;

        @NotNull
        private BigDecimal qty;
    }
}
