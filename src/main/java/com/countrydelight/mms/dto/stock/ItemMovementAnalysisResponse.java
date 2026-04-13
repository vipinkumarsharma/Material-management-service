package com.countrydelight.mms.dto.stock;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
@Getter
public class ItemMovementAnalysisResponse {

    private String itemId;
    private String itemDesc;
    private String branchId;
    private String branchName;
    private LocalDate fromDate;
    private LocalDate toDate;
    private MovementSectionDto inward;
    private MovementSectionDto outward;

    @Builder
    @Getter
    public static class MovementSectionDto {
        private List<MovementCategoryDto> categories;
        private BigDecimal totalQty;
        private BigDecimal totalValue;
    }

    @Builder
    @Getter
    public static class MovementCategoryDto {
        private String categoryName;
        private List<MovementLineDto> lines;
        private BigDecimal totalQty;
        private BigDecimal totalValue;
    }

    @Builder
    @Getter
    public static class MovementLineDto {
        private String particulars;
        private BigDecimal qty;
        private BigDecimal basicRate;
        private BigDecimal effectiveRate;
        private BigDecimal value;
    }
}
