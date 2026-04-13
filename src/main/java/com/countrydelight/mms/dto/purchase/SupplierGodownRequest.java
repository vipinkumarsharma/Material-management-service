package com.countrydelight.mms.dto.purchase;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupplierGodownRequest {

    private String suppId;
    private Long id;
    private String itemId;
    private String godownName;
}
