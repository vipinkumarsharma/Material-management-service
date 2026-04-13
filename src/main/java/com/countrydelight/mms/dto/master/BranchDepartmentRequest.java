package com.countrydelight.mms.dto.master;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class BranchDepartmentRequest {

    @NotBlank(message = "Branch ID is required")
    private String branchId;

    @NotNull(message = "Department ID is required")
    private Integer deptId;
}
