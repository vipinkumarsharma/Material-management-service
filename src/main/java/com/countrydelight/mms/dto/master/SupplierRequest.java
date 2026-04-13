package com.countrydelight.mms.dto.master;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierRequest {

    @NotBlank(message = "Supplier ID is required")
    private String suppId;

    @NotBlank(message = "Supplier name is required")
    private String suppName;

    private String address;

    private String mobNo;

    private String email;

    private String gstin;
}
