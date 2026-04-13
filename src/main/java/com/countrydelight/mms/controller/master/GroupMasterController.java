package com.countrydelight.mms.controller.master;

import com.countrydelight.mms.dto.common.ApiResponse;
import com.countrydelight.mms.dto.master.GroupRequest;
import com.countrydelight.mms.dto.master.SubGroupRequest;
import com.countrydelight.mms.entity.master.GroupMaster;
import com.countrydelight.mms.entity.master.SubGroupMaster;
import com.countrydelight.mms.service.master.GroupMasterService;
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
@RequestMapping("/api/v1/master/groups")
@RequiredArgsConstructor
public class GroupMasterController {

    private final GroupMasterService groupMasterService;

    // ---- Group endpoints ----

    @GetMapping
    public ResponseEntity<ApiResponse<List<GroupMaster>>> getAllGroups(
            @RequestParam(required = false) String groupId,
            @RequestParam(required = false) String groupDesc,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<GroupMaster> result = groupMasterService.getAllGroups(groupId, groupDesc, page, size);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result.getNumber() + 1, result.getTotalElements()));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupMaster>> getGroupById(@PathVariable String groupId) {
        GroupMaster group = groupMasterService.getGroupById(groupId);
        return ResponseEntity.ok(ApiResponse.success(group));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GroupMaster>> createGroup(@Valid @RequestBody GroupRequest request) {
        GroupMaster created = groupMasterService.createGroup(request);
        return ResponseEntity.ok(ApiResponse.success("Group created successfully", created));
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupMaster>> updateGroup(
            @PathVariable String groupId,
            @Valid @RequestBody GroupRequest request) {
        GroupMaster updated = groupMasterService.updateGroup(groupId, request);
        return ResponseEntity.ok(ApiResponse.success("Group updated successfully", updated));
    }

    // ---- SubGroup endpoints ----

    @GetMapping("/{groupId}/sub-groups")
    public ResponseEntity<ApiResponse<List<SubGroupMaster>>> getSubGroups(
            @PathVariable String groupId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<SubGroupMaster> result = groupMasterService.getSubGroupsByGroup(groupId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result.getNumber() + 1, result.getTotalElements()));
    }

    @GetMapping("/{groupId}/sub-groups/{subGroupId}")
    public ResponseEntity<ApiResponse<SubGroupMaster>> getSubGroupById(
            @PathVariable String groupId,
            @PathVariable String subGroupId) {
        SubGroupMaster subGroup = groupMasterService.getSubGroupById(groupId, subGroupId);
        return ResponseEntity.ok(ApiResponse.success(subGroup));
    }

    @PostMapping("/{groupId}/sub-groups")
    public ResponseEntity<ApiResponse<SubGroupMaster>> createSubGroup(
            @PathVariable String groupId,
            @Valid @RequestBody SubGroupRequest request) {
        SubGroupMaster created = groupMasterService.createSubGroup(groupId, request);
        return ResponseEntity.ok(ApiResponse.success("SubGroup created successfully", created));
    }

    @PutMapping("/{groupId}/sub-groups/{subGroupId}")
    public ResponseEntity<ApiResponse<SubGroupMaster>> updateSubGroup(
            @PathVariable String groupId,
            @PathVariable String subGroupId,
            @Valid @RequestBody SubGroupRequest request) {
        SubGroupMaster updated = groupMasterService.updateSubGroup(groupId, subGroupId, request);
        return ResponseEntity.ok(ApiResponse.success("SubGroup updated successfully", updated));
    }
}
