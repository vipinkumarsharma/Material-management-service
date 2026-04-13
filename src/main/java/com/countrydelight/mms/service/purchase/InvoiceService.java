package com.countrydelight.mms.service.purchase;

import com.countrydelight.mms.config.S3Config;
import com.countrydelight.mms.entity.purchase.SupplierInvoice;
import com.countrydelight.mms.exception.MmsException;
import com.countrydelight.mms.repository.purchase.SupplierInvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

/**
 * Invoice Service - Handles supplier invoices and S3 uploads.
 *
 * IMPORTANT: Invoices never update stock directly.
 * Stock is only updated through GRN posting.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final SupplierInvoiceRepository invoiceRepository;
    private final S3Client s3Client;
    private final S3Config s3Config;

    /**
     * Create a supplier invoice with optional PDF upload to S3.
     */
    @Transactional
    public SupplierInvoice createInvoice(String suppId, String invoiceNo, LocalDate invoiceDate,
                                          BigDecimal invoiceAmount, BigDecimal gstAmount,
                                          MultipartFile invoiceFile) {
        // Check for duplicate invoice
        if (invoiceRepository.findBySuppIdAndInvoiceNo(suppId, invoiceNo).isPresent()) {
            throw new MmsException("Invoice already exists: " + invoiceNo + " for supplier " + suppId);
        }

        BigDecimal netAmount = invoiceAmount.add(gstAmount);

        String s3Url = null;
        if (invoiceFile != null && !invoiceFile.isEmpty()) {
            s3Url = uploadInvoiceToS3(suppId, invoiceNo, invoiceFile);
        }

        SupplierInvoice invoice = SupplierInvoice.builder()
                .suppId(suppId)
                .invoiceNo(invoiceNo)
                .invoiceDate(invoiceDate)
                .invoiceAmount(invoiceAmount)
                .gstAmount(gstAmount)
                .netAmount(netAmount)
                .invoiceS3Url(s3Url)
                .build();

        invoice = invoiceRepository.save(invoice);
        log.info("Invoice created: ID={}, Supplier={}, InvoiceNo={}", invoice.getInvoiceId(), suppId, invoiceNo);

        return invoice;
    }

    /**
     * Upload invoice PDF to S3.
     */
    public String uploadInvoiceToS3(String suppId, String invoiceNo, MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".pdf";

            String key = String.format(Locale.ROOT, "invoices/%s/%s_%s%s",
                    suppId, invoiceNo, UUID.randomUUID().toString().substring(0, 8), extension);

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            String s3Url = String.format(Locale.ROOT, "s3://%s/%s", s3Config.getBucketName(), key);
            log.info("Invoice uploaded to S3: {}", s3Url);

            return s3Url;
        } catch (IOException e) {
            throw new MmsException("Failed to upload invoice to S3: " + e.getMessage(), e);
        }
    }

    /**
     * Update invoice S3 URL.
     */
    @Transactional
    public SupplierInvoice updateInvoiceFile(Long invoiceId, MultipartFile file) {
        SupplierInvoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new MmsException("Invoice not found: " + invoiceId));

        String s3Url = uploadInvoiceToS3(invoice.getSuppId(), invoice.getInvoiceNo(), file);
        invoice.setInvoiceS3Url(s3Url);

        return invoiceRepository.save(invoice);
    }

    /**
     * Get invoice by ID.
     */
    public SupplierInvoice getInvoice(Long invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new MmsException("Invoice not found: " + invoiceId));
    }

    /**
     * Get invoices by supplier.
     */
    public Page<SupplierInvoice> getInvoicesBySupplier(String suppId, int page, int size) {
        return invoiceRepository.findBySuppId(suppId,
                PageRequest.of(page - 1, size, Sort.by("invoiceId").descending()));
    }

    /**
     * Get invoices by date range.
     */
    public Page<SupplierInvoice> getInvoicesByDateRange(LocalDate startDate, LocalDate endDate, int page, int size) {
        return invoiceRepository.findByInvoiceDateBetween(startDate, endDate,
                PageRequest.of(page - 1, size, Sort.by("invoiceId").descending()));
    }
}
