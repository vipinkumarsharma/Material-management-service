package com.countrydelight.mms.dto.inward;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

/**
 * Price variance information for approval workflow.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceVarianceInfo {
    private String itemId;
    private String itemDesc;
    private BigDecimal lastGrnRate;
    private BigDecimal newRate;
    private BigDecimal varianceAmount;
    private BigDecimal variancePercentage;
    private boolean requiresApproval;
    private String requiredRole;
}
