package com.countrydelight.mms.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DTO for Stock Summary Report (TallyPrime-style period-end inventory review).
 * Groups items by item group with sub-totals per group and a grand total row.
 * Movement columns are dynamic, driven by VoucherTypeMaster.reportSummaryTitle.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockSummaryReportDto {

    private ReportHeader reportHeader;
    /** Ordered column metadata: IN-type columns first, then OUT-type columns. */
    private List<ColumnMeta> columns;
    private List<GroupRow> groups;
    private GroupRow grandTotal;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReportHeader {
        private String companyName;
        private String branchName;    // null if multi-branch
        private String address;
        private String pincode;
        private String gstNo;
        private String reportTitle;
        private LocalDate fromDate;
        private LocalDate toDate;
        private String periodDesc;    // "For 1-Apr-25" or "1-Apr-25 to 30-Apr-25"
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ColumnMeta {
        private String title;        // = VoucherTypeMaster.reportSummaryTitle
        private String movementType; // "IN" or "OUT"
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GroupRow {
        private String groupId;
        private String groupDesc;
        private List<ItemRow> items;

        private MovementColumn openingBalance;
        /** Dynamic movement columns keyed by reportSummaryTitle. */
        private Map<String, MovementColumn> movements;
        private ClosingColumn closingBalance;
        private MovementColumn consumed;
        private MovementColumn avgConsumptionPerDay;
        private Integer daysConsidered;
        private Integer daysCovered;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemRow {
        private String itemId;
        private String itemDesc;
        private String groupId;
        private String unitId;

        private MovementColumn openingBalance;
        /** Dynamic movement columns keyed by reportSummaryTitle. */
        private Map<String, MovementColumn> movements;
        private ClosingColumn closingBalance;
        private MovementColumn consumed;
        private MovementColumn avgConsumptionPerDay;
        private Integer daysConsidered;
        private Integer daysCovered;  // null = infinite (no consumption)
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MovementColumn {
        private BigDecimal qty;
        private BigDecimal rate;
        private BigDecimal value;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClosingColumn {
        private BigDecimal qty;
        private BigDecimal rate;
        private BigDecimal value;
        private BigDecimal gstPerc;
        private BigDecimal gstAmt;
        private BigDecimal totalAmt;
    }
}
