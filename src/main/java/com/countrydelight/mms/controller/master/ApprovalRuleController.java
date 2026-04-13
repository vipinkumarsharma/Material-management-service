package com.countrydelight.mms.controller.master;

import com.countrydelight.mms.dto.common.ApiResponse;
import com.countrydelight.mms.dto.master.ApprovalRuleRequest;
import com.countrydelight.mms.entity.master.ApprovalRule;
import com.countrydelight.mms.service.master.ApprovalRuleMasterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/master/approval-rules")
@RequiredArgsConstructor
public class ApprovalRuleController {

    private final ApprovalRuleMasterService approvalRuleMasterService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ApprovalRule>>> getAll(
            @RequestParam(required = false) String txnType,
            @RequestParam(required = false) String conditionType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ApprovalRule> result = approvalRuleMasterService.getAll(txnType, conditionType, page, size);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result.getNumber() + 1, result.getTotalElements()));
    }

    @GetMapping("/{ruleId}")
    public ResponseEntity<ApiResponse<ApprovalRule>> getById(@PathVariable String ruleId) {
        ApprovalRule rule = approvalRuleMasterService.getById(ruleId);
        return ResponseEntity.ok(ApiResponse.success(rule));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ApprovalRule>> create(@Valid @RequestBody ApprovalRuleRequest request) {
        ApprovalRule created = approvalRuleMasterService.create(request);
        return ResponseEntity.ok(ApiResponse.success("Approval rule created successfully", created));
    }

    @PutMapping("/{ruleId}")
    public ResponseEntity<ApiResponse<ApprovalRule>> update(
            @PathVariable String ruleId,
            @Valid @RequestBody ApprovalRuleRequest request) {
        ApprovalRule updated = approvalRuleMasterService.update(ruleId, request);
        return ResponseEntity.ok(ApiResponse.success("Approval rule updated successfully", updated));
    }
}
