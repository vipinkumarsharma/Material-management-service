package com.countrydelight.mms.dto.purchase;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThirdPartySupplierRequest {

    private String suppId;      // optional — auto-generated if blank
    private String suppName;
    private String mobNo;
    private String email;
    private String gstin;
    private String address1;    // maps to SupplierMaster.address
}
