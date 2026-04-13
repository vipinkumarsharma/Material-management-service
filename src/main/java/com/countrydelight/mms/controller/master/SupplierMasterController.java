package com.countrydelight.mms.controller.master;

import com.countrydelight.mms.dto.common.ApiResponse;
import com.countrydelight.mms.dto.master.SupplierRequest;
import com.countrydelight.mms.entity.master.SupplierMaster;
import com.countrydelight.mms.service.master.SupplierMasterService;
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
@RequestMapping("/api/v1/master/suppliers")
@RequiredArgsConstructor
public class SupplierMasterController {

    private final SupplierMasterService supplierMasterService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SupplierMaster>>> getAll(
            @RequestParam(required = false) String suppId,
            @RequestParam(required = false) String suppName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<SupplierMaster> result = supplierMasterService.getAll(suppId, suppName, page, size);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result.getNumber() + 1, result.getTotalElements()));
    }

    @GetMapping("/{suppId}")
    public ResponseEntity<ApiResponse<SupplierMaster>> getById(@PathVariable String suppId) {
        SupplierMaster supplier = supplierMasterService.getById(suppId);
        return ResponseEntity.ok(ApiResponse.success(supplier));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SupplierMaster>> create(@Valid @RequestBody SupplierRequest request) {
        SupplierMaster created = supplierMasterService.create(request);
        return ResponseEntity.ok(ApiResponse.success("Supplier created successfully", created));
    }

    @PutMapping("/{suppId}")
    public ResponseEntity<ApiResponse<SupplierMaster>> update(
            @PathVariable String suppId,
            @Valid @RequestBody SupplierRequest request) {
        SupplierMaster updated = supplierMasterService.update(suppId, request);
        return ResponseEntity.ok(ApiResponse.success("Supplier updated successfully", updated));
    }
}
