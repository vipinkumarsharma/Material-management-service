package com.countrydelight.mms.dto.master;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class VoucherTypeRequest {

    @Size(max = 20)
    private String voucherTypeId;

    private List<String> branchIds;

    @NotBlank
    @Size(max = 100)
    private String voucherTypeName;

    @Size(max = 50)
    private String alias;

    @NotBlank
    @Size(max = 30)
    private String voucherCategory;

    @Size(max = 10)
    private String abbreviation;

    private String setAlterNumbering = "No";
    private String defaultJurisdiction;
    private String defaultTitleToPrint;
    private boolean setAlterDeclaration = false;
    private boolean enableDefaultAllocations = false;
    private boolean whatsappAfterSaving = false;

    private boolean active = true;

    private String numberingMethod = "AUTOMATIC";
    private String numberingOnDeletion = "RETAIN_ORIGINAL";
    private boolean showUnusedNumbers = false;
    private boolean preventDuplicates = true;

    private boolean allowZeroValued = false;
    private boolean optionalDefault = false;
    private boolean useEffectiveDates = false;
    private String effectiveDateLabel = "Effective Date";

    private boolean allowNarration = true;
    private boolean narrationMandatory = false;
    private boolean narrationPerLine = false;

    private boolean printAfterSave = false;

    private boolean requireApproval = false;
    private BigDecimal approvalAmountLimit;

    private boolean useForJobWork = false;
    private boolean useForJobWorkIn = false;

    @JsonProperty("isDepartment")
    private boolean department = false;

    @JsonProperty("isSupplier")
    private boolean supplier = false;

    @Size(max = 200)
    private String reportSummaryTitle;

    private List<VoucherSeriesRequest> voucherSeries;
}
