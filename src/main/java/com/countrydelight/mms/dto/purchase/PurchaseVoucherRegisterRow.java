package com.countrydelight.mms.dto.purchase;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Getter
public class PurchaseVoucherRegisterRow {
    private Long pvId;
    private LocalDate date;
    private String itemId;
    private String particulars;
    private String suppId;
    private String supplier;
    private String voucherTypeId;
    private String voucherType;
    private String voucherNo;
    private BigDecimal qty;
    private String unitId;
    private BigDecimal rate;
    private BigDecimal value;
    private BigDecimal grossTotal;
    private String voucherCategory;
    private String status;
    private String fromBranchId;
    private String fromBranchName;
    private String consigneeId;
    private String consigneeType;
    private String consigneeEntityType;
    private String fromEntityType;
}
