package com.countrydelight.mms.service.approval;

import com.countrydelight.mms.dto.inward.PriceVarianceInfo;
import com.countrydelight.mms.entity.master.ApprovalRule;
import com.countrydelight.mms.repository.master.ApprovalRuleRepository;
import com.countrydelight.mms.repository.master.UserRoleMapRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalRuleRepository approvalRuleRepository;
    private final UserRoleMapRepository userRoleMapRepository;

    /**
     * Check if price variance requires approval.
     *
     * @param txnType       Transaction type (GRN, ISSUE, TRANSFER)
     * @param lastRate      Last GRN rate from ledger
     * @param newRate       New rate being entered
     * @param itemId        Item ID
     * @param itemDesc      Item description
     * @return PriceVarianceInfo with approval requirement details
     */
    public PriceVarianceInfo checkPriceVariance(String txnType, BigDecimal lastRate,
                                                  BigDecimal newRate, String itemId, String itemDesc) {
        if (lastRate == null || lastRate.compareTo(BigDecimal.ZERO) == 0) {
            // No historical price, no variance check needed
            return PriceVarianceInfo.builder()
                    .itemId(itemId)
                    .itemDesc(itemDesc)
                    .lastGrnRate(lastRate)
                    .newRate(newRate)
                    .varianceAmount(BigDecimal.ZERO)
                    .variancePercentage(BigDecimal.ZERO)
                    .requiresApproval(false)
                    .build();
        }

        BigDecimal varianceAmount = newRate.subtract(lastRate);
        BigDecimal variancePercentage = varianceAmount
                .divide(lastRate, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        // Get approval rules for price variance
        List<ApprovalRule> rules = approvalRuleRepository
                .findByTxnTypeAndConditionType(txnType, "PRICE_VARIANCE", Pageable.unpaged()).getContent();

        String requiredRole = null;
        boolean requiresApproval = false;

        for (ApprovalRule rule : rules) {
            // Check if variance exceeds threshold (can be positive or negative)
            if (variancePercentage.abs().compareTo(rule.getThresholdValue()) >= 0) {
                requiresApproval = true;
                requiredRole = rule.getRequiredRole();
                break;
            }
        }

        return PriceVarianceInfo.builder()
                .itemId(itemId)
                .itemDesc(itemDesc)
                .lastGrnRate(lastRate)
                .newRate(newRate)
                .varianceAmount(varianceAmount)
                .variancePercentage(variancePercentage)
                .requiresApproval(requiresApproval)
                .requiredRole(requiredRole)
                .build();
    }

    /**
     * Check if user has approval authority for the required role.
     */
    public boolean hasApprovalAuthority(String userId, String requiredRole) {
        if (requiredRole == null) {
            return true;
        }
        Set<String> userRoles = userRoleMapRepository.findRoleIdsByUserId(userId);
        return userRoles.contains(requiredRole);
    }

    /**
     * Check if user can approve the transaction based on variance info.
     */
    public boolean canApprove(String userId, List<PriceVarianceInfo> variances) {
        for (PriceVarianceInfo variance : variances) {
            if (variance.isRequiresApproval()) {
                if (!hasApprovalAuthority(userId, variance.getRequiredRole())) {
                    return false;
                }
            }
        }
        return true;
    }
}
