package com.countrydelight.mms.controller;

import com.countrydelight.mms.dto.audit.VoucherEditLogResponse;
import com.countrydelight.mms.dto.common.ApiResponse;
import com.countrydelight.mms.service.audit.VoucherEditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final VoucherEditLogService editLogService;

    @GetMapping("/{entityType}/{entityId}")
    public ResponseEntity<ApiResponse<List<VoucherEditLogResponse>>> getLog(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<VoucherEditLogResponse> result = editLogService
                .getLog(entityType.toUpperCase(java.util.Locale.ROOT), entityId, page, size)
                .map(VoucherEditLogResponse::from);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result.getNumber() + 1, result.getTotalElements()));
    }
}
