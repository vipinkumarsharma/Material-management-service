package com.countrydelight.mms.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Common filter criteria for all reports.
 * All filters are OPTIONAL.
 * If branchIds is null or empty, report shows ALL branches.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportFilterDto {

    // Branch filter - NULL means ALL branches
    private List<String> branchIds;

    // Date range filters
    private LocalDate fromDate;
    private LocalDate toDate;

    // Item filter
    private String itemId;
    private List<String> itemIds;
    private String groupId;
    private String subGroupId;

    // Supplier filter
    private String suppId;
    private List<String> suppIds;

    // Location filter
    private String locationId;

    // Department filter
    private Integer deptId;

    // Status filter
    private String status;
    private List<String> statuses;

    // Pagination
    private Integer page;
    private Integer size;

    // Aging specific
    private Integer agingDays; // for non-moving stock report

    // Price variance specific
    private BigDecimal minVariancePercent;

    // Purchase Voucher filter
    private Long pvId;
    private String voucherCategory;

    /**
     * Helper to check if branch filter should be applied
     */
    public boolean hasBranchFilter() {
        return branchIds != null && !branchIds.isEmpty();
    }

    /**
     * Helper to get single branch or null
     */
    public String getSingleBranch() {
        if (branchIds != null && branchIds.size() == 1) {
            return branchIds.get(0);
        }
        return null;
    }
}
