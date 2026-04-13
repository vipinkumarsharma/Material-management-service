package com.countrydelight.mms.dto.master;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRequest {

    @NotBlank(message = "Item ID is required")
    private String itemId;

    @NotBlank(message = "Item description is required")
    private String itemDesc;

    private String groupId;

    private String subGroupId;

    private String suppId;

    @NotBlank(message = "Unit ID is required")
    private String unitId;

    private BigDecimal gstPerc;

    private BigDecimal costPrice;

    private BigDecimal mrp;

    private String hsnCode;

    private BigDecimal cessPerc;

    private String companyId;
}
