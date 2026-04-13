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
public class SubGroupRequest {

    @NotBlank(message = "Sub-group ID is required")
    private String subGroupId;

    @NotBlank(message = "Sub-group description is required")
    private String subGroupDesc;
}
