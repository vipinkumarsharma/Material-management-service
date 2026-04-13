package com.countrydelight.mms.dto.master;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class GodownStockResponse {

    private Long id;
    private String godownName;
    private String suppId;
    private List<GodownItemStockEntry> items;

    @Getter
    @Builder
    public static class GodownItemStockEntry {
        private String itemId;
        private BigDecimal qty;
    }
}
