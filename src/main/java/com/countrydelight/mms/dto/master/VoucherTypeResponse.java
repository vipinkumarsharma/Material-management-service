package com.countrydelight.mms.dto.master;

import com.countrydelight.mms.entity.master.VoucherTypeMaster;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class VoucherTypeResponse {

    private String voucherTypeId;
    private List<String> branchIds;
    private String voucherTypeName;
    private String alias;
    private String voucherCategory;
    private String abbreviation;
    private boolean active;

    private String setAlterNumbering;
    private String defaultJurisdiction;
    private String defaultTitleToPrint;
    private boolean setAlterDeclaration;
    private boolean enableDefaultAllocations;
    private boolean whatsappAfterSaving;

    private String numberingMethod;
    private String numberingOnDeletion;
    private boolean showUnusedNumbers;
    private boolean preventDuplicates;

    private boolean allowZeroValued;
    private boolean optionalDefault;
    private boolean useEffectiveDates;
    private String effectiveDateLabel;

    private boolean allowNarration;
    private boolean narrationMandatory;
    private boolean narrationPerLine;

    private boolean printAfterSave;
    private boolean requireApproval;
    private BigDecimal approvalAmountLimit;

    private boolean useForJobWork;
    private boolean useForJobWorkIn;

    // New response flags
    @JsonProperty("isDepartment")
    private boolean department;

    @JsonProperty("isSupplier")
    private boolean supplier;

    private String reportSummaryTitle;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<VoucherSeriesResponse> voucherSeries;

    public static VoucherTypeResponse from(VoucherTypeMaster e) {
        List<VoucherSeriesResponse> series = null;
        if (e.getVoucherSeries() != null && !e.getVoucherSeries().isEmpty()) {
            series = e.getVoucherSeries().stream().map(VoucherSeriesResponse::from).toList();
        }
        return VoucherTypeResponse.builder()
                .voucherTypeId(e.getVoucherTypeId())
                .branchIds(e.getBranchIds())
                .voucherTypeName(e.getVoucherTypeName())
                .alias(e.getAlias())
                .voucherCategory(e.getVoucherCategory())
                .abbreviation(e.getAbbreviation())
                .active(e.isActive())
                .setAlterNumbering(e.getSetAlterNumbering())
                .defaultJurisdiction(e.getDefaultJurisdiction())
                .defaultTitleToPrint(e.getDefaultTitleToPrint())
                .setAlterDeclaration(e.isSetAlterDeclaration())
                .enableDefaultAllocations(e.isEnableDefaultAllocations())
                .whatsappAfterSaving(e.isWhatsappAfterSaving())
                .numberingMethod(e.getNumberingMethod())
                .numberingOnDeletion(e.getNumberingOnDeletion())
                .showUnusedNumbers(e.isShowUnusedNumbers())
                .preventDuplicates(e.isPreventDuplicates())
                .allowZeroValued(e.isAllowZeroValued())
                .optionalDefault(e.isOptionalDefault())
                .useEffectiveDates(e.isUseEffectiveDates())
                .effectiveDateLabel(e.getEffectiveDateLabel())
                .allowNarration(e.isAllowNarration())
                .narrationMandatory(e.isNarrationMandatory())
                .narrationPerLine(e.isNarrationPerLine())
                .printAfterSave(e.isPrintAfterSave())
                .requireApproval(e.isRequireApproval())
                .approvalAmountLimit(e.getApprovalAmountLimit())
                .useForJobWork(e.isUseForJobWork())
                .useForJobWorkIn(e.isUseForJobWorkIn())
                .department(e.isDepartment())
                .supplier(e.isSupplier())
                .reportSummaryTitle(e.getReportSummaryTitle())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .voucherSeries(series)
                .build();
    }
}
