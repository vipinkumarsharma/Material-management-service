package com.countrydelight.mms.entity.master;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "voucher_type_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherTypeMaster {

    @Id
    @Column(name = "voucher_type_id", length = 20)
    private String voucherTypeId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "voucher_type_branch_map", joinColumns = @JoinColumn(name = "voucher_type_id"))
    @Column(name = "branch_id", length = 20)
    @Builder.Default
    private List<String> branchIds = new ArrayList<>();

    @Column(name = "voucher_type_name", nullable = false, length = 100)
    private String voucherTypeName;

    @Column(name = "alias", length = 50)
    private String alias;

    @Column(name = "voucher_category", nullable = false, length = 30)
    private String voucherCategory;

    @Column(name = "abbreviation", length = 50)
    private String abbreviation;

    @Column(name = "set_alter_numbering", length = 10)
    @Builder.Default
    private String setAlterNumbering = "No";

    @Column(name = "default_jurisdiction", length = 100)
    private String defaultJurisdiction;

    @Column(name = "default_title_to_print", length = 200)
    private String defaultTitleToPrint;

    @Column(name = "set_alter_declaration")
    @Builder.Default
    private boolean setAlterDeclaration = false;

    @Column(name = "enable_default_alloc")
    @Builder.Default
    private boolean enableDefaultAllocations = false;

    @Column(name = "whatsapp_after_saving")
    @Builder.Default
    private boolean whatsappAfterSaving = false;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    @Column(name = "numbering_method", length = 30)
    @Builder.Default
    private String numberingMethod = "AUTOMATIC";

    @Column(name = "numbering_on_deletion", length = 30)
    @Builder.Default
    private String numberingOnDeletion = "RETAIN_ORIGINAL";

    @Column(name = "show_unused_numbers")
    @Builder.Default
    private boolean showUnusedNumbers = false;

    @Column(name = "prevent_duplicates")
    @Builder.Default
    private boolean preventDuplicates = true;

    @Column(name = "allow_zero_valued")
    @Builder.Default
    private boolean allowZeroValued = false;

    @Column(name = "is_optional_default")
    @Builder.Default
    private boolean optionalDefault = false;

    @Column(name = "use_effective_dates")
    @Builder.Default
    private boolean useEffectiveDates = false;

    @Column(name = "effective_date_label", length = 50)
    @Builder.Default
    private String effectiveDateLabel = "Effective Date";

    @Column(name = "allow_narration")
    @Builder.Default
    private boolean allowNarration = true;

    @Column(name = "narration_mandatory")
    @Builder.Default
    private boolean narrationMandatory = false;

    @Column(name = "narration_per_line")
    @Builder.Default
    private boolean narrationPerLine = false;

    @Column(name = "print_after_save")
    @Builder.Default
    private boolean printAfterSave = false;

    @Column(name = "require_approval")
    @Builder.Default
    private boolean requireApproval = false;

    @Column(name = "approval_amount_limit", precision = 15, scale = 2)
    private BigDecimal approvalAmountLimit;

    @Column(name = "use_for_job_work")
    @Builder.Default
    private boolean useForJobWork = false;

    @Column(name = "use_for_job_work_in")
    @Builder.Default
    private boolean useForJobWorkIn = false;

    @Column(name = "is_department")
    @Builder.Default
    private boolean department = false;

    @Column(name = "is_supplier")
    @Builder.Default
    private boolean supplier = false;

    @Column(name = "report_summary_title", length = 200)
    private String reportSummaryTitle;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    /** Not persisted — populated by service after embedded series creation */
    @Transient
    private List<VoucherSeriesMaster> voucherSeries;
}
