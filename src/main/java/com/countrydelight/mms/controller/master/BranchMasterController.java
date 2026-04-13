package com.countrydelight.mms.controller.master;

import com.countrydelight.mms.dto.common.ApiResponse;
import com.countrydelight.mms.dto.master.BranchRequest;
import com.countrydelight.mms.entity.master.BranchMaster;
import com.countrydelight.mms.service.master.BranchMasterService;
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
@RequestMapping("/api/v1/master/branches")
@RequiredArgsConstructor
public class BranchMasterController {

    private final BranchMasterService branchMasterService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BranchMaster>>> getAll(
            @RequestParam(required = false) String branchId,
            @RequestParam(required = false) String branchName,
            @RequestParam(required = false) String pincode,
            @RequestParam(required = false) String companyId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<BranchMaster> result = branchMasterService.getAll(branchId, branchName, pincode, companyId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result.getNumber() + 1, result.getTotalElements()));
    }

    @GetMapping("/{branchId}")
    public ResponseEntity<ApiResponse<BranchMaster>> getById(@PathVariable String branchId) {
        BranchMaster branch = branchMasterService.getById(branchId);
        return ResponseEntity.ok(ApiResponse.success(branch));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BranchMaster>> create(@Valid @RequestBody BranchRequest request) {
        BranchMaster created = branchMasterService.create(request);
        return ResponseEntity.ok(ApiResponse.success("Branch created successfully", created));
    }

    @PutMapping("/{branchId}")
    public ResponseEntity<ApiResponse<BranchMaster>> update(
            @PathVariable String branchId,
            @Valid @RequestBody BranchRequest request) {
        BranchMaster updated = branchMasterService.update(branchId, request);
        return ResponseEntity.ok(ApiResponse.success("Branch updated successfully", updated));
    }
}
