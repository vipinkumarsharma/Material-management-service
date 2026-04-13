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
public class PvVsGrnReportDto {
    private LocalDate date;
    private String supplierName;
    private String location;
    private String poNo;
    private LocalDate poDate;
    private String itemCode;
    private String itemDescription;
    private String unit;
    private BigDecimal poQty;
    private BigDecimal poBasicPrice;
    private BigDecimal poBasicAmount;
    private BigDecimal poTotalAmount;
    private BigDecimal receivedQty;
    private BigDecimal basicPrice;
    private BigDecimal basicAmount;
    private BigDecimal gstRate;
    private BigDecimal gstAmt;
    private BigDecimal freightAmt;
    private BigDecimal gstOnFreightAmt;
    private BigDecimal totalAmount;
}
