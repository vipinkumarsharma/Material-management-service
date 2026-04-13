package com.countrydelight.mms.controller.master;

import com.countrydelight.mms.dto.common.ApiResponse;
import com.countrydelight.mms.dto.master.LocationRequest;
import com.countrydelight.mms.entity.master.LocationMaster;
import com.countrydelight.mms.service.master.LocationMasterService;
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
@RequestMapping("/api/v1/master/locations")
@RequiredArgsConstructor
public class LocationMasterController {

    private final LocationMasterService locationMasterService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<LocationMaster>>> getAll(
            @RequestParam(required = false) String branchId,
            @RequestParam(required = false) String parentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<LocationMaster> result = locationMasterService.getAll(branchId, parentId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result.getNumber() + 1, result.getTotalElements()));
    }

    @GetMapping("/{locationId}")
    public ResponseEntity<ApiResponse<LocationMaster>> getById(@PathVariable String locationId) {
        LocationMaster location = locationMasterService.getById(locationId);
        return ResponseEntity.ok(ApiResponse.success(location));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LocationMaster>> create(@Valid @RequestBody LocationRequest request) {
        LocationMaster created = locationMasterService.create(request);
        return ResponseEntity.ok(ApiResponse.success("Location created successfully", created));
    }

    @PutMapping("/{locationId}")
    public ResponseEntity<ApiResponse<LocationMaster>> update(
            @PathVariable String locationId,
            @Valid @RequestBody LocationRequest request) {
        LocationMaster updated = locationMasterService.update(locationId, request);
        return ResponseEntity.ok(ApiResponse.success("Location updated successfully", updated));
    }
}
