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
public class GrnDetailReportDto {
    private Long grnId;
    private LocalDate grnDate;
    private String branchId;
    private String branchName;
    private String suppId;
    private String suppName;
    private String itemId;
    private String itemDesc;
    private String unitId;
    private BigDecimal qtyReceived;
    private BigDecimal rate;
    private BigDecimal grossAmount;
    private BigDecimal gstPerc;
    private BigDecimal gstAmount;
    private BigDecimal discountPerc;
    private BigDecimal discountAmount;
    private BigDecimal netAmount;
    private String locationId;
    private String locationName;
    private String invoiceNo;
    private String status;
    private Integer deptId;
    private String deptName;
}
