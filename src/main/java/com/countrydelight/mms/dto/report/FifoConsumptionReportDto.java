package com.countrydelight.mms.dto.report;

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
public class FifoConsumptionReportDto {
    private Long issueId;
    private LocalDate issueDate;
    private String branchId;
    private String branchName;
    private String itemId;
    private String itemDesc;
    private BigDecimal issuedQty;
    private BigDecimal weightedAvgRate;
    private List<GrnConsumption> grnConsumptions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GrnConsumption {
        private Long grnId;
        private LocalDate grnDate;
        private BigDecimal qtyConsumed;
        private BigDecimal rate;
        private BigDecimal value;
    }
}
