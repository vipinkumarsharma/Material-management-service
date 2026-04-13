package com.countrydelight.mms.controller.master;

import com.countrydelight.mms.dto.common.ApiResponse;
import com.countrydelight.mms.dto.master.UnitRequest;
import com.countrydelight.mms.entity.master.UnitMaster;
import com.countrydelight.mms.service.master.UnitMasterService;
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
@RequestMapping("/api/v1/master/units")
@RequiredArgsConstructor
public class UnitMasterController {

    private final UnitMasterService unitMasterService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UnitMaster>>> getAll(
            @RequestParam(required = false) String desc,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<UnitMaster> result = unitMasterService.getAll(desc, page, size);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result.getNumber() + 1, result.getTotalElements()));
    }

    @GetMapping("/{unitId}")
    public ResponseEntity<ApiResponse<UnitMaster>> getById(@PathVariable String unitId) {
        UnitMaster unit = unitMasterService.getById(unitId);
        return ResponseEntity.ok(ApiResponse.success(unit));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UnitMaster>> create(@Valid @RequestBody UnitRequest request) {
        UnitMaster created = unitMasterService.create(request);
        return ResponseEntity.ok(ApiResponse.success("Unit created successfully", created));
    }

    @PutMapping("/{unitId}")
    public ResponseEntity<ApiResponse<UnitMaster>> update(
            @PathVariable String unitId,
            @Valid @RequestBody UnitRequest request) {
        UnitMaster updated = unitMasterService.update(unitId, request);
        return ResponseEntity.ok(ApiResponse.success("Unit updated successfully", updated));
    }
}
