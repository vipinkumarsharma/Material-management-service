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
public class InterBranchTransferReportDto {
    private Long transferId;
    private LocalDate transferDate;
    private String branchId;
    private String branchName;
    private Integer fromDeptId;
    private String fromDeptName;
    private Integer toDeptId;
    private String toDeptName;
    private String itemId;
    private String itemDesc;
    private BigDecimal qtyTransferred;
    private BigDecimal rate;
    private BigDecimal value;
    private String status;
    private String createdBy;
}
