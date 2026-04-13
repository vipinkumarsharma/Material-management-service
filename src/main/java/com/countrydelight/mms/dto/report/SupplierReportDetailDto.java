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
public class SupplierReportDetailDto {
    private String suppId;
    private String suppName;
    private Long grnId;
    private LocalDate grnDate;
    private Long pvId;
    private LocalDate pvDate;
    private String itemId;
    private String itemDesc;
    private BigDecimal qtyReceived;
    private BigDecimal rate;
    private BigDecimal netAmount;
    private String branchId;
    private String branchName;
    private String invoiceNo;
    private Integer deptId;
    private String deptName;
}
