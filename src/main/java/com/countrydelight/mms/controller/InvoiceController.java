package com.countrydelight.mms.controller;

import com.countrydelight.mms.dto.common.ApiResponse;
import com.countrydelight.mms.entity.purchase.SupplierInvoice;
import com.countrydelight.mms.service.purchase.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/invoice")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    /**
     * Create a new supplier invoice with optional PDF upload.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<SupplierInvoice>> createInvoice(
            @RequestParam String suppId,
            @RequestParam String invoiceNo,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate invoiceDate,
            @RequestParam BigDecimal invoiceAmount,
            @RequestParam BigDecimal gstAmount,
            @RequestParam(required = false) MultipartFile invoiceFile) {
        SupplierInvoice invoice = invoiceService.createInvoice(
                suppId, invoiceNo, invoiceDate, invoiceAmount, gstAmount, invoiceFile);
        return ResponseEntity.ok(ApiResponse.success("Invoice created successfully", invoice));
    }

    /**
     * Upload/update invoice PDF for an existing invoice.
     */
    @PostMapping(value = "/{invoiceId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<SupplierInvoice>> uploadInvoiceFile(
            @PathVariable Long invoiceId,
            @RequestParam MultipartFile file) {
        SupplierInvoice invoice = invoiceService.updateInvoiceFile(invoiceId, file);
        return ResponseEntity.ok(ApiResponse.success("Invoice file uploaded successfully", invoice));
    }

    /**
     * Get invoice by ID.
     */
    @GetMapping("/{invoiceId}")
    public ResponseEntity<ApiResponse<SupplierInvoice>> getInvoice(@PathVariable Long invoiceId) {
        SupplierInvoice invoice = invoiceService.getInvoice(invoiceId);
        return ResponseEntity.ok(ApiResponse.success(invoice));
    }

    /**
     * Get invoices by supplier.
     */
    @GetMapping("/supplier/{suppId}")
    public ResponseEntity<ApiResponse<List<SupplierInvoice>>> getInvoicesBySupplier(
            @PathVariable String suppId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<SupplierInvoice> result = invoiceService.getInvoicesBySupplier(suppId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result.getNumber() + 1, result.getTotalElements()));
    }

    /**
     * Get invoices by date range.
     */
    @GetMapping("/by-date")
    public ResponseEntity<ApiResponse<List<SupplierInvoice>>> getInvoicesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<SupplierInvoice> result = invoiceService.getInvoicesByDateRange(startDate, endDate, page, size);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result.getNumber() + 1, result.getTotalElements()));
    }
}
