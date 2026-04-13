package com.countrydelight.mms.controller;

import com.countrydelight.mms.dto.common.ApiResponse;
import com.countrydelight.mms.dto.outward.IssueCreateRequest;
import com.countrydelight.mms.entity.outward.IssueHeader;
import com.countrydelight.mms.service.outward.IssueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/issue")
@RequiredArgsConstructor
public class IssueController {

    private final IssueService issueService;

    /**
     * Create a new issue in DRAFT status.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<IssueHeader>> createIssue(@Valid @RequestBody IssueCreateRequest request) {
        IssueHeader issue = issueService.createIssue(request);
        return ResponseEntity.ok(ApiResponse.success("Issue created successfully", issue));
    }

    /**
     * Post an issue - consumes stock using FIFO and updates ledger.
     */
    @PostMapping("/{issueId}/post")
    public ResponseEntity<ApiResponse<IssueHeader>> postIssue(
            @PathVariable Long issueId,
            @RequestParam String postedBy) {
        IssueHeader issue = issueService.postIssue(issueId, postedBy);
        return ResponseEntity.ok(ApiResponse.success("Issue posted successfully", issue));
    }

    /**
     * Get issue by ID.
     */
    @GetMapping("/{issueId}")
    public ResponseEntity<ApiResponse<IssueHeader>> getIssue(@PathVariable Long issueId) {
        IssueHeader issue = issueService.getIssue(issueId);
        return ResponseEntity.ok(ApiResponse.success(issue));
    }

    /**
     * Get issues by branch.
     */
    @GetMapping("/branch/{branchId}")
    public ResponseEntity<ApiResponse<List<IssueHeader>>> getIssuesByBranch(
            @PathVariable String branchId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<IssueHeader> result = issueService.getIssuesByBranch(branchId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result.getNumber() + 1, result.getTotalElements()));
    }

    /**
     * Check stock availability for issue.
     */
    @GetMapping("/check-stock")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkStockAvailability(
            @RequestParam String branchId,
            @RequestParam String itemId,
            @RequestParam String locationId,
            @RequestParam BigDecimal qty) {
        boolean available = issueService.checkStockAvailability(branchId, itemId, locationId, qty);
        Map<String, Object> result = Map.of(
                "available", available,
                "branchId", branchId,
                "itemId", itemId,
                "locationId", locationId,
                "requestedQty", qty
        );
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
