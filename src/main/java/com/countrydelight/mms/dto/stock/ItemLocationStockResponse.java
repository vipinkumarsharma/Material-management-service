package com.countrydelight.mms.dto.stock;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ItemLocationStockResponse {
    private String locationId;
    private String locationName;
    private BigDecimal qty;
}
