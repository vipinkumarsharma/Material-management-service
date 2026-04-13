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
public class LocationRequest {

    @NotBlank(message = "Location ID is required")
    private String locationId;

    @NotBlank(message = "Branch ID is required")
    private String branchId;

    @NotBlank(message = "Location name is required")
    private String locationName;

    private String parentId;
}
