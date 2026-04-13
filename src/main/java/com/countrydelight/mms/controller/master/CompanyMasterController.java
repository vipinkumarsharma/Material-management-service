package com.countrydelight.mms.controller.master;

import com.countrydelight.mms.dto.common.ApiResponse;
import com.countrydelight.mms.dto.master.CompanyRequest;
import com.countrydelight.mms.entity.master.CompanyMaster;
import com.countrydelight.mms.service.master.CompanyMasterService;
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
@RequestMapping("/api/v1/master/companies")
@RequiredArgsConstructor
public class CompanyMasterController {

    private final CompanyMasterService companyMasterService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CompanyMaster>>> getAll(
            @RequestParam(required = false) String companyId,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<CompanyMaster> result = companyMasterService.getAll(companyId, name, page, size);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result.getNumber() + 1, result.getTotalElements()));
    }

    @GetMapping("/{companyId}")
    public ResponseEntity<ApiResponse<CompanyMaster>> getById(@PathVariable String companyId) {
        CompanyMaster company = companyMasterService.getById(companyId);
        return ResponseEntity.ok(ApiResponse.success(company));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CompanyMaster>> create(@Valid @RequestBody CompanyRequest request) {
        CompanyMaster created = companyMasterService.create(request);
        return ResponseEntity.ok(ApiResponse.success("Company created successfully", created));
    }

    @PutMapping("/{companyId}")
    public ResponseEntity<ApiResponse<CompanyMaster>> update(
            @PathVariable String companyId,
            @Valid @RequestBody CompanyRequest request) {
        CompanyMaster updated = companyMasterService.update(companyId, request);
        return ResponseEntity.ok(ApiResponse.success("Company updated successfully", updated));
    }
}
