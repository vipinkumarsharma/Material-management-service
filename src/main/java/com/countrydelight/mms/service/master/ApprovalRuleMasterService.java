package com.countrydelight.mms.service.master;

import com.countrydelight.mms.dto.master.ApprovalRuleRequest;
import com.countrydelight.mms.entity.master.ApprovalRule;
import com.countrydelight.mms.exception.MmsException;
import com.countrydelight.mms.repository.master.ApprovalRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ApprovalRuleMasterService {

    private final ApprovalRuleRepository approvalRuleRepository;

    public Page<ApprovalRule> getAll(String txnType, String conditionType, int page, int size) {
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        if (StringUtils.hasText(txnType) && StringUtils.hasText(conditionType)) {
            return approvalRuleRepository.findByTxnTypeAndConditionType(txnType, conditionType, pageable);
        }
        if (StringUtils.hasText(txnType)) {
            return approvalRuleRepository.findByTxnType(txnType, pageable);
        }
        return approvalRuleRepository.findAll(pageable);
    }

    public ApprovalRule getById(String ruleId) {
        return approvalRuleRepository.findById(ruleId)
                .orElseThrow(() -> new MmsException("Approval rule not found: " + ruleId));
    }

    @Transactional
    public ApprovalRule create(ApprovalRuleRequest request) {
        if (approvalRuleRepository.existsById(request.getRuleId())) {
            throw new MmsException("Approval rule already exists: " + request.getRuleId());
        }
        ApprovalRule rule = ApprovalRule.builder()
                .ruleId(request.getRuleId())
                .txnType(request.getTxnType())
                .conditionType(request.getConditionType())
                .thresholdValue(request.getThresholdValue())
                .requiredRole(request.getRequiredRole())
                .build();
        return approvalRuleRepository.save(rule);
    }

    @Transactional
    public ApprovalRule update(String ruleId, ApprovalRuleRequest request) {
        ApprovalRule existing = getById(ruleId);
        existing.setTxnType(request.getTxnType());
        existing.setConditionType(request.getConditionType());
        existing.setThresholdValue(request.getThresholdValue());
        existing.setRequiredRole(request.getRequiredRole());
        return approvalRuleRepository.save(existing);
    }
}
