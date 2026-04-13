package com.countrydelight.mms.controller.master;

import com.countrydelight.mms.dto.common.ApiResponse;
import com.countrydelight.mms.dto.master.VoucherTypeRequest;
import com.countrydelight.mms.dto.master.VoucherTypeResponse;
import com.countrydelight.mms.entity.master.VoucherTypeMaster;
import com.countrydelight.mms.service.master.VoucherTypeService;
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
@RequestMapping("/api/v1/master/voucher-types")
@RequiredArgsConstructor
public class VoucherTypeController {

    private final VoucherTypeService voucherTypeService;

    @PostMapping
    public ResponseEntity<ApiResponse<VoucherTypeResponse>> create(
            @Valid @RequestBody VoucherTypeRequest request) {
        VoucherTypeMaster vt = voucherTypeService.create(request);
        return ResponseEntity.ok(ApiResponse.success("Voucher type created", VoucherTypeResponse.from(vt)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VoucherTypeResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody VoucherTypeRequest request) {
        VoucherTypeMaster vt = voucherTypeService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Voucher type updated", VoucherTypeResponse.from(vt)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VoucherTypeResponse>>> list(
            @RequestParam(required = false) String voucherCategory,
            @RequestParam(required = false) String branchId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<VoucherTypeMaster> result = voucherTypeService.list(voucherCategory, branchId, page, size);
        List<VoucherTypeResponse> content = result.getContent().stream().map(VoucherTypeResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.success(content, result.getNumber() + 1, result.getTotalElements()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VoucherTypeResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(VoucherTypeResponse.from(voucherTypeService.getById(id))));
    }
}
