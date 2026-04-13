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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// supplierData is required when transferMode == "THIRD_PARTY"

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeptTransferCreateRequest {

    @NotBlank(message = "From branch ID is required")
    private String fromBranchId;

    private String toBranchId;

    private String voucherTypeId;
    private String voucherNumber;

    private String transferCategory;
    private String transferType;
    private Long transferOutId;
    private String transferMode;

    @NotNull(message = "From department ID is required")
    private Integer fromDeptId;

    private Integer toDeptId;

    @NotNull(message = "Transfer date is required")
    private LocalDate transferDate;

    private String remarks;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<DeptTransferDetailRequest> details;

    private String createdBy;

    private BigDecimal itemsCount;
    private BigDecimal totalQty;
    private BigDecimal grossAmount;
    private BigDecimal netAmount;
    private BigDecimal roundingAmount;
    private String roundingType;
    private String thirdPartySupplierId;

    @Valid
    private ThirdPartySupplierRequest supplierData;
}
