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
public class PvItemFulfillmentReportDto {
    // PV header fields
    private Long pvId;
    private String pvVoucherNumber;
    private LocalDate pvDate;
    private String branchId;
    private String branchName;
    private String suppId;
    private String suppName;
    private String pvStatus;
    // Item + qty fields
    private String itemId;
    private String itemDesc;
    private BigDecimal pvQty;
    private BigDecimal pvRate;
    private BigDecimal pvLineAmount;
    private BigDecimal totalReceivedQty;
    private BigDecimal pendingQty;
    private BigDecimal fulfillmentPercent;
}
