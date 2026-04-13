package com.countrydelight.mms.controller;

import com.countrydelight.mms.dto.common.ApiResponse;
import com.countrydelight.mms.dto.transfer.DeptTransferCreateRequest;
import com.countrydelight.mms.entity.transfer.DeptTransferHeader;
import com.countrydelight.mms.service.transfer.DeptTransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/dept-transfer")
@RequiredArgsConstructor
@Tag(name = "Department Transfer", description = "Intra-branch department stock transfer APIs")
public class DeptTransferController {

    private final DeptTransferService deptTransferService;

    @PostMapping
    @Operation(summary = "Create and post department transfer",
            description = "Creates an intra-branch department transfer. Auto-posted on creation (no approval flow). " +
                    "Records paired DEPT_TRANSFER_OUT/IN ledger entries for departmental cost attribution.")
    public ResponseEntity<ApiResponse<DeptTransferHeader>> create(
            @Valid @RequestBody DeptTransferCreateRequest request) {
        DeptTransferHeader created = deptTransferService.createAndPost(request);
        return ResponseEntity.ok(ApiResponse.success("Department transfer created and posted successfully", created));
    }

    @GetMapping
    @Operation(summary = "List department transfers by branch and department")
    public ResponseEntity<ApiResponse<List<DeptTransferHeader>>> getByBranchAndDept(
            @RequestParam(required = false) String branchId,
            @RequestParam(required = false) Integer deptId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<DeptTransferHeader> result = deptTransferService.getByBranch(branchId, deptId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result.getNumber() + 1, result.getTotalElements()));
    }

    @GetMapping("/branch/{branchId}")
    @Operation(summary = "List department transfers by branch", description = "Optionally filter by department ID")
    public ResponseEntity<ApiResponse<List<DeptTransferHeader>>> getByBranch(
            @PathVariable String branchId,
            @RequestParam(required = false) Integer deptId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<DeptTransferHeader> result = deptTransferService.getByBranch(branchId, deptId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result.getNumber() + 1, result.getTotalElements()));
    }
}
