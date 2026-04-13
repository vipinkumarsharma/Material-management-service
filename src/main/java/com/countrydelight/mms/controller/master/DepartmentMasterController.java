package com.countrydelight.mms.controller.master;

import com.countrydelight.mms.dto.common.ApiResponse;
import com.countrydelight.mms.dto.master.BranchDepartmentRequest;
import com.countrydelight.mms.dto.master.DepartmentRequest;
import com.countrydelight.mms.dto.master.DepartmentResponse;
import com.countrydelight.mms.entity.master.BranchDepartmentMap;
import com.countrydelight.mms.entity.master.DepartmentMaster;
import com.countrydelight.mms.service.master.DepartmentMasterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/v1/master/departments")
@RequiredArgsConstructor
@Tag(name = "Department Master", description = "Department CRUD and branch mapping APIs")
public class DepartmentMasterController {

    private final DepartmentMasterService departmentMasterService;

    @GetMapping
    @Operation(summary = "List all departments", description = "Returns all departments with mapped branches, optionally filtered by name, branchId, or deptId")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String branchId,
            @RequestParam(required = false) Integer deptId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        org.springframework.data.domain.Page<DepartmentResponse> result =
                departmentMasterService.getAllDepartments(name, branchId, deptId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result.getNumber() + 1, result.getTotalElements()));
    }

    @GetMapping("/{deptId}")
    @Operation(summary = "Get department by ID")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getById(@PathVariable Integer deptId) {
        DepartmentResponse dept = departmentMasterService.getDepartmentById(deptId);
        return ResponseEntity.ok(ApiResponse.success(dept));
    }

    @PostMapping
    @Operation(summary = "Create department", description = "Creates a new department with auto-generated ID")
    public ResponseEntity<ApiResponse<DepartmentMaster>> create(@Valid @RequestBody DepartmentRequest request) {
        DepartmentMaster created = departmentMasterService.createDepartment(request);
        return ResponseEntity.ok(ApiResponse.success("Department created successfully", created));
    }

    @PutMapping("/{deptId}")
    @Operation(summary = "Update department")
    public ResponseEntity<ApiResponse<DepartmentMaster>> update(
            @PathVariable Integer deptId,
            @Valid @RequestBody DepartmentRequest request) {
        DepartmentMaster updated = departmentMasterService.updateDepartment(deptId, request);
        return ResponseEntity.ok(ApiResponse.success("Department updated successfully", updated));
    }

    // ---- Branch-Department Mapping ----

    @GetMapping("/branch-mappings")
    @Operation(summary = "List branch-department mappings", description = "Filter by branchId or deptId")
    public ResponseEntity<ApiResponse<List<BranchDepartmentMap>>> getBranchMappings(
            @RequestParam(required = false) String branchId,
            @RequestParam(required = false) Integer deptId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        org.springframework.data.domain.Page<BranchDepartmentMap> result =
                departmentMasterService.getBranchDeptMappings(branchId, deptId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result.getNumber() + 1, result.getTotalElements()));
    }

    @PostMapping("/branch-mappings")
    @Operation(summary = "Map department to branch")
    public ResponseEntity<ApiResponse<BranchDepartmentMap>> mapToBranch(
            @Valid @RequestBody BranchDepartmentRequest request) {
        BranchDepartmentMap mapping = departmentMasterService.mapDeptToBranch(request);
        return ResponseEntity.ok(ApiResponse.success("Department mapped to branch successfully", mapping));
    }

    @DeleteMapping("/branch-mappings")
    @Operation(summary = "Unmap department from branch")
    public ResponseEntity<ApiResponse<Void>> unmapFromBranch(
            @RequestParam String branchId,
            @RequestParam Integer deptId) {
        departmentMasterService.unmapDeptFromBranch(branchId, deptId);
        return ResponseEntity.ok(ApiResponse.success("Department unmapped from branch successfully", null));
    }
}
