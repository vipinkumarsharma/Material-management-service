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
public class GrnVsInvoiceReportDto {
    private Long grnId;
    private String branchId;
    private String branchName;
    private String suppId;
    private String suppName;
    private String invoiceNo;
    private LocalDate invoiceDate;
    private BigDecimal invoiceAmount;
    private BigDecimal grnAmount;
    private BigDecimal difference;
    private BigDecimal differencePercent;
    private String approvalStatus;
    private String approvedBy;
}
