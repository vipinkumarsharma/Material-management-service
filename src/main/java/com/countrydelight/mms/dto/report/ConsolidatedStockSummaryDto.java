package com.countrydelight.mms.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsolidatedStockSummaryDto {
    private String itemId;
    private String itemDesc;
    private BigDecimal totalQty;
    private BigDecimal totalValue;
    private List<BranchBreakup> branchBreakup;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BranchBreakup {
        private String branchId;
        private String branchName;
        private BigDecimal qty;
        private BigDecimal value;
    }
}
