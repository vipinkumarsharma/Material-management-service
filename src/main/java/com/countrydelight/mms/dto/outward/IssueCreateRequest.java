package com.countrydelight.mms.dto.outward;

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
public class IssueCreateRequest {

    @NotBlank(message = "Branch ID is required")
    private String branchId;

    private String voucherTypeId;
    private String voucherNumber;

    private Integer deptId; // Optional - Department reference

    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;

    @NotBlank(message = "Issued to is required")
    private String issuedTo;

    private String issueType; // REGULAR (default) or JOB_WORK

    private String suppId; // Required for JOB_WORK

    private LocalDate expectedReturnDate; // Optional, for JOB_WORK

    private String remarks;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<IssueDetailRequest> details;

    private String createdBy;
}
