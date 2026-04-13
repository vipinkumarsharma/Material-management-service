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
public class BranchReportDto {
    private String branchId;
    private String branchName;
    private BigDecimal currentStockValue;
    private Long currentStockItems;
    private Long grnCount;
    private BigDecimal grnValue;
    private Long issueCount;
    private BigDecimal issueValue;
    private Long transferInCount;
    private BigDecimal transferInValue;
    private Long transferOutCount;
    private BigDecimal transferOutValue;
    private List<TopItem> topItems;
    private List<TopSupplier> topSuppliers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopItem {
        private String itemId;
        private String itemDesc;
        private BigDecimal stockQty;
        private BigDecimal stockValue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopSupplier {
        private String suppId;
        private String suppName;
        private Long grnCount;
        private BigDecimal totalValue;
    }
}
