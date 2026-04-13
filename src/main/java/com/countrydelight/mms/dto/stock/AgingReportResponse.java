package com.countrydelight.mms.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Aging report response - aging is DERIVED from ledger txn_date, never stored.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgingReportResponse {
    private String branchId;
    private String branchName;
    private LocalDate asOfDate;
    private List<ItemAgingDetail> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemAgingDetail {
        private String itemId;
        private String itemDesc;
        private String locationId;
        private String locationName;
        private BigDecimal totalQty;
        private BigDecimal totalValue;

        // Aging buckets: 0-30, 31-60, 61-90, 90+
        private AgingBucket bucket0To30;
        private AgingBucket bucket31To60;
        private AgingBucket bucket61To90;
        private AgingBucket bucket90Plus;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AgingBucket {
        private BigDecimal qty;
        private BigDecimal value;
        private BigDecimal percentage; // Percentage of total
    }
}
