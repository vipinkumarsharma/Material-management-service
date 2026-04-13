package com.countrydelight.mms.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueToProductionReportDto {
    private Long issueId;
    private LocalDate issueDate;
    private String branchId;
    private String branchName;
    private String itemId;
    private String itemDesc;
    private BigDecimal qtyIssued;
    private BigDecimal fifoRate;
    private BigDecimal totalValue;
    private String issuedTo;
    private String locationId;
    private String locationName;
    private String status;
    private Integer deptId;
    private String deptName;
    private String issueType;
    private String suppId;
    private String suppName;
}
