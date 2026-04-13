package com.countrydelight.mms.dto.master;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalRuleRequest {

    @NotBlank(message = "Rule ID is required")
    private String ruleId;

    @NotBlank(message = "Transaction type is required")
    private String txnType;

    @NotBlank(message = "Condition type is required")
    private String conditionType;

    @NotNull(message = "Threshold value is required")
    private BigDecimal thresholdValue;

    @NotBlank(message = "Required role is required")
    private String requiredRole;
}
