package com.countrydelight.mms.controller;

import com.countrydelight.mms.dto.common.ApiResponse;
import com.countrydelight.mms.dto.inward.GrnCreateRequest;
import com.countrydelight.mms.dto.inward.PriceSuggestionResponse;
import com.countrydelight.mms.entity.inward.GrnHeader;
import com.countrydelight.mms.service.inward.GrnService;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/grn")
@RequiredArgsConstructor
public class GrnController {

    private final GrnService grnService;

    /**
     * Get price suggestion for an item at a branch.
     * Returns the last GRN rate from ledger (primary source for pricing).
     */
    @GetMapping("/price-suggestion")
    public ResponseEntity<ApiResponse<PriceSuggestionResponse>> getPriceSuggestion(
            @RequestParam String branchId,
            @RequestParam String itemId) {
        PriceSuggestionResponse suggestion = grnService.getPriceSuggestion(branchId, itemId);
        return ResponseEntity.ok(ApiResponse.success(suggestion));
    }

    /**
     * Create a new GRN in DRAFT status.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<GrnHeader>> createGrn(@Valid @RequestBody GrnCreateRequest request) {
        GrnHeader grn = grnService.createGrn(request);
        return ResponseEntity.ok(ApiResponse.success("GRN created successfully", grn));
    }

    /**
     * Submit GRN for approval. Will auto-post if no approval is required.
     */
    @PostMapping("/{grnId}/submit")
    public ResponseEntity<ApiResponse<GrnHeader>> submitGrn(
            @PathVariable Long grnId,
            @RequestParam String submittedBy) {
        GrnHeader grn = grnService.submitForApproval(grnId, submittedBy);
        return ResponseEntity.ok(ApiResponse.success("GRN submitted successfully", grn));
    }

    /**
     * Approve and post a GRN that requires approval.
     */
    @PostMapping("/{grnId}/approve")
    public ResponseEntity<ApiResponse<GrnHeader>> approveGrn(
            @PathVariable Long grnId,
            @RequestParam String approvedBy) {
        GrnHeader grn = grnService.approveAndPost(grnId, approvedBy);
        return ResponseEntity.ok(ApiResponse.success("GRN approved and posted successfully", grn));
    }

    /**
     * Get GRNs by optional grnId, branchId, and/or pvId filters.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<GrnHeader>>> getGrns(
            @RequestParam(required = false) Long grnId,
            @RequestParam(required = false) String branchId,
            @RequestParam(required = false) Long pvId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<GrnHeader> result = grnService.getGrns(grnId, branchId, pvId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result.getNumber() + 1, result.getTotalElements()));
    }

    /**
     * Get GRNs by branch.
     */
    @GetMapping("/branch/{branchId}")
    public ResponseEntity<ApiResponse<List<GrnHeader>>> getGrnsByBranch(
            @PathVariable String branchId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<GrnHeader> result = grnService.getGrnsByBranch(branchId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result.getNumber() + 1, result.getTotalElements()));
    }

    /**
     * Get GRNs pending approval.
     */
    @GetMapping("/pending-approval")
    public ResponseEntity<ApiResponse<List<GrnHeader>>> getPendingApprovalGrns(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<GrnHeader> result = grnService.getPendingApprovalGrns(page, size);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result.getNumber() + 1, result.getTotalElements()));
    }
}
