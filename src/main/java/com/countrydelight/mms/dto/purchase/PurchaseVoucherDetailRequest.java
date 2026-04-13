package com.countrydelight.mms.dto.purchase;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PurchaseVoucherDetailRequest {

    @NotBlank
    private String itemId;

    private String unitId;
    private String locationId;

    private BigDecimal qty = BigDecimal.ZERO;
    private BigDecimal rate = BigDecimal.ZERO;
    private BigDecimal grossAmount = BigDecimal.ZERO;
    private BigDecimal discountPerc = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal gstPerc = BigDecimal.ZERO;
    private BigDecimal gstAmount = BigDecimal.ZERO;
    private BigDecimal cessPerc = BigDecimal.ZERO;
    private BigDecimal cessAmount = BigDecimal.ZERO;
    private BigDecimal netAmount = BigDecimal.ZERO;
    private BigDecimal orderedQty;
    private String lineNarration;
}
