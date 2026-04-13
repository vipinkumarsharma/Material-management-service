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
public class ReceiptNoteItemDto {
    private LocalDate grnDate;
    private String grnNo;
    private String branchName;
    private String suppName;
    private String poNo;
    private LocalDate poDate;
    private String invNo;
    private LocalDate invDate;
    private String itemId;
    private String itemDesc;
    private String unitDesc;
    private BigDecimal qty;
    private BigDecimal basicPrice;
    private BigDecimal basicAmount;
    private BigDecimal gstRate;
    private BigDecimal gstAmount;
    private BigDecimal amountWithGst;
    private String remarks;
}
