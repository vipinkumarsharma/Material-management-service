package com.countrydelight.mms.dto.stock;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemStockPositionResponse {
    private String itemId;
    private String itemDesc;
    private String branchId;
    private String branchName;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate toDate;
    private StockAmountDto openingBalance;
    private List<StockVoucherLineDto> transactions;
    private TotalsDto totals;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TotalsDto {
        private StockAmountDto inward;
        private StockAmountDto outward;
        private StockAmountDto closing;
    }
}
