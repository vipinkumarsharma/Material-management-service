package com.countrydelight.mms.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceVarianceReportDto {
    private Long grnId;
    private LocalDate grnDate;
    private String branchId;
    private String branchName;
    private String itemId;
    private String itemDesc;
    private String suppId;
    private String suppName;
    private BigDecimal lastReferencePrice;
    private BigDecimal enteredPrice;
    private BigDecimal varianceAmount;
    private BigDecimal variancePercent;
    private String approvalRole;
    private String approvedBy;
    private LocalDateTime approvalDate;
    private String status;
}
