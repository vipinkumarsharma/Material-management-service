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
public class GrnSummaryReportDto {
    private Long grnId;
    private String branchId;
    private String branchName;
    private String suppId;
    private String suppName;
    private String invoiceNo;
    private LocalDate grnDate;
    private BigDecimal totalQty;
    private BigDecimal totalValue;
    private String status;
    private String createdBy;
    private String approvedBy;
}
