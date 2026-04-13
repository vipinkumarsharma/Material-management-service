package com.countrydelight.mms.dto.transfer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferCreateRequest {

    @NotBlank(message = "Source branch is required")
    private String fromBranch;

    @NotBlank(message = "Destination branch is required")
    private String toBranch;

    private String voucherTypeId;
    private String voucherNumber;

    @NotNull(message = "Transfer date is required")
    private LocalDate transferDate;

    private Integer deptId; // Optional - Department reference

    private String remarks;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<TransferDetailRequest> details;

    private String createdBy;
}
