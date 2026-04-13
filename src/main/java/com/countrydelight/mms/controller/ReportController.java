package com.countrydelight.mms.controller;

import com.countrydelight.mms.dto.common.ApiResponse;
import com.countrydelight.mms.dto.report.AuditExceptionReportDto;
import com.countrydelight.mms.dto.report.BranchReportDto;
import com.countrydelight.mms.dto.report.ConsolidatedStockSummaryDto;
import com.countrydelight.mms.dto.report.CurrentStockReportDto;
import com.countrydelight.mms.dto.report.FifoConsumptionReportDto;
import com.countrydelight.mms.dto.report.GrnDetailReportDto;
import com.countrydelight.mms.dto.report.ItemStockPositionDto;
import com.countrydelight.mms.dto.report.GrnSummaryReportDto;
import com.countrydelight.mms.dto.report.GrnVsInvoiceReportDto;
import com.countrydelight.mms.dto.report.InterBranchTransferReportDto;
import com.countrydelight.mms.dto.report.IssueToProductionReportDto;
import com.countrydelight.mms.dto.report.ItemReportDetailDto;
import com.countrydelight.mms.dto.report.ItemReportSummaryDto;
import com.countrydelight.mms.dto.report.NonMovingStockReportDto;
import com.countrydelight.mms.dto.report.PoVsGrnReportDto;
import com.countrydelight.mms.dto.report.PriceVarianceReportDto;
import com.countrydelight.mms.dto.report.PvItemFulfillmentReportDto;
import com.countrydelight.mms.dto.report.PurchaseOrderItemDto;
import com.countrydelight.mms.dto.report.PvVsGrnReportDto;
import com.countrydelight.mms.dto.report.ReceiptNoteItemDto;
import com.countrydelight.mms.dto.report.ReportFilterDto;
import com.countrydelight.mms.dto.report.StockAgingReportDto;
import com.countrydelight.mms.dto.report.StockLedgerReportDto;
import com.countrydelight.mms.dto.report.GodownSummaryResponse;
import com.countrydelight.mms.dto.report.GodownVoucherResponse;
import com.countrydelight.mms.dto.report.StockStatementDto;
import com.countrydelight.mms.dto.report.StockSummaryReportDto;
import com.countrydelight.mms.dto.report.SupplierGodownSummaryResponse;
import com.countrydelight.mms.dto.report.SupplierGodownVoucherResponse;
import com.countrydelight.mms.dto.report.SupplierReportDetailDto;
import com.countrydelight.mms.dto.report.SupplierReportSummaryDto;
import com.countrydelight.mms.service.report.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * REST API endpoints for all reports.
 *
 * All reports support multi-branch filtering:
 * - No branch filter = ALL branches (company-wide view)
 * - Single branch = Single branch view
 * - Multiple branches = Multi-branch view
 *
 * Common query parameters:
 * - branchIds: Optional, comma-separated list of branch IDs
 * - fromDate: Optional, start date (inclusive)
 * - toDate: Optional, end date (inclusive)
 * - itemId: Optional, filter by specific item
 * - suppId: Optional, filter by specific supplier
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reports", description = "MMS Reporting APIs")
public class ReportController {
    private final ReportService reportService;

    // ========================================================================
    // REPORT 1: CURRENT STOCK REPORT
    // ========================================================================
    @GetMapping("/current-stock")
    @Operation(summary = "Current Stock Report",
            description = "Shows current stock on hand by branch, item, and location. " +
                    "Use for daily stock visibility and inventory management.")
    public ResponseEntity<ApiResponse<List<CurrentStockReportDto>>> getCurrentStockReport(
            @Parameter(description = "Branch IDs (comma-separated). Omit for all branches.")
            @RequestParam(required = false) List<String> branchIds,
            @Parameter(description = "Item ID to filter by specific item")
            @RequestParam(required = false) String itemId,
            @Parameter(description = "Location ID to filter by specific storage location")
            @RequestParam(required = false) String locationId
    ) {
        log.info("GET /reports/current-stock - branches: {}, item: {}", branchIds, itemId);

        ReportFilterDto filter = ReportFilterDto.builder()
                .branchIds(branchIds)
                .itemId(itemId)
                .locationId(locationId)
                .build();

        return ResponseEntity.ok(ApiResponse.success(reportService.getCurrentStockReport(filter)));
    }

    // ========================================================================
    // REPORT 2: CONSOLIDATED STOCK SUMMARY
    // ========================================================================
    @GetMapping("/consolidated-stock")
    @Operation(summary = "Consolidated Stock Summary",
            description = "Total stock per item across all/selected branches with branch-wise breakup. " +
                    "Use for company-wide inventory overview.")
    public ResponseEntity<ApiResponse<List<ConsolidatedStockSummaryDto>>> getConsolidatedStockSummary(
            @Parameter(description = "Branch IDs (comma-separated). Omit for all branches.")
            @RequestParam(required = false) List<String> branchIds
    ) {
        log.info("GET /reports/consolidated-stock - branches: {}", branchIds);

        ReportFilterDto filter = ReportFilterDto.builder()
                .branchIds(branchIds)
                .build();

        return ResponseEntity.ok(ApiResponse.success(reportService.getConsolidatedStockSummary(filter)));
    }

    // ========================================================================
    // REPORT 3: STOCK LEDGER REPORT
    // ========================================================================
    @GetMapping("/stock-ledger")
    @Operation(summary = "Stock Ledger Report",
            description = "Complete audit trail of all stock movements. " +
                    "Use for reconciliation and detailed movement analysis.")
    public ResponseEntity<ApiResponse<List<StockLedgerReportDto>>> getStockLedgerReport(
            @Parameter(description = "Branch IDs (comma-separated). Omit for all branches.")
            @RequestParam(required = false) List<String> branchIds,
            @Parameter(description = "Item ID to filter")
            @RequestParam(required = false) String itemId,
            @Parameter(description = "Start date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Transaction type: GRN, ISSUE, TRANSFER_IN, TRANSFER_OUT")
            @RequestParam(required = false) String txnType,
            @Parameter(description = "Department ID to filter")
            @RequestParam(required = false) Integer deptId
    ) {
        log.info("GET /reports/stock-ledger - branches: {}, item: {}, dates: {} to {}",
                branchIds, itemId, fromDate, toDate);

        ReportFilterDto filter = ReportFilterDto.builder()
                .branchIds(branchIds)
                .itemId(itemId)
                .fromDate(fromDate)
                .toDate(toDate)
                .status(txnType)
                .deptId(deptId)
                .build();

        return ResponseEntity.ok(ApiResponse.success(reportService.getStockLedgerReport(filter)));
    }

    // ========================================================================
    // REPORT 4: STOCK AGING REPORT
    // ========================================================================
    @GetMapping("/stock-aging")
    @Operation(summary = "Stock Aging Report",
            description = "Stock age based on GRN date. Buckets: 0-30, 31-60, 61-90, 90+ days. " +
                    "Use to ensure FIFO compliance and identify old stock.")
    public ResponseEntity<ApiResponse<List<StockAgingReportDto>>> getStockAgingReport(
            @Parameter(description = "Branch IDs (comma-separated). Omit for all branches.")
            @RequestParam(required = false) List<String> branchIds,
            @Parameter(description = "Item ID to filter")
            @RequestParam(required = false) String itemId
    ) {
        log.info("GET /reports/stock-aging - branches: {}, item: {}", branchIds, itemId);

        ReportFilterDto filter = ReportFilterDto.builder()
                .branchIds(branchIds)
                .itemId(itemId)
                .build();

        return ResponseEntity.ok(ApiResponse.success(reportService.getStockAgingReport(filter)));
    }

    // ========================================================================
    // REPORT 5: FIFO CONSUMPTION REPORT
    // ========================================================================
    @GetMapping("/fifo-consumption")
    @Operation(summary = "FIFO Consumption Report",
            description = "Shows which GRN batches were consumed for each issue. " +
                    "Use to verify FIFO compliance and trace material source.")
    public ResponseEntity<ApiResponse<List<FifoConsumptionReportDto>>> getFifoConsumptionReport(
            @Parameter(description = "Branch IDs (comma-separated). Omit for all branches.")
            @RequestParam(required = false) List<String> branchIds,
            @Parameter(description = "Item ID to filter")
            @RequestParam(required = false) String itemId,
            @Parameter(description = "Start date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        log.info("GET /reports/fifo-consumption - branches: {}, item: {}, dates: {} to {}",
                branchIds, itemId, fromDate, toDate);

        ReportFilterDto filter = ReportFilterDto.builder()
                .branchIds(branchIds)
                .itemId(itemId)
                .fromDate(fromDate)
                .toDate(toDate)
                .build();

        return ResponseEntity.ok(ApiResponse.success(reportService.getFifoConsumptionReport(filter)));
    }

    // ========================================================================
    // REPORT 6: GRN SUMMARY REPORT
    // ========================================================================
    @GetMapping("/grn-summary")
    @Operation(summary = "GRN Summary Report",
            description = "Overview of all goods received. " +
                    "Use for receipt tracking and delivery monitoring.")
    public ResponseEntity<ApiResponse<List<GrnSummaryReportDto>>> getGrnSummaryReport(
            @Parameter(description = "Branch IDs (comma-separated). Omit for all branches.")
            @RequestParam(required = false) List<String> branchIds,
            @Parameter(description = "Supplier ID to filter")
            @RequestParam(required = false) String suppId,
            @Parameter(description = "Start date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "GRN status: DRAFT, PENDING_APPROVAL, POSTED")
            @RequestParam(required = false) String status,
            @Parameter(description = "Department ID to filter")
            @RequestParam(required = false) Integer deptId
    ) {
        log.info("GET /reports/grn-summary - branches: {}, supplier: {}, dates: {} to {}",
                branchIds, suppId, fromDate, toDate);

        ReportFilterDto filter = ReportFilterDto.builder()
                .branchIds(branchIds)
                .suppId(suppId)
                .fromDate(fromDate)
                .toDate(toDate)
                .status(status)
                .deptId(deptId)
                .build();

        return ResponseEntity.ok(ApiResponse.success(reportService.getGrnSummaryReport(filter)));
    }

    // ========================================================================
    // REPORT 7: GRN VS INVOICE COMPARISON
    // ========================================================================
    @GetMapping("/grn-vs-invoice")
    @Operation(summary = "GRN vs Invoice Comparison Report",
            description = "Compare GRN amount vs supplier invoice amount. " +
                    "Use for three-way matching and discrepancy identification.")
    public ResponseEntity<ApiResponse<List<GrnVsInvoiceReportDto>>> getGrnVsInvoiceReport(
            @Parameter(description = "Branch IDs (comma-separated). Omit for all branches.")
            @RequestParam(required = false) List<String> branchIds,
            @Parameter(description = "Supplier ID to filter")
            @RequestParam(required = false) String suppId,
            @Parameter(description = "Start date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Department ID to filter")
            @RequestParam(required = false) Integer deptId
    ) {
        log.info("GET /reports/grn-vs-invoice - branches: {}, supplier: {}", branchIds, suppId);

        ReportFilterDto filter = ReportFilterDto.builder()
                .branchIds(branchIds)
                .suppId(suppId)
                .fromDate(fromDate)
                .toDate(toDate)
                .deptId(deptId)
                .build();

        return ResponseEntity.ok(ApiResponse.success(reportService.getGrnVsInvoiceReport(filter)));
    }

    // ========================================================================
    // REPORT 8: PRICE VARIANCE REPORT
    // ========================================================================
    @GetMapping("/price-variance")
    @Operation(summary = "Price Variance Report",
            description = "Identify price changes from reference price across branches. " +
                    "Use for cost control and price negotiation analysis.")
    public ResponseEntity<ApiResponse<List<PriceVarianceReportDto>>> getPriceVarianceReport(
            @Parameter(description = "Branch IDs (comma-separated). Omit for all branches.")
            @RequestParam(required = false) List<String> branchIds,
            @Parameter(description = "Item ID to filter")
            @RequestParam(required = false) String itemId,
            @Parameter(description = "Supplier ID to filter")
            @RequestParam(required = false) String suppId,
            @Parameter(description = "Start date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Minimum variance % to filter (e.g., 5 for >5%)")
            @RequestParam(required = false) BigDecimal minVariance,
            @Parameter(description = "Department ID to filter")
            @RequestParam(required = false) Integer deptId
    ) {
        log.info("GET /reports/price-variance - branches: {}, item: {}, minVariance: {}%",
                branchIds, itemId, minVariance);

        ReportFilterDto filter = ReportFilterDto.builder()
                .branchIds(branchIds)
                .itemId(itemId)
                .suppId(suppId)
                .fromDate(fromDate)
                .toDate(toDate)
                .minVariancePercent(minVariance)
                .deptId(deptId)
                .build();

        return ResponseEntity.ok(ApiResponse.success(reportService.getPriceVarianceReport(filter)));
    }

    // ========================================================================
    // REPORT 9: PO VS GRN REPORT
    // ========================================================================
    @GetMapping("/po-vs-grn")
    @Operation(summary = "PO vs GRN Report",
            description = "Track purchase order fulfillment status. " +
                    "Use for pending delivery follow-up and supplier performance.")
    public ResponseEntity<ApiResponse<List<PoVsGrnReportDto>>> getPoVsGrnReport(
            @Parameter(description = "Branch IDs (comma-separated). Omit for all branches.")
            @RequestParam(required = false) List<String> branchIds,
            @Parameter(description = "Supplier ID to filter")
            @RequestParam(required = false) String suppId,
            @Parameter(description = "Item ID to filter")
            @RequestParam(required = false) String itemId,
            @Parameter(description = "Start date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "PO status: OPEN, PARTIAL, CLOSED")
            @RequestParam(required = false) String status
    ) {
        log.info("GET /reports/po-vs-grn - branches: {}, supplier: {}, status: {}",
                branchIds, suppId, status);

        ReportFilterDto filter = ReportFilterDto.builder()
                .branchIds(branchIds)
                .suppId(suppId)
                .itemId(itemId)
                .fromDate(fromDate)
                .toDate(toDate)
                .status(status)
                .build();

        return ResponseEntity.ok(ApiResponse.success(reportService.getPoVsGrnReport(filter)));
    }

    // ========================================================================
    // REPORT 10: INTER-BRANCH TRANSFER REPORT
    // ========================================================================
    @GetMapping("/inter-branch-transfers")
    @Operation(summary = "Inter-Branch Transfer Report",
            description = "Track stock movements between branches. " +
                    "Use for logistics tracking and shortage identification.")
    public ResponseEntity<ApiResponse<List<InterBranchTransferReportDto>>> getInterBranchTransferReport(
            @Parameter(description = "Branch IDs (comma-separated). Filters by from OR to branch.")
            @RequestParam(required = false) List<String> branchIds,
            @Parameter(description = "Item ID to filter")
            @RequestParam(required = false) String itemId,
            @Parameter(description = "Start date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Transfer status: CREATED, IN_TRANSIT, RECEIVED")
            @RequestParam(required = false) String status,
            @Parameter(description = "Department ID to filter")
            @RequestParam(required = false) Integer deptId
    ) {
        log.info("GET /reports/inter-branch-transfers - branches: {}, status: {}",
                branchIds, status);

        ReportFilterDto filter = ReportFilterDto.builder()
                .branchIds(branchIds)
                .itemId(itemId)
                .fromDate(fromDate)
                .toDate(toDate)
                .status(status)
                .deptId(deptId)
                .build();

        return ResponseEntity.ok(ApiResponse.success(reportService.getInterBranchTransferReport(filter)));
    }

    // ========================================================================
    // REPORT 11: ISSUE TO PRODUCTION REPORT
    // ========================================================================
    @GetMapping("/issue-to-production")
    @Operation(summary = "Issue to Production Report",
            description = "Track materials consumed by production. " +
                    "Use for consumption tracking and cost allocation.")
    public ResponseEntity<ApiResponse<List<IssueToProductionReportDto>>> getIssueToProductionReport(
            @Parameter(description = "Branch IDs (comma-separated). Omit for all branches.")
            @RequestParam(required = false) List<String> branchIds,
            @Parameter(description = "Item ID to filter")
            @RequestParam(required = false) String itemId,
            @Parameter(description = "Start date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Issue status: DRAFT, PENDING_APPROVAL, POSTED")
            @RequestParam(required = false) String status,
            @Parameter(description = "Department ID to filter")
            @RequestParam(required = false) Integer deptId
    ) {
        log.info("GET /reports/issue-to-production - branches: {}, item: {}",
                branchIds, itemId);

        ReportFilterDto filter = ReportFilterDto.builder()
                .branchIds(branchIds)
                .itemId(itemId)
                .fromDate(fromDate)
                .toDate(toDate)
                .status(status)
                .deptId(deptId)
                .build();

        return ResponseEntity.ok(ApiResponse.success(reportService.getIssueToProductionReport(filter)));
    }

    // ========================================================================
    // REPORT 12: NON-MOVING / SLOW-MOVING STOCK
    // ========================================================================
    @GetMapping("/non-moving-stock")
    @Operation(summary = "Non-Moving / Slow-Moving Stock Report",
            description = "Identify dead/slow stock. Non-moving = 90+ days, Slow-moving = 30-90 days. " +
                    "Use for inventory optimization and clearance planning.")
    public ResponseEntity<ApiResponse<List<NonMovingStockReportDto>>> getNonMovingStockReport(
            @Parameter(description = "Branch IDs (comma-separated). Omit for all branches.")
            @RequestParam(required = false) List<String> branchIds,
            @Parameter(description = "Item ID to filter")
            @RequestParam(required = false) String itemId,
            @Parameter(description = "Minimum days since last movement (default: 30)")
            @RequestParam(required = false, defaultValue = "30") Integer minDays
    ) {
        log.info("GET /reports/non-moving-stock - branches: {}, minDays: {}", branchIds, minDays);

        ReportFilterDto filter = ReportFilterDto.builder()
                .branchIds(branchIds)
                .itemId(itemId)
                .agingDays(minDays)
                .build();

        return ResponseEntity.ok(ApiResponse.success(reportService.getNonMovingStockReport(filter)));
    }

    // ========================================================================
    // REPORT 13a: SUPPLIER REPORT - SUMMARY
    // ========================================================================
    @GetMapping("/supplier-report-summary")
    @Operation(summary = "Supplier Report - Summary",
            description = "Per supplier + per item: total qty, total value, avg rate, GRN count. " +
                    "Use for supplier-wise procurement analysis.")
    public ResponseEntity<ApiResponse<List<SupplierReportSummaryDto>>> getSupplierReportSummary(
            @Parameter(description = "Branch IDs (comma-separated). Omit for all branches.")
            @RequestParam(required = false) List<String> branchIds,
            @Parameter(description = "Supplier ID to filter")
            @RequestParam(required = false) String suppId,
            @Parameter(description = "Item ID to filter")
            @RequestParam(required = false) String itemId,
            @Parameter(description = "Start date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Department ID to filter")
            @RequestParam(required = false) Integer deptId
    ) {
        log.info("GET /reports/supplier-report-summary - suppId: {}, branches: {}", suppId, branchIds);

        ReportFilterDto filter = ReportFilterDto.builder()
                .branchIds(branchIds)
                .suppId(suppId)
                .itemId(itemId)
                .fromDate(fromDate)
                .toDate(toDate)
                .deptId(deptId)
                .build();

        return ResponseEntity.ok(ApiResponse.success(reportService.getSupplierReportSummary(filter)));
    }

    // ========================================================================
    // REPORT 13b: SUPPLIER REPORT - DETAIL
    // ========================================================================
    @GetMapping("/supplier-report-detail")
    @Operation(summary = "Supplier Report - Detail",
            description = "Individual GRN lines: supplier, GRN date, PO date, item, qty, rate, value, branch, invoice. " +
                    "Use for detailed supplier transaction history.")
    public ResponseEntity<ApiResponse<List<SupplierReportDetailDto>>> getSupplierReportDetail(
            @Parameter(description = "Branch IDs (comma-separated). Omit for all branches.")
            @RequestParam(required = false) List<String> branchIds,
            @Parameter(description = "Supplier ID to filter")
            @RequestParam(required = false) String suppId,
            @Parameter(description = "Item ID to filter")
            @RequestParam(required = false) String itemId,
            @Parameter(description = "Start date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Department ID to filter")
            @RequestParam(required = false) Integer deptId
    ) {
        log.info("GET /reports/supplier-report-detail - suppId: {}, branches: {}", suppId, branchIds);

        ReportFilterDto filter = ReportFilterDto.builder()
                .branchIds(branchIds)
                .suppId(suppId)
                .itemId(itemId)
                .fromDate(fromDate)
                .toDate(toDate)
                .deptId(deptId)
                .build();

        return ResponseEntity.ok(ApiResponse.success(reportService.getSupplierReportDetail(filter)));
    }

    // ========================================================================
    // REPORT 14: AUDIT & EXCEPTION REPORT
    // ========================================================================
    @GetMapping("/audit-exceptions")
    @Operation(summary = "Audit & Exception Report",
            description = "Identify exceptions and anomalies for audit review. " +
                    "Includes: GRNs without PO, high price variance, transfer shortages.")
    public ResponseEntity<ApiResponse<List<AuditExceptionReportDto>>> getAuditExceptionReport(
            @Parameter(description = "Branch IDs (comma-separated). Omit for all branches.")
            @RequestParam(required = false) List<String> branchIds,
            @Parameter(description = "Start date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Minimum variance % to flag (default: 5%)")
            @RequestParam(required = false, defaultValue = "5") BigDecimal minVariance,
            @Parameter(description = "Department ID to filter")
            @RequestParam(required = false) Integer deptId
    ) {
        log.info("GET /reports/audit-exceptions - branches: {}, minVariance: {}%",
                branchIds, minVariance);

        ReportFilterDto filter = ReportFilterDto.builder()
                .branchIds(branchIds)
                .fromDate(fromDate)
                .toDate(toDate)
                .minVariancePercent(minVariance)
                .deptId(deptId)
                .build();

        return ResponseEntity.ok(ApiResponse.success(reportService.getAuditExceptionReport(filter)));
    }

    // ========================================================================
    // REPORT 15: STOCK STATEMENT (MOVEMENT SUMMARY)
    // ========================================================================
    @GetMapping("/stock-statement")
    @Operation(summary = "Stock Statement / Movement Summary",
            description = "Shows opening balance, inward, outward, and closing balance for a date range. " +
                    "Use for period-end stock reconciliation. fromDate and toDate are required.")
    public ResponseEntity<ApiResponse<List<StockStatementDto>>> getStockStatementReport(
            @Parameter(description = "Branch IDs (comma-separated). Omit for all branches.")
            @RequestParam(required = false) List<String> branchIds,
            @Parameter(description = "Item ID to filter")
            @RequestParam(required = false) String itemId,
            @Parameter(description = "Location ID to filter")
            @RequestParam(required = false) String locationId,
            @Parameter(description = "Department ID to filter")
            @RequestParam(required = false) Integer deptId,
            @Parameter(description = "Start date (inclusive). Defaults to start of current month if omitted.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date (inclusive). Defaults to today if omitted.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        log.info("GET /reports/stock-statement - branches: {}, item: {}, dates: {} to {}",
                branchIds, itemId, fromDate, toDate);

        ReportFilterDto filter = ReportFilterDto.builder()
                .branchIds(branchIds)
                .itemId(itemId)
                .locationId(locationId)
                .deptId(deptId)
                .fromDate(fromDate)
                .toDate(toDate)
                .build();

        return ResponseEntity.ok(ApiResponse.success(reportService.getStockStatementReport(filter)));
    }

    // ========================================================================
    // REPORT 16: GRN DETAIL REPORT
    // ========================================================================
    @GetMapping("/grn-detail")
    @Operation(summary = "GRN Detail Report",
            description = "Line-item level GRN details with item, qty, rate, taxes, discount, net amount. " +
                    "Use alongside grn-summary for detailed receipt analysis.")
    public ResponseEntity<ApiResponse<List<GrnDetailReportDto>>> getGrnDetailReport(
            @Parameter(description = "Branch IDs (comma-separated). Omit for all branches.")
            @RequestParam(required = false) List<String> branchIds,
            @Parameter(description = "Supplier ID to filter")
            @RequestParam(required = false) String suppId,
            @Parameter(description = "Item ID to filter")
            @RequestParam(required = false) String itemId,
            @Parameter(description = "Start date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Department ID to filter")
            @RequestParam(required = false) Integer deptId
    ) {
        log.info("GET /reports/grn-detail - branches: {}, supplier: {}, item: {}", branchIds, suppId, itemId);

        ReportFilterDto filter = ReportFilterDto.builder()
                .branchIds(branchIds)
                .suppId(suppId)
                .itemId(itemId)
                .fromDate(fromDate)
                .toDate(toDate)
                .deptId(deptId)
                .build();

        return ResponseEntity.ok(ApiResponse.success(reportService.getGrnDetailReport(filter)));
    }

    // ========================================================================
    // REPORT: RECEIPT NOTE ITEM-WISE
    // ========================================================================
    @GetMapping("/receipt-note-items")
    @Operation(summary = "Receipt Note Item-wise Report",
            description = "Item-level details for all Receipt Note purchase vouchers. " +
                    "Shows GRN date, GRN No, branch, supplier, linked PO No/date, invoice No/date, " +
                    "item code, description, unit, qty, basic price, basic amount, GST rate, GST amount, " +
                    "amount with GST, and remarks.")
    public ResponseEntity<ApiResponse<List<ReceiptNoteItemDto>>> getReceiptNoteItemReport(
            @Parameter(description = "Branch ID to filter")
            @RequestParam(required = false) String branchId,
            @Parameter(description = "Supplier ID to filter")
            @RequestParam(required = false) String suppId,
            @Parameter(description = "Item ID to filter")
            @RequestParam(required = false) String itemId,
            @Parameter(description = "Start date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        log.info("GET /reports/receipt-note-items - branch: {}, supplier: {}, item: {}", branchId, suppId, itemId);

        ReportFilterDto filter = ReportFilterDto.builder()
                .branchIds(branchId != null ? List.of(branchId) : null)
                .suppId(suppId)
                .itemId(itemId)
                .fromDate(fromDate)
                .toDate(toDate)
                .build();

        return ResponseEntity.ok(ApiResponse.success(reportService.getReceiptNoteItemReport(filter)));
    }

    // ========================================================================
    // REPORT: PURCHASE ORDER ITEM-WISE
    // ========================================================================
    @GetMapping("/purchase-order-items")
    @Operation(summary = "Purchase Order Item-wise Report",
            description = "Item-level details for all Purchase Order purchase vouchers. " +
                    "Shows date, supplier, branch, PO No, PO date, item code, description, unit, " +
                    "ordered qty, basic price, basic amount, GST rate, GST amount, total amount, and remarks.")
    public ResponseEntity<ApiResponse<List<PurchaseOrderItemDto>>> getPurchaseOrderItemReport(
            @Parameter(description = "Branch ID to filter")
            @RequestParam(required = false) String branchId,
            @Parameter(description = "Supplier ID to filter")
            @RequestParam(required = false) String suppId,
            @Parameter(description = "Item ID to filter")
            @RequestParam(required = false) String itemId,
            @Parameter(description = "Start date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "25") int size
    ) {
        log.info("GET /reports/purchase-order-items - branch: {}, supplier: {}, item: {}", branchId, suppId, itemId);

        ReportFilterDto filter = ReportFilterDto.builder()
                .branchIds(branchId != null ? List.of(branchId) : null)
                .suppId(suppId)
                .itemId(itemId)
                .fromDate(fromDate)
                .toDate(toDate)
                .build();

        return ResponseEntity.ok(ApiResponse.success(reportService.getPurchaseOrderItemReport(filter, page, size)));
    }

    // ========================================================================
    // REPORT 17: ITEM REPORT - SUMMARY
    // ========================================================================
    @GetMapping("/item-report-summary")
    @Operation(summary = "Item Report - Summary",
            description = "Per item: current stock, total GRN qty/value, total issue qty/value, supplier count, last dates. " +
                    "Use for item-centric inventory analysis.")
    public ResponseEntity<ApiResponse<List<ItemReportSummaryDto>>> getItemReportSummary(
            @Parameter(description = "Branch IDs (comma-separated). Omit for all branches.")
            @RequestParam(required = false) List<String> branchIds,
            @Parameter(description = "Item ID to filter")
            @RequestParam(required = false) String itemId,
            @Parameter(description = "Start date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Department ID to filter")
            @RequestParam(required = false) Integer deptId
    ) {
        log.info("GET /reports/item-report-summary - item: {}, branches: {}", itemId, branchIds);

        ReportFilterDto filter = ReportFilterDto.builder()
                .branchIds(branchIds)
                .itemId(itemId)
                .fromDate(fromDate)
                .toDate(toDate)
                .deptId(deptId)
                .build();

        return ResponseEntity.ok(ApiResponse.success(reportService.getItemReportSummary(filter)));
    }

    // ========================================================================
    // REPORT 18: ITEM REPORT - DETAIL
    // ========================================================================
    @GetMapping("/item-report-detail")
    @Operation(summary = "Item Report - Detail",
            description = "All transactions for an item: GRN/ISSUE/TRANSFER with dates, qty, rate, counterparty. " +
                    "itemId is required.")
    public ResponseEntity<ApiResponse<List<ItemReportDetailDto>>> getItemReportDetail(
            @Parameter(description = "Item ID (required)")
            @RequestParam String itemId,
            @Parameter(description = "Branch IDs (comma-separated). Omit for all branches.")
            @RequestParam(required = false) List<String> branchIds,
            @Parameter(description = "Start date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Department ID to filter")
            @RequestParam(required = false) Integer deptId
    ) {
        log.info("GET /reports/item-report-detail - item: {}, branches: {}", itemId, branchIds);

        ReportFilterDto filter = ReportFilterDto.builder()
                .branchIds(branchIds)
                .itemId(itemId)
                .fromDate(fromDate)
                .toDate(toDate)
                .deptId(deptId)
                .build();

        return ResponseEntity.ok(ApiResponse.success(reportService.getItemReportDetail(filter)));
    }

    // ========================================================================
    // REPORT 19: BRANCH REPORT
    // ========================================================================
    @GetMapping("/branch-report")
    @Operation(summary = "Branch Report",
            description = "Per branch: stock value, GRN count+value, issue count+value, transfers, top items, top suppliers. " +
                    "Use for branch-level performance overview.")
    public ResponseEntity<ApiResponse<List<BranchReportDto>>> getBranchReport(
            @Parameter(description = "Branch IDs (comma-separated). Omit for all branches.")
            @RequestParam(required = false) List<String> branchIds,
            @Parameter(description = "Start date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Department ID to filter")
            @RequestParam(required = false) Integer deptId
    ) {
        log.info("GET /reports/branch-report - branches: {}, dates: {} to {}", branchIds, fromDate, toDate);

        ReportFilterDto filter = ReportFilterDto.builder()
                .branchIds(branchIds)
                .fromDate(fromDate)
                .toDate(toDate)
                .deptId(deptId)
                .build();

        return ResponseEntity.ok(ApiResponse.success(reportService.getBranchReport(filter)));
    }

    // ========================================================================
    // REPORT 20: PV VS GRN (Purchase Order base, received qty from Receipt Notes)
    // ========================================================================
    @GetMapping("/pv-vs-grn")
    @Operation(summary = "PV vs GRN Report",
            description = "One row per Purchase Order PV × item. receivedQty = SUM of all linked Receipt Note PVs.")
    public ResponseEntity<ApiResponse<List<PvVsGrnReportDto>>> getPvVsGrnReport(
            @Parameter(description = "Branch IDs (comma-separated). Omit for all branches.")
            @RequestParam(required = false) List<String> branchIds,
            @Parameter(description = "Supplier ID to filter")
            @RequestParam(required = false) String suppId,
            @Parameter(description = "Start date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Page number (1-based)")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "25") int size
    ) {
        log.info("GET /reports/pv-vs-grn - branches: {}, supplier: {}, page: {}, size: {}", branchIds, suppId, page, size);

        ReportFilterDto filter = ReportFilterDto.builder()
                .branchIds(branchIds)
                .suppId(suppId)
                .fromDate(fromDate)
                .toDate(toDate)
                .build();

        return ResponseEntity.ok(ApiResponse.success(reportService.getPvVsGrnReport(filter, page, size)));
    }

    // ========================================================================
    // REPORT 22: STOCK SUMMARY REPORT (TallyPrime-style period-end inventory)
    // ========================================================================
    @GetMapping("/stock-summary")
    @Operation(summary = "Stock Summary Report",
            description = "TallyPrime-style period-end inventory report. Groups items by item group with " +
                    "sub-totals per group and a grand total row. Shows opening balance, all movement " +
                    "sub-types (purchase, sales return, transfer-in third-party/inhouse, sales, " +
                    "purchase return, transfer-out third-party/inhouse), closing balance with GST, " +
                    "and consumption analytics (consumed qty, avg consumption/day, days covered). " +
                    "fromDate and toDate are required.")
    public ResponseEntity<ApiResponse<StockSummaryReportDto>> getStockSummaryReport(
            @Parameter(description = "Start date (inclusive, required)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date (inclusive, required)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Branch IDs (comma-separated). Omit for all branches.")
            @RequestParam(required = false) List<String> branchIds,
            @Parameter(description = "Group ID to filter by specific item group")
            @RequestParam(required = false) String groupId,
            @Parameter(description = "Item ID to filter by specific item")
            @RequestParam(required = false) String itemId
    ) {
        log.info("GET /reports/stock-summary - branches: {}, dates: {} to {}, group: {}, item: {}",
                branchIds, fromDate, toDate, groupId, itemId);

        ReportFilterDto filter = ReportFilterDto.builder()
                .branchIds(branchIds)
                .fromDate(fromDate)
                .toDate(toDate)
                .groupId(groupId)
                .itemId(itemId)
                .build();

        return ResponseEntity.ok(ApiResponse.success(reportService.getStockSummaryReport(filter)));
    }

    // ========================================================================
    // REPORT 21: PV ITEM FULFILLMENT (item-level ordered vs received vs pending)
    // ========================================================================
    @GetMapping("/pv-item-fulfillment")
    @Operation(summary = "PV Item Fulfillment Report",
            description = "Per-item fulfillment status for Purchase Vouchers: ordered qty vs received qty vs pending qty. " +
                    "Use for delivery tracking and supplier follow-up.")
    public ResponseEntity<ApiResponse<List<PvItemFulfillmentReportDto>>> getPvItemFulfillmentReport(
            @Parameter(description = "Purchase Voucher ID to filter")
            @RequestParam(required = false) Long pvId,
            @Parameter(description = "Branch IDs (comma-separated). Omit for all branches.")
            @RequestParam(required = false) List<String> branchIds,
            @Parameter(description = "Supplier ID to filter")
            @RequestParam(required = false) String suppId,
            @Parameter(description = "Start date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "PV status: DRAFT, PENDING_APPROVAL, POSTED, PARTIAL, COMPLETED")
            @RequestParam(required = false) String status
    ) {
        log.info("GET /reports/pv-item-fulfillment - pvId: {}, branches: {}, suppId: {}", pvId, branchIds, suppId);

        ReportFilterDto filter = ReportFilterDto.builder()
                .pvId(pvId)
                .branchIds(branchIds)
                .suppId(suppId)
                .fromDate(fromDate)
                .toDate(toDate)
                .status(status)
                .build();

        return ResponseEntity.ok(ApiResponse.success(reportService.getPvItemFulfillmentReport(filter)));
    }

    // ========================================================================
    // REPORT: ITEM-WISE STOCK POSITION (BRANCH + GODOWN)
    // ========================================================================
    @GetMapping("/item-stock-position")
    @Operation(summary = "Item-wise Stock Position Report",
            description = "Shows quantity of each item at every storage location — both internal branch locations " +
                    "and external supplier godowns — in a single unified view. " +
                    "locationType='BRANCH' for warehouse rows, 'GODOWN' for supplier-held stock.")
    public ResponseEntity<ApiResponse<List<ItemStockPositionDto>>> getItemStockPosition(
            @Parameter(description = "Item ID to filter")
            @RequestParam(required = false) String itemId,
            @Parameter(description = "Branch IDs to filter branch rows (comma-separated). Omit for all branches.")
            @RequestParam(required = false) List<String> branchIds,
            @Parameter(description = "Supplier ID to filter godown rows")
            @RequestParam(required = false) String suppId
    ) {
        log.info("GET /reports/item-stock-position - item: {}, branches: {}, suppId: {}", itemId, branchIds, suppId);

        ReportFilterDto filter = ReportFilterDto.builder()
                .itemId(itemId)
                .branchIds(branchIds)
                .suppId(suppId)
                .build();

        return ResponseEntity.ok(ApiResponse.success(reportService.getItemStockPositionReport(filter)));
    }

    // ========================================================================
    // GODOWN SUMMARY (TallyPrime-style location summary)
    // ========================================================================
    @GetMapping("/godown-summary")
    @Operation(summary = "Godown / Location Summary",
            description = "TallyPrime-style godown summary: opening balance, inwards, outwards, closing balance " +
                    "with qty+rate+value per item for a specific branch. Optionally filter by location.")
    public ResponseEntity<ApiResponse<GodownSummaryResponse>> getGodownSummary(
            @Parameter(description = "Branch ID (required)")
            @RequestParam String branchId,
            @Parameter(description = "Location ID to filter to a specific godown. Omit for all locations in branch.")
            @RequestParam(required = false) String locationId,
            @Parameter(description = "Start date (inclusive). Defaults to start of current month.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date (inclusive). Defaults to today.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        log.info("GET /reports/godown-summary - branch: {}, location: {}, dates: {} to {}",
                branchId, locationId, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(
                reportService.getGodownSummary(branchId, locationId, fromDate, toDate)));
    }

    // ========================================================================
    // GODOWN VOUCHER (TallyPrime-style per-item, per-location transaction ledger)
    // ========================================================================
    @GetMapping("/godown-voucher")
    @Operation(summary = "Godown Voucher (Location Ledger)",
            description = "TallyPrime-style per-item, per-godown transaction ledger showing date, " +
                    "particulars, vch type, vch no, inwards qty/rate/value, outwards qty/rate/value, " +
                    "and running closing qty/rate/value. Includes opening balance and period totals.")
    public ResponseEntity<ApiResponse<GodownVoucherResponse>> getGodownVoucher(
            @Parameter(description = "Item ID (required)")
            @RequestParam String itemId,
            @Parameter(description = "Godown / Location ID (required)")
            @RequestParam String godownId,
            @Parameter(description = "Start date (inclusive). Defaults to start of current month.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date (inclusive). Defaults to today.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        log.info("GET /reports/godown-voucher - item: {}, godown: {}, dates: {} to {}",
                itemId, godownId, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(
                reportService.getGodownVoucher(itemId, godownId, fromDate, toDate)));
    }

    // ========================================================================
    // SUPPLIER GODOWN SUMMARY (TallyPrime-style third-party godown summary)
    // ========================================================================
    @GetMapping("/supplier-godown-summary")
    @Operation(summary = "Supplier Godown Summary",
            description = "TallyPrime-style godown summary for a supplier's third-party godown. " +
                    "Shows opening, inwards (Job Work Out), outwards (Job Work In), and closing " +
                    "per item grouped by item group. Defaults to current month if no dates provided.")
    public ResponseEntity<ApiResponse<SupplierGodownSummaryResponse>> getSupplierGodownSummary(
            @Parameter(description = "Supplier ID (required)")
            @RequestParam String suppId,
            @Parameter(description = "Godown ID (supplier_godown_map.id) to narrow to a specific godown.")
            @RequestParam(required = false) Long godownId,
            @Parameter(description = "Item ID to filter to a specific item.")
            @RequestParam(required = false) String itemId,
            @Parameter(description = "Start date (inclusive). Defaults to start of current month.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date (inclusive). Defaults to today.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        log.info("GET /reports/supplier-godown-summary - suppId: {}, godownId: {}, itemId: {}, dates: {} to {}",
                suppId, godownId, itemId, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(
                reportService.getSupplierGodownSummary(suppId, godownId, itemId, fromDate, toDate)));
    }

    // ========================================================================
    // SUPPLIER GODOWN VOUCHER (TallyPrime-style third-party godown transaction ledger)
    // ========================================================================
    @GetMapping("/supplier-godown-voucher")
    @Operation(summary = "Supplier Godown Voucher",
            description = "Transaction-by-transaction ledger for a specific item at a supplier's godown. " +
                    "Inwards = goods sent to supplier (Delivery Note / Job Work Out). " +
                    "Outwards = goods received back (Receipt Note / Job Work In). " +
                    "Shows running closing balance with opening balance and period totals.")
    public ResponseEntity<ApiResponse<SupplierGodownVoucherResponse>> getSupplierGodownVoucher(
            @Parameter(description = "Supplier ID (required)")
            @RequestParam String suppId,
            @Parameter(description = "Item ID (required)")
            @RequestParam String itemId,
            @Parameter(description = "Godown ID (supplier_godown_map.id) to narrow to a specific godown.")
            @RequestParam(required = false) Long godownId,
            @Parameter(description = "Start date (inclusive). Defaults to start of current month.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date (inclusive). Defaults to today.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        log.info("GET /reports/supplier-godown-voucher - suppId: {}, godownId: {}, itemId: {}, dates: {} to {}",
                suppId, godownId, itemId, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(
                reportService.getSupplierGodownVoucher(suppId, godownId, itemId, fromDate, toDate)));
    }
}
