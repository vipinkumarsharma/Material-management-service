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
public class AuditExceptionReportDto {
    private String exceptionType; // GRN_WITHOUT_PO, HIGH_PRICE_VARIANCE, OVERRIDE, CROSS_BRANCH_ANOMALY
    private String branchId;
    private String branchName;
    private Long refId;
    private String refType;
    private LocalDate txnDate;
    private String description;
    private BigDecimal amount;
    private BigDecimal variancePercent;
    private String approvedBy;
    private LocalDateTime approvalDate;
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL
}
