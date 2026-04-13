package com.countrydelight.mms.dto.stock;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockVoucherLineDto {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    private String particulars;
    private String vchType;
    private String vchNo;
    private StockAmountDto inward;
    private StockAmountDto outward;
    private StockAmountDto closing;
}
