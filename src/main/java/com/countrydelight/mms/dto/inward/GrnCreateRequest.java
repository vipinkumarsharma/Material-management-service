package com.countrydelight.mms.dto.inward;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrnCreateRequest {

    @NotBlank(message = "Branch ID is required")
    private String branchId;

    private String voucherTypeId;
    private String voucherNumber;

    @NotBlank(message = "Supplier ID is required")
    private String suppId;

    private Integer deptId; // Optional - Department reference

    private Long poId; // Optional - Purchase Voucher reference

    private Long invoiceId; // Optional - Invoice reference

    private String challanNo;

    private LocalDate challanDate;

    private LocalDate invoiceDate;

    @NotNull(message = "GRN date is required")
    private LocalDate grnDate;

    private String remarks;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<GrnDetailRequest> details;

    private BigDecimal itemsCount;
    private BigDecimal totalQty;
    private BigDecimal grossAmount;
    private BigDecimal netAmount;
    private BigDecimal roundOffAmount;

    private String createdBy;
}
