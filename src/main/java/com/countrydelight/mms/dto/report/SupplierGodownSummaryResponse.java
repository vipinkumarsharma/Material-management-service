package com.countrydelight.mms.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierGodownSummaryResponse {
    private String suppId;
    private String suppName;
    private Long godownId;
    private String godownName;
    private LocalDate fromDate;
    private LocalDate toDate;
    private List<GodownSummaryResponse.GroupRow> groups;
    private GodownSummaryDto grandTotal;
}
