package com.countrydelight.mms.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemStockPositionDto {

    String locationType;   // "BRANCH" or "GODOWN"

    String itemId;
    String itemDesc;

    // Branch fields (null for GODOWN rows)
    String branchId;
    String branchName;
    String locationId;
    String locationName;

    // Godown fields (null for BRANCH rows)
    String suppId;
    String suppName;
    String godownId;
    String godownName;

    // Common
    BigDecimal qty;
    BigDecimal avgCost;    // null for GODOWN rows
    BigDecimal stockValue; // null for GODOWN rows
}
