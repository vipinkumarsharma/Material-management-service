package com.countrydelight.mms.dto.inward;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response for price suggestion during GRN entry.
 * Returns the last GRN rate from the ledger for the item at the branch.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceSuggestionResponse {
    private String itemId;
    private String itemDesc;
    private String branchId;

    // Last GRN price from ledger (primary suggestion)
    private BigDecimal lastGrnRate;
    private LocalDate lastGrnDate;
    private Long lastGrnId;

    // Branch-level price (second fallback)
    private BigDecimal branchCostPrice;

    // Reference price from item master (tertiary reference only)
    private BigDecimal itemMasterCostPrice;

    // Price source indicator
    private PriceSource priceSource;

    public enum PriceSource {
        LAST_GRN,       // Price from last GRN in ledger
        BRANCH_PRICE,   // Fallback to branch-level price
        ITEM_MASTER,    // Fallback to item master (no GRN history)
        NO_HISTORY      // No price history available
    }
}
