package com.countrydelight.mms.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderItemDto {
    private LocalDate date;
    private String suppName;
    private String locationName;
    private String poNo;
    private LocalDate poDate;
    private String itemId;
    private String itemDesc;
    private String unitDesc;
    private BigDecimal poQty;
    private BigDecimal basicPrice;
    private BigDecimal basicAmount;
    private BigDecimal gstRate;
    private BigDecimal gstAmount;
    private BigDecimal totalAmount;
    private String remarks;
}
