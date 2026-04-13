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
public class BranchRequest {

    @NotBlank(message = "Branch ID is required")
    private String branchId;

    @NotBlank(message = "Branch name is required")
    private String branchName;

    private String branchCode;

    private String address1;

    private String gstNo;

    private String pincode;

    private String companyId;
}
