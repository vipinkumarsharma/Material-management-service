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
public class GodownSummaryResponse {
    private String branchId;
    private String branchName;
    private String locationId;
    private String locationName;
    private LocalDate fromDate;
    private LocalDate toDate;
    private List<GroupRow> groups;
    private GodownSummaryDto grandTotal;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GroupRow {
        private String groupId;
        private String groupDesc;
        private List<GodownSummaryDto> items;
        private GodownSummaryDto subTotal;
    }
}
