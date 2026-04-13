package com.countrydelight.mms.controller;

import com.countrydelight.mms.dto.common.ApiResponse;
import com.countrydelight.mms.dto.purchase.PurchaseVoucherCreateRequest;
import com.countrydelight.mms.dto.purchase.PurchaseVoucherRegisterRow;
import com.countrydelight.mms.dto.purchase.PurchaseVoucherResponse;
import com.countrydelight.mms.entity.purchase.PurchaseVoucherHeader;
import com.countrydelight.mms.service.purchase.PurchaseVoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/purchase-voucher")
@RequiredArgsConstructor
public class PurchaseVoucherController {

    private final PurchaseVoucherService purchaseVoucherService;

    @PostMapping
    public ResponseEntity<ApiResponse<PurchaseVoucherResponse>> createManual(
            @Valid @RequestBody PurchaseVoucherCreateRequest request,
            @RequestParam(defaultValue = "false") boolean preClose) {
        PurchaseVoucherHeader pv = purchaseVoucherService.createManual(request, preClose);
        return ResponseEntity.ok(ApiResponse.success("Purchase Voucher created", PurchaseVoucherResponse.from(pv)));
    }

    @PostMapping("/from-grn/{grnId}")
    public ResponseEntity<ApiResponse<PurchaseVoucherResponse>> createFromGrn(
            @PathVariable Long grnId,
            @RequestBody(required = false) PurchaseVoucherCreateRequest request,
            @RequestParam(defaultValue = "false") boolean preClose) {
        if (request == null) {
            request = new PurchaseVoucherCreateRequest();
        }
        PurchaseVoucherHeader pv = purchaseVoucherService.createFromGrn(grnId, request, preClose);
        return ResponseEntity.ok(ApiResponse.success("Purchase Voucher created from GRN", PurchaseVoucherResponse.from(pv)));
    }

    @GetMapping("/{pvId}")
    public ResponseEntity<ApiResponse<PurchaseVoucherResponse>> getById(@PathVariable Long pvId) {
        PurchaseVoucherHeader pv = purchaseVoucherService.getById(pvId);
        return ResponseEntity.ok(ApiResponse.success(PurchaseVoucherResponse.from(pv)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PurchaseVoucherResponse>>> list(
            @RequestParam(required = false) String branchId,
            @RequestParam(required = false) String supplierFromId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String voucherCategory,
            @RequestParam(required = false) String voucherTypeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<PurchaseVoucherHeader> result = purchaseVoucherService
                .list(branchId, supplierFromId, status, voucherCategory, voucherTypeId, page, size);
        List<PurchaseVoucherResponse> data = result.getContent().stream().map(PurchaseVoucherResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.success(data, page, result.getTotalElements()));
    }

    @GetMapping("/register")
    public ResponseEntity<ApiResponse<List<PurchaseVoucherRegisterRow>>> register(
            @RequestParam(required = false) String branchId,
            @RequestParam(required = false) String voucherTypeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        List<PurchaseVoucherRegisterRow> rows = purchaseVoucherService
                .listRegister(branchId, voucherTypeId, fromDate, toDate, page, size);
        return ResponseEntity.ok(ApiResponse.success(rows));
    }

    @PutMapping("/{pvId}")
    public ResponseEntity<ApiResponse<PurchaseVoucherResponse>> update(
            @PathVariable Long pvId,
            @RequestBody PurchaseVoucherCreateRequest request) {
        PurchaseVoucherHeader pv = purchaseVoucherService.update(pvId, request);
        return ResponseEntity.ok(ApiResponse.success("Purchase Voucher updated", PurchaseVoucherResponse.from(pv)));
    }

    @PostMapping("/{pvId}/submit")
    public ResponseEntity<ApiResponse<PurchaseVoucherResponse>> submit(
            @PathVariable Long pvId,
            @RequestParam String submittedBy) {
        PurchaseVoucherHeader pv = purchaseVoucherService.submitForApproval(pvId, submittedBy);
        return ResponseEntity.ok(ApiResponse.success("Submitted for approval", PurchaseVoucherResponse.from(pv)));
    }

    @PostMapping("/{pvId}/approve")
    public ResponseEntity<ApiResponse<PurchaseVoucherResponse>> approve(
            @PathVariable Long pvId,
            @RequestParam String approvedBy) {
        PurchaseVoucherHeader pv = purchaseVoucherService.approveAndPost(pvId, approvedBy);
        return ResponseEntity.ok(ApiResponse.success("Purchase Voucher approved and posted", PurchaseVoucherResponse.from(pv)));
    }

}
