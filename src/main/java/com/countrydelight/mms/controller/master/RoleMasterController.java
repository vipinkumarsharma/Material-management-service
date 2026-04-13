package com.countrydelight.mms.controller.master;

import com.countrydelight.mms.dto.common.ApiResponse;
import com.countrydelight.mms.dto.master.RoleRequest;
import com.countrydelight.mms.dto.master.UserRoleRequest;
import com.countrydelight.mms.entity.master.RoleMaster;
import com.countrydelight.mms.entity.master.UserRoleMap;
import com.countrydelight.mms.service.master.RoleMasterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
@RequestMapping("/api/v1/master/roles")
@RequiredArgsConstructor
public class RoleMasterController {

    private final RoleMasterService roleMasterService;

    // ---- Role endpoints ----

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleMaster>>> getAllRoles(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<RoleMaster> result = roleMasterService.getAllRoles(name, page, size);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result.getNumber() + 1, result.getTotalElements()));
    }

    @GetMapping("/{roleId}")
    public ResponseEntity<ApiResponse<RoleMaster>> getRoleById(@PathVariable String roleId) {
        RoleMaster role = roleMasterService.getRoleById(roleId);
        return ResponseEntity.ok(ApiResponse.success(role));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoleMaster>> createRole(@Valid @RequestBody RoleRequest request) {
        RoleMaster created = roleMasterService.createRole(request);
        return ResponseEntity.ok(ApiResponse.success("Role created successfully", created));
    }

    @PutMapping("/{roleId}")
    public ResponseEntity<ApiResponse<RoleMaster>> updateRole(
            @PathVariable String roleId,
            @Valid @RequestBody RoleRequest request) {
        RoleMaster updated = roleMasterService.updateRole(roleId, request);
        return ResponseEntity.ok(ApiResponse.success("Role updated successfully", updated));
    }

    // ---- User-Role mapping endpoints ----

    @GetMapping("/user-roles")
    public ResponseEntity<ApiResponse<List<UserRoleMap>>> getUserRoles(
            @RequestParam(required = false) String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<UserRoleMap> result = roleMasterService.getUserRoles(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result.getNumber() + 1, result.getTotalElements()));
    }

    @PostMapping("/user-roles")
    public ResponseEntity<ApiResponse<UserRoleMap>> assignRole(@Valid @RequestBody UserRoleRequest request) {
        UserRoleMap created = roleMasterService.assignRole(request);
        return ResponseEntity.ok(ApiResponse.success("Role assigned successfully", created));
    }

    @DeleteMapping("/user-roles")
    public ResponseEntity<ApiResponse<Void>> removeRole(
            @RequestParam String userId,
            @RequestParam String roleId) {
        roleMasterService.removeRole(userId, roleId);
        return ResponseEntity.ok(ApiResponse.success("Role removed successfully", null));
    }
}
