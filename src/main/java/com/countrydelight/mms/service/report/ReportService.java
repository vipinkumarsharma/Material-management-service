package com.countrydelight.mms.service.report;

import com.countrydelight.mms.dto.report.AuditExceptionReportDto;
import com.countrydelight.mms.dto.report.BranchReportDto;
import com.countrydelight.mms.dto.report.ConsolidatedStockSummaryDto;
import com.countrydelight.mms.dto.report.CurrentStockReportDto;
import com.countrydelight.mms.dto.report.FifoConsumptionReportDto;
import com.countrydelight.mms.dto.report.GrnDetailReportDto;
import com.countrydelight.mms.dto.report.GodownSummaryDto;
import com.countrydelight.mms.dto.report.GodownSummaryResponse;
import com.countrydelight.mms.dto.report.GodownVoucherEntryDto;
import com.countrydelight.mms.dto.report.GodownVoucherResponse;
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
import com.countrydelight.mms.dto.report.PvVsGrnReportDto;
import com.countrydelight.mms.dto.report.PurchaseOrderItemDto;
import com.countrydelight.mms.dto.report.ReceiptNoteItemDto;
import com.countrydelight.mms.dto.report.ReportFilterDto;
import com.countrydelight.mms.dto.report.StockAgingReportDto;
import com.countrydelight.mms.dto.report.StockLedgerReportDto;
import com.countrydelight.mms.dto.report.StockStatementDto;
import com.countrydelight.mms.dto.report.StockSummaryReportDto;
import com.countrydelight.mms.dto.report.SupplierGodownSummaryResponse;
import com.countrydelight.mms.dto.report.SupplierGodownVoucherResponse;
import com.countrydelight.mms.dto.report.SupplierReportDetailDto;
import com.countrydelight.mms.dto.report.SupplierReportSummaryDto;
import com.countrydelight.mms.entity.master.CompanyMaster;
import com.countrydelight.mms.entity.master.VoucherTypeMaster;
import com.countrydelight.mms.exception.MmsException;
import com.countrydelight.mms.repository.master.BranchMasterRepository;
import com.countrydelight.mms.repository.master.CompanyMasterRepository;
import com.countrydelight.mms.repository.master.LocationMasterRepository;
import com.countrydelight.mms.repository.master.SupplierMasterRepository;
import com.countrydelight.mms.repository.master.VoucherTypeMasterRepository;
import com.countrydelight.mms.repository.report.ReportRepository;
import org.springframework.data.domain.Sort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for all reports.
 * All reports support:
 * - Single branch view (branchIds with one element)
 * - Multiple branch view (branchIds with multiple elements)
 * - Company-wide view (branchIds is null or empty)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final BranchMasterRepository branchMasterRepository;
    private final CompanyMasterRepository companyMasterRepository;
    private final LocationMasterRepository locationMasterRepository;
    private final VoucherTypeMasterRepository voucherTypeMasterRepository;
    private final SupplierMasterRepository supplierMasterRepository;

    private static final Set<String> IN_CATEGORIES = Set.of(
            "Receipt Note", "Material In", "Rejections In", "Job Work In Order");
    private static final Set<String> OUT_CATEGORIES = Set.of(
            "Delivery Note", "Material Out", "Rejections Out", "Job Work Out Order");

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter REPORT_DATE_FMT = DateTimeFormatter.ofPattern("d-MMM-yy", java.util.Locale.ROOT);

    // ========================================================================
    // REPORT 1: CURRENT STOCK REPORT
    // ========================================================================
    /**
     * Business Purpose: Shows current stock on hand at each branch/location.
     * Used by: Store team for daily operations, Management for inventory visibility.
     */
    public List<CurrentStockReportDto> getCurrentStockReport(ReportFilterDto filter) {
        log.info("Generating Current Stock Report with filter: {}", filter);

        List<String> branchIds = filter.hasBranchFilter() ? filter.getBranchIds() : null;
        List<Object[]> results = reportRepository.findCurrentStock(
                safeIds(branchIds),
                branchFilter(branchIds),
                filter.getItemId(),
                filter.getLocationId()
        );

        return results.stream()
                .map(this::mapToCurrentStockDto)
                .collect(Collectors.toList());
    }

    private CurrentStockReportDto mapToCurrentStockDto(Object[] row) {
        return CurrentStockReportDto.builder()
                .branchId((String) row[0])
                .branchName((String) row[1])
                .itemId((String) row[2])
                .itemDesc((String) row[3])
                .locationId((String) row[4])
                .locationName((String) row[5])
                .qtyOnHand(toBigDecimal(row[6]))
                .avgCost(toBigDecimal(row[7]))
                .stockValue(toBigDecimal(row[8]))
                .build();
    }

    // ========================================================================
    // REPORT 2: CONSOLIDATED STOCK SUMMARY
    // ========================================================================
    /**
     * Business Purpose: Shows total stock per item across all branches with branch-wise breakup.
     * Used by: Management for company-wide inventory overview, Finance for valuation.
     */
    public List<ConsolidatedStockSummaryDto> getConsolidatedStockSummary(ReportFilterDto filter) {
        log.info("Generating Consolidated Stock Summary with filter: {}", filter);

        List<String> branchIds = filter.hasBranchFilter() ? filter.getBranchIds() : null;

        // Get consolidated totals
        List<Object[]> summaryResults = reportRepository.findConsolidatedStockSummary(safeIds(branchIds), branchFilter(branchIds));

        // Get branch-wise breakup
        List<Object[]> breakupResults = reportRepository.findBranchWiseStockBreakup(safeIds(branchIds), branchFilter(branchIds));

        // Group breakup by item
        Map<String, List<ConsolidatedStockSummaryDto.BranchBreakup>> breakupMap = breakupResults.stream()
                .collect(Collectors.groupingBy(
                        row -> (String) row[0],
                        Collectors.mapping(row -> ConsolidatedStockSummaryDto.BranchBreakup.builder()
                                .branchId((String) row[1])
                                .branchName((String) row[2])
                                .qty(toBigDecimal(row[3]))
                                .value(toBigDecimal(row[4]))
                                .build(), Collectors.toList())
                ));

        return summaryResults.stream()
                .map(row -> ConsolidatedStockSummaryDto.builder()
                        .itemId((String) row[0])
                        .itemDesc((String) row[1])
                        .totalQty(toBigDecimal(row[2]))
                        .totalValue(toBigDecimal(row[3]))
                        .branchBreakup(breakupMap.getOrDefault((String) row[0], Collections.emptyList()))
                        .build())
                .collect(Collectors.toList());
    }

    // ========================================================================
    // REPORT 3: STOCK LEDGER REPORT
    // ========================================================================
    /**
     * Business Purpose: Complete audit trail of all stock movements.
     * Used by: Auditors, Finance for reconciliation, Operations for investigation.
     */
    public List<StockLedgerReportDto> getStockLedgerReport(ReportFilterDto filter) {
        log.info("Generating Stock Ledger Report with filter: {}", filter);

        List<String> branchIds = filter.hasBranchFilter() ? filter.getBranchIds() : null;
        List<Object[]> results = reportRepository.findStockLedger(
                safeIds(branchIds),
                branchFilter(branchIds),
                filter.getItemId(),
                filter.getFromDate(),
                filter.getToDate(),
                filter.getStatus(), // using status as txnType filter
                filter.getDeptId()
        );

        return results.stream()
                .map(this::mapToStockLedgerDto)
                .collect(Collectors.toList());
    }

    private StockLedgerReportDto mapToStockLedgerDto(Object[] row) {
        return StockLedgerReportDto.builder()
                .ledgerId(toLong(row[0]))
                .txnDate(toLocalDate(row[1]))
                .branchId((String) row[2])
                .branchName((String) row[3])
                .itemId((String) row[4])
                .itemDesc((String) row[5])
                .locationId((String) row[6])
                .locationName((String) row[7])
                .txnType((String) row[8])
                .refId(toLong(row[9]))
                .qtyIn(toBigDecimal(row[10]))
                .qtyOut(toBigDecimal(row[11]))
                .rate(toBigDecimal(row[12]))
                .balanceQty(toBigDecimal(row[13]))
                .createdOn(toLocalDateTime(row[14]))
                .build();
    }

    // ========================================================================
    // REPORT 4: STOCK AGING REPORT
    // ========================================================================
    /**
     * Business Purpose: Shows age of current stock based on GRN date.
     * Used by: Operations to prioritize FIFO, Management for dead stock identification.
     * Aging buckets: 0-30, 31-60, 61-90, 90+ days
     */
    public List<StockAgingReportDto> getStockAgingReport(ReportFilterDto filter) {
        log.info("Generating Stock Aging Report with filter: {}", filter);

        List<String> branchIds = filter.hasBranchFilter() ? filter.getBranchIds() : null;
        List<Object[]> results = reportRepository.findStockAging(safeIds(branchIds), branchFilter(branchIds), filter.getItemId());

        return results.stream()
                .map(this::mapToStockAgingDto)
                .collect(Collectors.toList());
    }

    private StockAgingReportDto mapToStockAgingDto(Object[] row) {
        return StockAgingReportDto.builder()
                .branchId((String) row[0])
                .branchName((String) row[1])
                .itemId((String) row[2])
                .itemDesc((String) row[3])
                .locationId((String) row[4])
                .locationName((String) row[5])
                .agingBucket((String) row[6])
                .qty(toBigDecimal(row[7]))
                .rate(toBigDecimal(row[8]))
                .value(toBigDecimal(row[9]))
                .ageDays(toInteger(row[10]))
                .build();
    }

    // ========================================================================
    // REPORT 5: FIFO CONSUMPTION REPORT
    // ========================================================================
    /**
     * Business Purpose: Shows which GRN batches were consumed for each issue.
     * Used by: Auditors for FIFO compliance verification, Finance for cost accuracy.
     */
    public List<FifoConsumptionReportDto> getFifoConsumptionReport(ReportFilterDto filter) {
        log.info("Generating FIFO Consumption Report with filter: {}", filter);

        List<String> branchIds = filter.hasBranchFilter() ? filter.getBranchIds() : null;
        List<Object[]> results = reportRepository.findFifoConsumption(
                safeIds(branchIds),
                branchFilter(branchIds),
                filter.getItemId(),
                filter.getFromDate(),
                filter.getToDate()
        );

        // Group by issue_id + item_id
        Map<String, List<Object[]>> grouped = results.stream()
                .collect(Collectors.groupingBy(row -> row[0] + "_" + row[4]));

        return grouped.entrySet().stream()
                .map(entry -> {
                    Object[] first = entry.getValue().get(0);
                    List<FifoConsumptionReportDto.GrnConsumption> consumptions = entry.getValue().stream()
                            .map(row -> FifoConsumptionReportDto.GrnConsumption.builder()
                                    .grnId(toLong(row[8]))
                                    .grnDate(toLocalDate(row[9]))
                                    .qtyConsumed(toBigDecimal(row[10]))
                                    .rate(toBigDecimal(row[11]))
                                    .value(toBigDecimal(row[12]))
                                    .build())
                            .collect(Collectors.toList());

                    return FifoConsumptionReportDto.builder()
                            .issueId(toLong(first[0]))
                            .issueDate(toLocalDate(first[1]))
                            .branchId((String) first[2])
                            .branchName((String) first[3])
                            .itemId((String) first[4])
                            .itemDesc((String) first[5])
                            .issuedQty(toBigDecimal(first[6]))
                            .weightedAvgRate(toBigDecimal(first[7]))
                            .grnConsumptions(consumptions)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ========================================================================
    // REPORT 6: GRN SUMMARY REPORT
    // ========================================================================
    /**
     * Business Purpose: Overview of all goods received.
     * Used by: Store for receipt tracking, Purchase for supplier delivery monitoring.
     */
    public List<GrnSummaryReportDto> getGrnSummaryReport(ReportFilterDto filter) {
        log.info("Generating GRN Summary Report with filter: {}", filter);

        List<String> branchIds = filter.hasBranchFilter() ? filter.getBranchIds() : null;
        String suppId = filter.getSuppId();
        if (suppId == null && filter.getSuppIds() != null && !filter.getSuppIds().isEmpty()) {
            suppId = filter.getSuppIds().get(0); // Use first supplier if list provided
        }

        List<Object[]> results = reportRepository.findGrnSummary(
                safeIds(branchIds),
                branchFilter(branchIds),
                suppId,
                filter.getFromDate(),
                filter.getToDate(),
                filter.getStatus(),
                filter.getDeptId()
        );

        return results.stream()
                .map(this::mapToGrnSummaryDto)
                .collect(Collectors.toList());
    }

    private GrnSummaryReportDto mapToGrnSummaryDto(Object[] row) {
        return GrnSummaryReportDto.builder()
                .grnId(toLong(row[0]))
                .branchId((String) row[1])
                .branchName((String) row[2])
                .suppId((String) row[3])
                .suppName((String) row[4])
                .invoiceNo((String) row[5])
                .grnDate(toLocalDate(row[6]))
                .totalQty(toBigDecimal(row[7]))
                .totalValue(toBigDecimal(row[8]))
                .status((String) row[9])
                .createdBy((String) row[10])
                .approvedBy((String) row[11])
                .build();
    }

    // ========================================================================
    // REPORT 7: GRN VS INVOICE COMPARISON
    // ========================================================================
    /**
     * Business Purpose: Identify discrepancies between GRN value and supplier invoice.
     * Used by: Finance for three-way matching, Audit for fraud detection.
     */
    public List<GrnVsInvoiceReportDto> getGrnVsInvoiceReport(ReportFilterDto filter) {
        log.info("Generating GRN vs Invoice Report with filter: {}", filter);

        List<String> branchIds = filter.hasBranchFilter() ? filter.getBranchIds() : null;
        List<Object[]> results = reportRepository.findGrnVsInvoice(
                safeIds(branchIds),
                branchFilter(branchIds),
                filter.getSuppId(),
                filter.getFromDate(),
                filter.getToDate(),
                filter.getDeptId()
        );

        return results.stream()
                .map(this::mapToGrnVsInvoiceDto)
                .collect(Collectors.toList());
    }

    private GrnVsInvoiceReportDto mapToGrnVsInvoiceDto(Object[] row) {
        return GrnVsInvoiceReportDto.builder()
                .grnId(toLong(row[0]))
                .branchId((String) row[1])
                .branchName((String) row[2])
                .suppId((String) row[3])
                .suppName((String) row[4])
                .invoiceNo((String) row[5])
                .invoiceDate(toLocalDate(row[6]))
                .invoiceAmount(toBigDecimal(row[7]))
                .grnAmount(toBigDecimal(row[8]))
                .difference(toBigDecimal(row[9]))
                .differencePercent(toBigDecimal(row[10]))
                .approvalStatus((String) row[11])
                .approvedBy((String) row[12])
                .build();
    }

    // ========================================================================
    // REPORT 8: PRICE VARIANCE REPORT
    // ========================================================================
    /**
     * Business Purpose: Identify price changes from reference price across branches.
     * Used by: Purchase for price negotiation, Finance for cost control, Audit.
     */
    public List<PriceVarianceReportDto> getPriceVarianceReport(ReportFilterDto filter) {
        log.info("Generating Price Variance Report with filter: {}", filter);

        List<String> branchIds = filter.hasBranchFilter() ? filter.getBranchIds() : null;
        List<Object[]> results = reportRepository.findPriceVariance(
                safeIds(branchIds),
                branchFilter(branchIds),
                filter.getItemId(),
                filter.getSuppId(),
                filter.getFromDate(),
                filter.getToDate(),
                filter.getMinVariancePercent()
        );

        return results.stream()
                .map(this::mapToPriceVarianceDto)
                .collect(Collectors.toList());
    }

    private PriceVarianceReportDto mapToPriceVarianceDto(Object[] row) {
        return PriceVarianceReportDto.builder()
                .grnId(toLong(row[0]))
                .grnDate(toLocalDate(row[1]))
                .branchId((String) row[2])
                .branchName((String) row[3])
                .itemId((String) row[4])
                .itemDesc((String) row[5])
                .suppId((String) row[6])
                .suppName((String) row[7])
                .lastReferencePrice(toBigDecimal(row[8]))
                .enteredPrice(toBigDecimal(row[9]))
                .varianceAmount(toBigDecimal(row[10]))
                .variancePercent(toBigDecimal(row[11]))
                .status((String) row[12])
                .approvedBy((String) row[13])
                .approvalDate(toLocalDateTime(row[14]))
                .build();
    }

    // ========================================================================
    // REPORT 9: PO VS GRN REPORT
    // ========================================================================
    /**
     * Business Purpose: Track PO fulfillment status.
     * Used by: Purchase for follow-up, Store for pending delivery awareness.
     */
    public List<PoVsGrnReportDto> getPoVsGrnReport(ReportFilterDto filter) {
        log.info("Generating PO vs GRN Report with filter: {}", filter);

        List<String> branchIds = filter.hasBranchFilter() ? filter.getBranchIds() : null;
        List<Object[]> results = reportRepository.findPoVsGrn(
                safeIds(branchIds),
                branchFilter(branchIds),
                filter.getSuppId(),
                filter.getItemId(),
                filter.getFromDate(),
                filter.getToDate(),
                filter.getStatus()
        );

        return results.stream()
                .map(this::mapToPoVsGrnDto)
                .collect(Collectors.toList());
    }

    private PoVsGrnReportDto mapToPoVsGrnDto(Object[] row) {
        return PoVsGrnReportDto.builder()
                .poId(toLong(row[0]))
                .poDate(toLocalDate(row[1]))
                .branchId((String) row[2])
                .branchName((String) row[3])
                .suppId((String) row[4])
                .suppName((String) row[5])
                .itemId((String) row[6])
                .itemDesc((String) row[7])
                .qtyOrdered(toBigDecimal(row[8]))
                .qtyReceived(toBigDecimal(row[9]))
                .pendingQty(toBigDecimal(row[10]))
                .poRate(toBigDecimal(row[11]))
                .poStatus((String) row[12])
                .fulfillmentPercent(toBigDecimal(row[13]))
                .build();
    }

    // ========================================================================
    // REPORT 10: INTER-BRANCH TRANSFER REPORT
    // ========================================================================
    /**
     * Business Purpose: Track stock movements between branches.
     * Used by: Operations for logistics, Finance for inter-branch reconciliation.
     */
    public List<InterBranchTransferReportDto> getInterBranchTransferReport(ReportFilterDto filter) {
        log.info("Generating Inter-Branch Transfer Report with filter: {}", filter);

        List<String> branchIds = filter.hasBranchFilter() ? filter.getBranchIds() : null;
        List<Object[]> results = reportRepository.findInterBranchTransfers(
                safeIds(branchIds),
                branchFilter(branchIds),
                filter.getItemId(),
                filter.getFromDate(),
                filter.getToDate(),
                filter.getStatus(),
                filter.getDeptId()
        );

        return results.stream()
                .map(this::mapToInterBranchTransferDto)
                .collect(Collectors.toList());
    }

    private InterBranchTransferReportDto mapToInterBranchTransferDto(Object[] row) {
        return InterBranchTransferReportDto.builder()
                .transferId(toLong(row[0]))
                .transferDate(toLocalDate(row[1]))
                .branchId((String) row[2])
                .branchName((String) row[3])
                .fromDeptId(row[4] != null ? ((Number) row[4]).intValue() : null)
                .fromDeptName((String) row[5])
                .toDeptId(row[6] != null ? ((Number) row[6]).intValue() : null)
                .toDeptName((String) row[7])
                .itemId((String) row[8])
                .itemDesc((String) row[9])
                .qtyTransferred(toBigDecimal(row[10]))
                .rate(toBigDecimal(row[11]))
                .value(toBigDecimal(row[12]))
                .status((String) row[13])
                .createdBy((String) row[14])
                .build();
    }

    // ========================================================================
    // REPORT 11: ISSUE TO PRODUCTION REPORT
    // ========================================================================
    /**
     * Business Purpose: Track materials consumed by production.
     * Used by: Production for consumption tracking, Finance for cost allocation.
     */
    public List<IssueToProductionReportDto> getIssueToProductionReport(ReportFilterDto filter) {
        log.info("Generating Issue to Production Report with filter: {}", filter);

        List<String> branchIds = filter.hasBranchFilter() ? filter.getBranchIds() : null;
        List<Object[]> results = reportRepository.findIssueToProduction(
                safeIds(branchIds),
                branchFilter(branchIds),
                filter.getItemId(),
                filter.getFromDate(),
                filter.getToDate(),
                filter.getStatus(),
                filter.getDeptId()
        );

        return results.stream()
                .map(this::mapToIssueToProductionDto)
                .collect(Collectors.toList());
    }

    private IssueToProductionReportDto mapToIssueToProductionDto(Object[] row) {
        return IssueToProductionReportDto.builder()
                .issueId(toLong(row[0]))
                .issueDate(toLocalDate(row[1]))
                .branchId((String) row[2])
                .branchName((String) row[3])
                .itemId((String) row[4])
                .itemDesc((String) row[5])
                .qtyIssued(toBigDecimal(row[6]))
                .fifoRate(toBigDecimal(row[7]))
                .totalValue(toBigDecimal(row[8]))
                .issuedTo((String) row[9])
                .locationId((String) row[10])
                .locationName((String) row[11])
                .status((String) row[12])
                .deptId(toInteger(row[13]))
                .deptName((String) row[14])
                .issueType((String) row[15])
                .suppId((String) row[16])
                .suppName((String) row[17])
                .build();
    }

    // ========================================================================
    // REPORT 12: NON-MOVING / SLOW-MOVING STOCK
    // ========================================================================
    /**
     * Business Purpose: Identify dead/slow stock for clearance or write-off.
     * Used by: Operations for inventory optimization, Finance for provisions.
     * Default: Non-moving = no movement in 90+ days, Slow-moving = 30-90 days
     */
    public List<NonMovingStockReportDto> getNonMovingStockReport(ReportFilterDto filter) {
        log.info("Generating Non-Moving Stock Report with filter: {}", filter);

        List<String> branchIds = filter.hasBranchFilter() ? filter.getBranchIds() : null;
        Integer minDays = filter.getAgingDays() != null ? filter.getAgingDays() : 30;
        Integer nonMovingDays = 90; // threshold for non-moving vs slow-moving

        List<Object[]> results = reportRepository.findNonMovingStock(
                safeIds(branchIds),
                branchFilter(branchIds),
                filter.getItemId(),
                minDays,
                nonMovingDays
        );

        return results.stream()
                .map(this::mapToNonMovingStockDto)
                .collect(Collectors.toList());
    }

    private NonMovingStockReportDto mapToNonMovingStockDto(Object[] row) {
        return NonMovingStockReportDto.builder()
                .branchId((String) row[0])
                .branchName((String) row[1])
                .itemId((String) row[2])
                .itemDesc((String) row[3])
                .locationId((String) row[4])
                .locationName((String) row[5])
                .lastMovementDate(toLocalDate(row[6]))
                .daysSinceLastMovement(toInteger(row[7]))
                .qty(toBigDecimal(row[8]))
                .avgCost(toBigDecimal(row[9]))
                .value(toBigDecimal(row[10]))
                .movementCategory((String) row[11])
                .build();
    }

    // ========================================================================
    // REPORT 13a: SUPPLIER REPORT - SUMMARY
    // ========================================================================
    public List<SupplierReportSummaryDto> getSupplierReportSummary(ReportFilterDto filter) {
        log.info("Generating Supplier Report Summary with filter: {}", filter);

        List<String> branchIds = filter.hasBranchFilter() ? filter.getBranchIds() : null;
        List<Object[]> results = reportRepository.findSupplierReportSummary(
                safeIds(branchIds), branchFilter(branchIds),
                filter.getSuppId(), filter.getItemId(),
                filter.getFromDate(), filter.getToDate(), filter.getDeptId()
        );

        return results.stream()
                .map(row -> SupplierReportSummaryDto.builder()
                        .suppId((String) row[0])
                        .suppName((String) row[1])
                        .itemId((String) row[2])
                        .itemDesc((String) row[3])
                        .totalQtyReceived(toBigDecimal(row[4]))
                        .totalValue(toBigDecimal(row[5]))
                        .avgRate(toBigDecimal(row[6]))
                        .grnCount(toLong(row[7]))
                        .build())
                .collect(Collectors.toList());
    }

    // ========================================================================
    // REPORT 13b: SUPPLIER REPORT - DETAIL
    // ========================================================================
    public List<SupplierReportDetailDto> getSupplierReportDetail(ReportFilterDto filter) {
        log.info("Generating Supplier Report Detail with filter: {}", filter);

        List<String> branchIds = filter.hasBranchFilter() ? filter.getBranchIds() : null;
        List<Object[]> results = reportRepository.findSupplierReportDetail(
                safeIds(branchIds), branchFilter(branchIds),
                filter.getSuppId(), filter.getItemId(),
                filter.getFromDate(), filter.getToDate(), filter.getDeptId()
        );

        return results.stream()
                .map(row -> SupplierReportDetailDto.builder()
                        .suppId((String) row[0])
                        .suppName((String) row[1])
                        .grnId(toLong(row[2]))
                        .grnDate(toLocalDate(row[3]))
                        .pvId(toLong(row[4]))
                        .pvDate(toLocalDate(row[5]))
                        .itemId((String) row[6])
                        .itemDesc((String) row[7])
                        .qtyReceived(toBigDecimal(row[8]))
                        .rate(toBigDecimal(row[9]))
                        .netAmount(toBigDecimal(row[10]))
                        .branchId((String) row[11])
                        .branchName((String) row[12])
                        .invoiceNo((String) row[13])
                        .deptId(toInteger(row[14]))
                        .deptName((String) row[15])
                        .build())
                .collect(Collectors.toList());
    }

    // ========================================================================
    // REPORT 14: AUDIT & EXCEPTION REPORT
    // ========================================================================
    /**
     * Business Purpose: Identify exceptions and anomalies for audit review.
     * Used by: Auditors, Internal control team, Management.
     */
    public List<AuditExceptionReportDto> getAuditExceptionReport(ReportFilterDto filter) {
        log.info("Generating Audit Exception Report with filter: {}", filter);

        List<String> branchIds = filter.hasBranchFilter() ? filter.getBranchIds() : null;
        BigDecimal minVariance = filter.getMinVariancePercent() != null ?
                filter.getMinVariancePercent() : BigDecimal.valueOf(5);

        List<AuditExceptionReportDto> exceptions = new ArrayList<>();

        // 1. GRNs without PO
        List<Object[]> grnWithoutPo = reportRepository.findGrnsWithoutPo(
                safeIds(branchIds), branchFilter(branchIds), filter.getFromDate(), filter.getToDate(), filter.getDeptId());
        exceptions.addAll(grnWithoutPo.stream()
                .map(this::mapToAuditExceptionDto)
                .collect(Collectors.toList()));

        // 2. High price variance approvals
        List<Object[]> highVariance = reportRepository.findHighVarianceApprovals(
                safeIds(branchIds), branchFilter(branchIds), filter.getFromDate(), filter.getToDate(), minVariance, filter.getDeptId());
        exceptions.addAll(highVariance.stream()
                .map(this::mapToAuditExceptionDto)
                .collect(Collectors.toList()));

        // 3. Transfer shortages
        List<Object[]> shortages = reportRepository.findTransferShortages(
                safeIds(branchIds), branchFilter(branchIds), filter.getFromDate(), filter.getToDate());
        exceptions.addAll(shortages.stream()
                .map(this::mapToAuditExceptionDto)
                .collect(Collectors.toList()));

        // Sort by severity and date
        exceptions.sort((a, b) -> {
            int severityCompare = getSeverityOrder(b.getSeverity()) - getSeverityOrder(a.getSeverity());
            if (severityCompare != 0) {
                return severityCompare;
            }
            return b.getTxnDate().compareTo(a.getTxnDate());
        });

        return exceptions;
    }

    private AuditExceptionReportDto mapToAuditExceptionDto(Object[] row) {
        return AuditExceptionReportDto.builder()
                .exceptionType((String) row[0])
                .branchId((String) row[1])
                .branchName((String) row[2])
                .refId(toLong(row[3]))
                .refType((String) row[4])
                .txnDate(toLocalDate(row[5]))
                .description((String) row[6])
                .amount(toBigDecimal(row[7]))
                .variancePercent(toBigDecimal(row[8]))
                .approvedBy((String) row[9])
                .approvalDate(toLocalDateTime(row[10]))
                .severity((String) row[11])
                .build();
    }

    private int getSeverityOrder(String severity) {
        return switch (severity) {
            case "CRITICAL" -> 4;
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> 0;
        };
    }

    // ========================================================================
    // REPORT 15: STOCK STATEMENT (MOVEMENT SUMMARY)
    // ========================================================================
    /**
     * Business Purpose: Shows opening balance, inward, outward, and closing balance for a date range.
     * Used by: Management for period-end stock reconciliation, Finance for inventory valuation.
     */
    public List<StockStatementDto> getStockStatementReport(ReportFilterDto filter) {
        log.info("Generating Stock Statement Report with filter: {}", filter);

        if (filter.getFromDate() == null) {
            filter.setFromDate(LocalDate.now(IST).withDayOfMonth(1));
        }
        if (filter.getToDate() == null) {
            filter.setToDate(LocalDate.now(IST));
        }

        List<String> branchIds = filter.hasBranchFilter() ? filter.getBranchIds() : null;

        // Items with movement in the date range
        List<Object[]> movementResults = reportRepository.findStockStatement(
                safeIds(branchIds),
                branchFilter(branchIds),
                filter.getItemId(),
                filter.getLocationId(),
                filter.getFromDate(),
                filter.getToDate(),
                filter.getDeptId()
        );

        List<StockStatementDto> results = new ArrayList<>(
                movementResults.stream()
                        .map(this::mapToStockStatementDto)
                        .collect(Collectors.toList())
        );

        // Items with opening balance but no movement in the date range
        List<Object[]> openingOnlyResults = reportRepository.findItemsWithOpeningOnly(
                safeIds(branchIds),
                branchFilter(branchIds),
                filter.getItemId(),
                filter.getLocationId(),
                filter.getFromDate(),
                filter.getToDate(),
                filter.getDeptId()
        );

        openingOnlyResults.forEach(row -> {
            BigDecimal openingQty = toBigDecimal(row[6]);
            results.add(StockStatementDto.builder()
                    .branchId((String) row[0])
                    .branchName((String) row[1])
                    .itemId((String) row[2])
                    .itemDesc((String) row[3])
                    .locationId((String) row[4])
                    .locationName((String) row[5])
                    .openingQty(openingQty)
                    .openingValue(BigDecimal.ZERO)
                    .inwardQty(BigDecimal.ZERO)
                    .inwardValue(BigDecimal.ZERO)
                    .outwardQty(BigDecimal.ZERO)
                    .outwardValue(BigDecimal.ZERO)
                    .closingQty(openingQty)
                    .closingValue(BigDecimal.ZERO)
                    .build());
        });

        // Sort combined results by branch and item
        results.sort((a, b) -> {
            int branchCompare = a.getBranchId().compareTo(b.getBranchId());
            if (branchCompare != 0) {
                return branchCompare;
            }
            return a.getItemDesc().compareTo(b.getItemDesc());
        });

        return results;
    }

    private StockStatementDto mapToStockStatementDto(Object[] row) {
        BigDecimal openingQty = toBigDecimal(row[6]);
        BigDecimal inwardQty = toBigDecimal(row[7]);
        BigDecimal outwardQty = toBigDecimal(row[8]);
        BigDecimal closingQty = openingQty.add(inwardQty).subtract(outwardQty);

        return StockStatementDto.builder()
                .branchId((String) row[0])
                .branchName((String) row[1])
                .itemId((String) row[2])
                .itemDesc((String) row[3])
                .locationId((String) row[4])
                .locationName((String) row[5])
                .openingQty(openingQty)
                .openingValue(BigDecimal.ZERO)
                .inwardQty(inwardQty)
                .inwardValue(BigDecimal.ZERO)
                .outwardQty(outwardQty)
                .outwardValue(BigDecimal.ZERO)
                .closingQty(closingQty)
                .closingValue(BigDecimal.ZERO)
                .build();
    }

    // ========================================================================
    // REPORT 16: GRN DETAIL REPORT
    // ========================================================================
    public List<GrnDetailReportDto> getGrnDetailReport(ReportFilterDto filter) {
        log.info("Generating GRN Detail Report with filter: {}", filter);

        List<String> branchIds = filter.hasBranchFilter() ? filter.getBranchIds() : null;
        String suppId = filter.getSuppId();
        if (suppId == null && filter.getSuppIds() != null && !filter.getSuppIds().isEmpty()) {
            suppId = filter.getSuppIds().get(0);
        }

        List<Object[]> results = reportRepository.findGrnDetail(
                safeIds(branchIds), branchFilter(branchIds),
                suppId, filter.getItemId(),
                filter.getFromDate(), filter.getToDate(),
                filter.getDeptId()
        );

        return results.stream()
                .map(row -> GrnDetailReportDto.builder()
                        .grnId(toLong(row[0]))
                        .grnDate(toLocalDate(row[1]))
                        .branchId((String) row[2])
                        .branchName((String) row[3])
                        .suppId((String) row[4])
                        .suppName((String) row[5])
                        .itemId((String) row[6])
                        .itemDesc((String) row[7])
                        .unitId((String) row[8])
                        .qtyReceived(toBigDecimal(row[9]))
                        .rate(toBigDecimal(row[10]))
                        .grossAmount(toBigDecimal(row[11]))
                        .gstPerc(toBigDecimal(row[12]))
                        .gstAmount(toBigDecimal(row[13]))
                        .discountPerc(toBigDecimal(row[14]))
                        .discountAmount(toBigDecimal(row[15]))
                        .netAmount(toBigDecimal(row[16]))
                        .locationId((String) row[17])
                        .locationName((String) row[18])
                        .invoiceNo((String) row[19])
                        .status((String) row[20])
                        .deptId(toInteger(row[21]))
                        .deptName((String) row[22])
                        .build())
                .collect(Collectors.toList());
    }

    // ========================================================================
    // REPORT: RECEIPT NOTE ITEM-WISE
    // ========================================================================
    public List<ReceiptNoteItemDto> getReceiptNoteItemReport(ReportFilterDto filter) {
        log.info("Generating Receipt Note Item Report with filter: {}", filter);

        String branchId = filter.getSingleBranch();
        String suppId = filter.getSuppId();

        List<Object[]> results = reportRepository.findReceiptNoteItems(
                branchId, suppId, filter.getItemId(),
                filter.getFromDate(), filter.getToDate()
        );

        return results.stream()
                .map(row -> ReceiptNoteItemDto.builder()
                        .grnDate(toLocalDate(row[0]))
                        .grnNo((String) row[1])
                        .branchName((String) row[2])
                        .suppName((String) row[3])
                        .poNo((String) row[4])
                        .poDate(toLocalDate(row[5]))
                        .invNo((String) row[6])
                        .invDate(toLocalDate(row[7]))
                        .itemId((String) row[8])
                        .itemDesc((String) row[9])
                        .unitDesc((String) row[10])
                        .qty(toBigDecimal(row[11]))
                        .basicPrice(toBigDecimal(row[12]))
                        .basicAmount(toBigDecimal(row[13]))
                        .gstRate(toBigDecimal(row[14]))
                        .gstAmount(toBigDecimal(row[15]))
                        .amountWithGst(toBigDecimal(row[16]))
                        .remarks((String) row[17])
                        .build())
                .collect(Collectors.toList());
    }

    // ========================================================================
    // REPORT: PURCHASE ORDER ITEM-WISE
    // ========================================================================
    public List<PurchaseOrderItemDto> getPurchaseOrderItemReport(ReportFilterDto filter, int page, int size) {
        log.info("Generating Purchase Order Item Report with filter: {}", filter);
        int offset = (page - 1) * size;

        List<Object[]> results = reportRepository.findPurchaseOrderItems(
                filter.getSingleBranch(), filter.getSuppId(), filter.getItemId(),
                filter.getFromDate(), filter.getToDate(), size, offset
        );

        return results.stream()
                .map(row -> PurchaseOrderItemDto.builder()
                        .date(toLocalDate(row[0]))
                        .suppName((String) row[1])
                        .locationName((String) row[2])
                        .poNo((String) row[3])
                        .poDate(toLocalDate(row[4]))
                        .itemId((String) row[5])
                        .itemDesc((String) row[6])
                        .unitDesc((String) row[7])
                        .poQty(toBigDecimal(row[8]))
                        .basicPrice(toBigDecimal(row[9]))
                        .basicAmount(toBigDecimal(row[10]))
                        .gstRate(toBigDecimal(row[11]))
                        .gstAmount(toBigDecimal(row[12]))
                        .totalAmount(toBigDecimal(row[13]))
                        .remarks((String) row[14])
                        .build())
                .collect(Collectors.toList());
    }

    // ========================================================================
    // REPORT 17: ITEM REPORT - SUMMARY
    // ========================================================================
    public List<ItemReportSummaryDto> getItemReportSummary(ReportFilterDto filter) {
        log.info("Generating Item Report Summary with filter: {}", filter);

        List<String> branchIds = filter.hasBranchFilter() ? filter.getBranchIds() : null;
        List<Object[]> results = reportRepository.findItemReportSummary(
                safeIds(branchIds), branchFilter(branchIds),
                filter.getItemId(),
                filter.getFromDate(), filter.getToDate(), filter.getDeptId()
        );

        return results.stream()
                .map(row -> ItemReportSummaryDto.builder()
                        .itemId((String) row[0])
                        .itemDesc((String) row[1])
                        .groupId((String) row[2])
                        .subGroupId((String) row[3])
                        .currentStockQty(toBigDecimal(row[4]))
                        .currentStockValue(toBigDecimal(row[5]))
                        .totalGrnQty(toBigDecimal(row[6]))
                        .totalGrnValue(toBigDecimal(row[7]))
                        .totalIssueQty(toBigDecimal(row[8]))
                        .totalIssueValue(toBigDecimal(row[9]))
                        .supplierCount(toLong(row[10]))
                        .lastGrnDate(toLocalDate(row[11]))
                        .lastIssueDate(toLocalDate(row[12]))
                        .build())
                .collect(Collectors.toList());
    }

    // ========================================================================
    // REPORT 18: ITEM REPORT - DETAIL
    // ========================================================================
    public List<ItemReportDetailDto> getItemReportDetail(ReportFilterDto filter) {
        log.info("Generating Item Report Detail with filter: {}", filter);

        List<String> branchIds = filter.hasBranchFilter() ? filter.getBranchIds() : null;
        List<Object[]> results = reportRepository.findItemReportDetail(
                safeIds(branchIds), branchFilter(branchIds),
                filter.getItemId(),
                filter.getFromDate(), filter.getToDate(), filter.getDeptId()
        );

        return results.stream()
                .map(row -> ItemReportDetailDto.builder()
                        .itemId((String) row[0])
                        .itemDesc((String) row[1])
                        .txnType((String) row[2])
                        .txnId(toLong(row[3]))
                        .txnDate(toLocalDate(row[4]))
                        .branchId((String) row[5])
                        .branchName((String) row[6])
                        .qtyIn(toBigDecimal(row[7]))
                        .qtyOut(toBigDecimal(row[8]))
                        .rate(toBigDecimal(row[9]))
                        .value(toBigDecimal(row[10]))
                        .counterparty((String) row[11])
                        .refNo((String) row[12])
                        .build())
                .collect(Collectors.toList());
    }

    // ========================================================================
    // REPORT 19: BRANCH REPORT
    // ========================================================================
    public List<BranchReportDto> getBranchReport(ReportFilterDto filter) {
        log.info("Generating Branch Report with filter: {}", filter);

        List<String> branchIds = filter.hasBranchFilter() ? filter.getBranchIds() : null;

        List<Object[]> results = reportRepository.findBranchReport(
                safeIds(branchIds), branchFilter(branchIds),
                filter.getFromDate(), filter.getToDate(), filter.getDeptId()
        );

        // Get top items per branch
        List<Object[]> topItemsResults = reportRepository.findBranchTopItems(
                safeIds(branchIds), branchFilter(branchIds)
        );
        Map<String, List<BranchReportDto.TopItem>> topItemsMap = topItemsResults.stream()
                .collect(Collectors.groupingBy(
                        row -> (String) row[0],
                        Collectors.mapping(row -> BranchReportDto.TopItem.builder()
                                .itemId((String) row[1])
                                .itemDesc((String) row[2])
                                .stockQty(toBigDecimal(row[3]))
                                .stockValue(toBigDecimal(row[4]))
                                .build(), Collectors.toList())
                ));

        // Get top suppliers per branch
        List<Object[]> topSuppResults = reportRepository.findBranchTopSuppliers(
                safeIds(branchIds), branchFilter(branchIds),
                filter.getFromDate(), filter.getToDate(), filter.getDeptId()
        );
        Map<String, List<BranchReportDto.TopSupplier>> topSuppMap = topSuppResults.stream()
                .collect(Collectors.groupingBy(
                        row -> (String) row[0],
                        Collectors.mapping(row -> BranchReportDto.TopSupplier.builder()
                                .suppId((String) row[1])
                                .suppName((String) row[2])
                                .grnCount(toLong(row[3]))
                                .totalValue(toBigDecimal(row[4]))
                                .build(), Collectors.toList())
                ));

        return results.stream()
                .map(row -> {
                    String branchId = (String) row[0];
                    List<BranchReportDto.TopItem> topItems =
                            topItemsMap.getOrDefault(branchId, Collections.emptyList());
                    List<BranchReportDto.TopSupplier> topSuppliers =
                            topSuppMap.getOrDefault(branchId, Collections.emptyList());

                    return BranchReportDto.builder()
                            .branchId(branchId)
                            .branchName((String) row[1])
                            .currentStockValue(toBigDecimal(row[2]))
                            .currentStockItems(toLong(row[3]))
                            .grnCount(toLong(row[4]))
                            .grnValue(toBigDecimal(row[5]))
                            .issueCount(toLong(row[6]))
                            .issueValue(toBigDecimal(row[7]))
                            .transferInCount(toLong(row[8]))
                            .transferInValue(toBigDecimal(row[9]))
                            .transferOutCount(toLong(row[10]))
                            .transferOutValue(toBigDecimal(row[11]))
                            .topItems(topItems.size() > 5 ? topItems.subList(0, 5) : topItems)
                            .topSuppliers(topSuppliers.size() > 5 ? topSuppliers.subList(0, 5) : topSuppliers)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ========================================================================
    // REPORT 20: PV VS GRN (line-item level, Receipt Note type only)
    // ========================================================================
    public List<PvVsGrnReportDto> getPvVsGrnReport(ReportFilterDto filter, int page, int size) {
        log.info("Generating PV vs GRN Report with filter: {}, page: {}, size: {}", filter, page, size);

        List<String> branchIds = filter.hasBranchFilter() ? filter.getBranchIds() : null;
        int offset = (page - 1) * size;
        List<Object[]> results = reportRepository.findPvVsGrn(
                safeIds(branchIds), branchFilter(branchIds),
                filter.getSuppId(), filter.getFromDate(), filter.getToDate(),
                size, offset
        );

        return results.stream()
                .map(row -> PvVsGrnReportDto.builder()
                        .date(toLocalDate(row[0]))
                        .supplierName((String) row[1])
                        .location((String) row[2])
                        .poNo((String) row[3])
                        .poDate(toLocalDate(row[4]))
                        .itemCode((String) row[5])
                        .itemDescription((String) row[6])
                        .unit((String) row[7])
                        .poQty(toBigDecimal(row[8]))
                        .poBasicPrice(toBigDecimal(row[9]))
                        .poBasicAmount(toBigDecimal(row[10]))
                        .poTotalAmount(toBigDecimal(row[11]))
                        .receivedQty(toBigDecimal(row[12]))
                        .basicPrice(toBigDecimal(row[13]))
                        .basicAmount(toBigDecimal(row[14]))
                        .gstRate(toBigDecimal(row[15]))
                        .gstAmt(toBigDecimal(row[16]))
                        .freightAmt(toBigDecimal(row[17]))
                        .gstOnFreightAmt(toBigDecimal(row[18]))
                        .totalAmount(toBigDecimal(row[19]))
                        .build())
                .collect(Collectors.toList());
    }

    // ========================================================================
    // REPORT 21: PV ITEM FULFILLMENT (item-level ordered vs received vs pending)
    // ========================================================================
    public List<PvItemFulfillmentReportDto> getPvItemFulfillmentReport(ReportFilterDto filter) {
        log.info("Generating PV Item Fulfillment Report with filter: {}", filter);

        List<String> branchIds = filter.hasBranchFilter() ? filter.getBranchIds() : null;
        List<Object[]> results = reportRepository.findPvItemFulfillment(
                safeIds(branchIds), branchFilter(branchIds),
                filter.getPvId(), filter.getSuppId(),
                filter.getFromDate(), filter.getToDate(), filter.getStatus()
        );

        return results.stream()
                .map(row -> PvItemFulfillmentReportDto.builder()
                        .pvId(toLong(row[0]))
                        .pvVoucherNumber((String) row[1])
                        .pvDate(toLocalDate(row[2]))
                        .branchId((String) row[3])
                        .branchName((String) row[4])
                        .suppId((String) row[5])
                        .suppName((String) row[6])
                        .pvStatus((String) row[7])
                        .itemId((String) row[8])
                        .itemDesc((String) row[9])
                        .pvQty(toBigDecimal(row[10]))
                        .pvRate(toBigDecimal(row[11]))
                        .pvLineAmount(toBigDecimal(row[12]))
                        .totalReceivedQty(toBigDecimal(row[13]))
                        .pendingQty(toBigDecimal(row[14]))
                        .fulfillmentPercent(toBigDecimal(row[15]))
                        .build())
                .collect(Collectors.toList());
    }

    // ========================================================================
    // REPORT 22: STOCK SUMMARY REPORT (TallyPrime-style period-end inventory)
    // ========================================================================

    /**
     * Business Purpose: Period-end stock summary grouped by item group, showing all movement
     * sub-types driven by VoucherTypeMaster.reportSummaryTitle, plus closing balance with GST
     * and consumption analytics.
     * Used by: Finance and Management for month-end/quarter-end inventory review.
     */
    public StockSummaryReportDto getStockSummaryReport(ReportFilterDto filter) {
        log.info("Generating Stock Summary Report with filter: {}", filter);

        LocalDate fromDate = filter.getFromDate();
        LocalDate toDate = filter.getToDate();
        if (fromDate == null || toDate == null) {
            throw new MmsException("fromDate and toDate are required for Stock Summary Report");
        }

        int days = (int) (toDate.toEpochDay() - fromDate.toEpochDay() + 1);
        List<String> branchIds = filter.hasBranchFilter() ? filter.getBranchIds() : null;

        // Build ordered column metadata from VoucherTypeMaster (IN first, then OUT)
        List<VoucherTypeMaster> vtWithTitle = voucherTypeMasterRepository
                .findByReportSummaryTitleIsNotNull(Sort.by("voucherTypeName"));
        List<StockSummaryReportDto.ColumnMeta> inCols = vtWithTitle.stream()
                .filter(v -> IN_CATEGORIES.contains(v.getVoucherCategory()))
                .map(v -> StockSummaryReportDto.ColumnMeta.builder()
                        .title(v.getReportSummaryTitle()).movementType("IN").build())
                .collect(Collectors.toList());
        List<StockSummaryReportDto.ColumnMeta> outCols = vtWithTitle.stream()
                .filter(v -> OUT_CATEGORIES.contains(v.getVoucherCategory()))
                .map(v -> StockSummaryReportDto.ColumnMeta.builder()
                        .title(v.getReportSummaryTitle()).movementType("OUT").build())
                .collect(Collectors.toList());
        List<StockSummaryReportDto.ColumnMeta> columns = new ArrayList<>(inCols);
        columns.addAll(outCols);
        Set<String> inTitles = inCols.stream()
                .map(StockSummaryReportDto.ColumnMeta::getTitle).collect(Collectors.toSet());

        List<Object[]> movRows = reportRepository.findStockSummaryMovementsDynamic(
                safeIds(branchIds), branchFilter(branchIds),
                fromDate, toDate, filter.getGroupId(), filter.getItemId()
        );
        List<Object[]> obRows = reportRepository.findStockSummaryOpening(
                safeIds(branchIds), branchFilter(branchIds),
                fromDate, filter.getGroupId(), filter.getItemId()
        );

        // Build opening map keyed by item_id: [qty, rate]; also seed groupDescMap
        Map<String, BigDecimal[]> openingMap = new HashMap<>();
        Map<String, String> groupDescMap = new LinkedHashMap<>();
        for (Object[] row : obRows) {
            String itemId = (String) row[0];
            openingMap.put(itemId, new BigDecimal[]{toBigDecimal(row[2]), toBigDecimal(row[3])});
            groupDescMap.put((String) row[4], (String) row[5]);
        }

        // Pivot movement rows: itemId → (title → [qtyIn, valIn, qtyOut, valOut])
        // itemMetaMap preserves SQL insertion order (group_desc, item_desc)
        Map<String, String[]> itemMetaMap = new LinkedHashMap<>();
        Map<String, Map<String, BigDecimal[]>> itemMovMap = new LinkedHashMap<>();
        for (Object[] row : movRows) {
            String itemId    = (String) row[0];
            String itemDesc  = (String) row[1];
            String groupId   = (String) row[2];
            String groupDesc = (String) row[3];
            String unitId    = (String) row[4];
            String gstStr    = row[5] == null ? "0" : row[5].toString();
            String title     = (String) row[6];
            groupDescMap.put(groupId, groupDesc);
            itemMetaMap.putIfAbsent(itemId, new String[]{itemDesc, groupId, groupDesc, unitId, gstStr});
            itemMovMap.computeIfAbsent(itemId, k -> new LinkedHashMap<>())
                    .put(title, new BigDecimal[]{
                            toBigDecimal(row[8]), toBigDecimal(row[9]),
                            toBigDecimal(row[10]), toBigDecimal(row[11])});
        }

        // Build item rows for items with movements (preserving SQL order)
        Map<String, List<StockSummaryReportDto.ItemRow>> groupItemsMap = new LinkedHashMap<>();
        Set<String> processedItemIds = new HashSet<>(itemMetaMap.keySet());
        for (Map.Entry<String, String[]> entry : itemMetaMap.entrySet()) {
            String itemId = entry.getKey();
            String[] meta = entry.getValue();
            StockSummaryReportDto.ItemRow itemRow = buildDynamicItemRow(
                    itemId, meta, openingMap,
                    itemMovMap.getOrDefault(itemId, Map.of()),
                    columns, inTitles, days);
            groupItemsMap.computeIfAbsent(meta[1], k -> new ArrayList<>()).add(itemRow);
        }

        // Add opening-only items (stock before period, zero movements in period)
        for (Object[] row : obRows) {
            String itemId = (String) row[0];
            if (!processedItemIds.contains(itemId)) {
                String gId = (String) row[4];
                groupItemsMap.computeIfAbsent(gId, k -> new ArrayList<>())
                        .add(buildOpeningOnlyDynamicItemRow(row, columns, days));
            }
        }

        // Build group rows and accumulate for grand total
        List<StockSummaryReportDto.GroupRow> groups = new ArrayList<>();
        List<StockSummaryReportDto.ItemRow> allItems = new ArrayList<>();
        for (Map.Entry<String, List<StockSummaryReportDto.ItemRow>> entry : groupItemsMap.entrySet()) {
            String gId = entry.getKey();
            List<StockSummaryReportDto.ItemRow> items = entry.getValue();
            allItems.addAll(items);
            groups.add(aggregateToDynamicGroupRow(gId, groupDescMap.getOrDefault(gId, gId), items, columns, days));
        }

        StockSummaryReportDto.GroupRow grandTotal =
                aggregateToDynamicGroupRow("TOTAL", "Grand Total", allItems, columns, days);

        return StockSummaryReportDto.builder()
                .reportHeader(buildReportHeader(filter))
                .columns(columns)
                .groups(groups)
                .grandTotal(grandTotal)
                .build();
    }

    // meta: [0]=itemDesc, [1]=groupId, [2]=groupDesc, [3]=unitId, [4]=gstPercStr
    private StockSummaryReportDto.ItemRow buildDynamicItemRow(
            String itemId, String[] meta,
            Map<String, BigDecimal[]> openingMap,
            Map<String, BigDecimal[]> movData,
            List<StockSummaryReportDto.ColumnMeta> columns,
            Set<String> inTitles, int days) {

        String itemDesc = meta[0];
        String groupId  = meta[1];
        String unitId   = meta[3];
        BigDecimal gstPerc = new BigDecimal(meta[4]);

        BigDecimal[] ob = openingMap.getOrDefault(itemId,
                new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
        BigDecimal obQty = ob[0];
        BigDecimal obRate = ob[1];
        BigDecimal obVal = obQty.multiply(obRate);

        Map<String, StockSummaryReportDto.MovementColumn> movements = new LinkedHashMap<>();
        BigDecimal totalInQty = BigDecimal.ZERO, totalInVal = BigDecimal.ZERO;
        BigDecimal totalOutQty = BigDecimal.ZERO, totalOutVal = BigDecimal.ZERO;

        for (StockSummaryReportDto.ColumnMeta col : columns) {
            BigDecimal[] d = movData.getOrDefault(col.getTitle(),
                    new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO});
            if (inTitles.contains(col.getTitle())) {
                movements.put(col.getTitle(), mv(d[0], wavg(d[0], d[1]), d[1]));
                totalInQty = totalInQty.add(d[0]);
                totalInVal = totalInVal.add(d[1]);
            } else {
                movements.put(col.getTitle(), mv(d[2], wavg(d[2], d[3]), d[3]));
                totalOutQty = totalOutQty.add(d[2]);
                totalOutVal = totalOutVal.add(d[3]);
            }
        }

        BigDecimal cbQty = obQty.add(totalInQty).subtract(totalOutQty);
        BigDecimal cbVal = obVal.add(totalInVal).subtract(totalOutVal);
        BigDecimal gstAmt = cbVal.multiply(gstPerc)
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal avgQty = totalOutQty.divide(BigDecimal.valueOf(days), 4, RoundingMode.HALF_UP);
        BigDecimal avgVal = totalOutVal.divide(BigDecimal.valueOf(days), 4, RoundingMode.HALF_UP);
        Integer daysCovered = null;
        if (avgQty.compareTo(BigDecimal.ZERO) > 0) {
            daysCovered = cbQty.divide(avgQty, 0, RoundingMode.HALF_UP).intValue();
        }

        return StockSummaryReportDto.ItemRow.builder()
                .itemId(itemId).itemDesc(itemDesc).groupId(groupId).unitId(unitId)
                .openingBalance(mv(obQty, obRate, obVal))
                .movements(movements)
                .closingBalance(StockSummaryReportDto.ClosingColumn.builder()
                        .qty(cbQty).rate(wavg(cbQty, cbVal)).value(cbVal)
                        .gstPerc(gstPerc).gstAmt(gstAmt).totalAmt(cbVal.add(gstAmt))
                        .build())
                .consumed(mv(totalOutQty, wavg(totalOutQty, totalOutVal), totalOutVal))
                .avgConsumptionPerDay(mv(avgQty, BigDecimal.ZERO, avgVal))
                .daysConsidered(days).daysCovered(daysCovered)
                .build();
    }

    private StockSummaryReportDto.ItemRow buildOpeningOnlyDynamicItemRow(
            Object[] row, List<StockSummaryReportDto.ColumnMeta> columns, int days) {
        // row: [0]=item_id, [1]=item_desc, [2]=opening_qty, [3]=opening_rate,
        //      [4]=group_id, [5]=group_desc, [6]=unit_id, [7]=gst_perc
        BigDecimal obQty  = toBigDecimal(row[2]);
        BigDecimal obRate = toBigDecimal(row[3]);
        BigDecimal obVal  = obQty.multiply(obRate);
        BigDecimal gstPerc = toBigDecimal(row[7]);
        BigDecimal gstAmt = obVal.multiply(gstPerc)
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        Map<String, StockSummaryReportDto.MovementColumn> movements = new LinkedHashMap<>();
        for (StockSummaryReportDto.ColumnMeta col : columns) {
            movements.put(col.getTitle(), mv(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        }

        return StockSummaryReportDto.ItemRow.builder()
                .itemId((String) row[0]).itemDesc((String) row[1])
                .groupId((String) row[4]).unitId((String) row[6])
                .openingBalance(mv(obQty, obRate, obVal))
                .movements(movements)
                .closingBalance(StockSummaryReportDto.ClosingColumn.builder()
                        .qty(obQty).rate(obRate).value(obVal)
                        .gstPerc(gstPerc).gstAmt(gstAmt).totalAmt(obVal.add(gstAmt))
                        .build())
                .consumed(mv(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO))
                .avgConsumptionPerDay(mv(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO))
                .daysConsidered(days).daysCovered(null)
                .build();
    }

    private StockSummaryReportDto.GroupRow aggregateToDynamicGroupRow(
            String groupId, String groupDesc,
            List<StockSummaryReportDto.ItemRow> items,
            List<StockSummaryReportDto.ColumnMeta> columns, int days) {

        BigDecimal zero = BigDecimal.ZERO;
        BigDecimal obQty = items.stream().map(r -> r.getOpeningBalance().getQty()).reduce(zero, BigDecimal::add);
        BigDecimal obVal = items.stream().map(r -> r.getOpeningBalance().getValue()).reduce(zero, BigDecimal::add);

        Map<String, StockSummaryReportDto.MovementColumn> movements = new LinkedHashMap<>();
        BigDecimal totalOutQty = zero, totalOutVal = zero;
        for (StockSummaryReportDto.ColumnMeta col : columns) {
            BigDecimal sumQty = items.stream()
                    .map(r -> r.getMovements().getOrDefault(col.getTitle(), mv(zero, zero, zero)).getQty())
                    .reduce(zero, BigDecimal::add);
            BigDecimal sumVal = items.stream()
                    .map(r -> r.getMovements().getOrDefault(col.getTitle(), mv(zero, zero, zero)).getValue())
                    .reduce(zero, BigDecimal::add);
            movements.put(col.getTitle(), mv(sumQty, wavg(sumQty, sumVal), sumVal));
            if ("OUT".equals(col.getMovementType())) {
                totalOutQty = totalOutQty.add(sumQty);
                totalOutVal = totalOutVal.add(sumVal);
            }
        }

        BigDecimal cbQty    = items.stream().map(r -> r.getClosingBalance().getQty()).reduce(zero, BigDecimal::add);
        BigDecimal cbVal    = items.stream().map(r -> r.getClosingBalance().getValue()).reduce(zero, BigDecimal::add);
        BigDecimal cbGstAmt = items.stream().map(r -> r.getClosingBalance().getGstAmt()).reduce(zero, BigDecimal::add);
        BigDecimal cbTotAmt = items.stream().map(r -> r.getClosingBalance().getTotalAmt()).reduce(zero, BigDecimal::add);

        BigDecimal avgQty = totalOutQty.divide(BigDecimal.valueOf(days), 4, RoundingMode.HALF_UP);
        BigDecimal avgVal = totalOutVal.divide(BigDecimal.valueOf(days), 4, RoundingMode.HALF_UP);
        Integer daysCovered = null;
        if (avgQty.compareTo(zero) > 0) {
            daysCovered = cbQty.divide(avgQty, 0, RoundingMode.HALF_UP).intValue();
        }

        return StockSummaryReportDto.GroupRow.builder()
                .groupId(groupId).groupDesc(groupDesc).items(items)
                .openingBalance(mv(obQty, wavg(obQty, obVal), obVal))
                .movements(movements)
                .closingBalance(StockSummaryReportDto.ClosingColumn.builder()
                        .qty(cbQty).rate(wavg(cbQty, cbVal)).value(cbVal)
                        .gstPerc(null).gstAmt(cbGstAmt).totalAmt(cbTotAmt)
                        .build())
                .consumed(mv(totalOutQty, wavg(totalOutQty, totalOutVal), totalOutVal))
                .avgConsumptionPerDay(mv(avgQty, zero, avgVal))
                .daysConsidered(days).daysCovered(daysCovered)
                .build();
    }

    private StockSummaryReportDto.ReportHeader buildReportHeader(ReportFilterDto filter) {
        LocalDate fromDate = filter.getFromDate();
        LocalDate toDate = filter.getToDate();

        String periodDesc = fromDate.equals(toDate)
                ? "For " + fromDate.format(REPORT_DATE_FMT)
                : fromDate.format(REPORT_DATE_FMT) + " to " + toDate.format(REPORT_DATE_FMT);

        StockSummaryReportDto.ReportHeader.ReportHeaderBuilder builder =
                StockSummaryReportDto.ReportHeader.builder()
                        .reportTitle("Stock Summary")
                        .fromDate(fromDate)
                        .toDate(toDate)
                        .periodDesc(periodDesc);

        List<String> branchIds = filter.getBranchIds();
        if (branchIds != null && branchIds.size() == 1) {
            branchMasterRepository.findById(branchIds.get(0)).ifPresent(branch -> {
                builder.branchName(branch.getBranchName())
                        .address(branch.getAddress1())
                        .pincode(branch.getPincode())
                        .gstNo(branch.getGstNo());
                if (branch.getCompanyId() != null) {
                    companyMasterRepository.findById(branch.getCompanyId())
                            .map(CompanyMaster::getCompanyName)
                            .ifPresent(builder::companyName);
                }
            });
        }

        return builder.build();
    }

    private StockSummaryReportDto.MovementColumn mv(BigDecimal qty, BigDecimal rate, BigDecimal value) {
        return StockSummaryReportDto.MovementColumn.builder()
                .qty(qty != null ? qty : BigDecimal.ZERO)
                .rate(rate != null ? rate : BigDecimal.ZERO)
                .value(value != null ? value : BigDecimal.ZERO)
                .build();
    }

    private BigDecimal wavg(BigDecimal qty, BigDecimal value) {
        if (qty == null || qty.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return value.divide(qty, 4, RoundingMode.HALF_UP);
    }

    // ========================================================================
    // REPORT: ITEM-WISE STOCK POSITION
    // ========================================================================
    /**
     * Business Purpose: Shows how much of each item is held at every storage location —
     * both internal branch locations and external supplier godowns — in one unified view.
     * Used by: Procurement team, Supply chain for total inventory visibility.
     */
    public List<ItemStockPositionDto> getItemStockPositionReport(ReportFilterDto filter) {
        log.info("Generating Item Stock Position Report with filter: {}", filter);

        List<String> branchIds = filter.hasBranchFilter() ? filter.getBranchIds() : null;
        List<Object[]> results = reportRepository.findItemStockPosition(
                safeIds(branchIds),
                branchFilter(branchIds),
                filter.getItemId(),
                filter.getSuppId()
        );

        return results.stream()
                .map(this::mapToItemStockPositionDto)
                .collect(Collectors.toList());
    }

    private ItemStockPositionDto mapToItemStockPositionDto(Object[] row) {
        return ItemStockPositionDto.builder()
                .locationType((String) row[0])
                .itemId((String) row[1])
                .itemDesc((String) row[2])
                .branchId((String) row[3])
                .branchName((String) row[4])
                .locationId((String) row[5])
                .locationName((String) row[6])
                .suppId((String) row[7])
                .suppName((String) row[8])
                .godownId((String) row[9])
                .godownName((String) row[10])
                .qty(toBigDecimal(row[11]))
                .avgCost(row[12] != null ? toBigDecimal(row[12]) : null)
                .stockValue(row[13] != null ? toBigDecimal(row[13]) : null)
                .build();
    }

    // ========================================================================
    // GODOWN SUMMARY
    // ========================================================================
    /**
     * Business Purpose: TallyPrime-style godown/location summary for a branch.
     * Shows per-item: opening balance, inwards, outwards, closing balance with qty + rate + value,
     * grouped by item group with group subtotals and a grand total.
     */
    public GodownSummaryResponse getGodownSummary(String branchId, String locationId,
                                                   LocalDate fromDate, LocalDate toDate) {
        log.info("Generating Godown Summary - branch: {}, location: {}, dates: {} to {}",
                branchId, locationId, fromDate, toDate);

        if (fromDate == null) {
            fromDate = LocalDate.now(IST).withDayOfMonth(1);
        }
        if (toDate == null) {
            toDate = LocalDate.now(IST);
        }

        var branch = branchMasterRepository.findById(branchId)
                .orElseThrow(() -> new MmsException("Branch not found: " + branchId));

        String resolvedLocationName = null;
        if (locationId != null) {
            var location = locationMasterRepository.findById(locationId)
                    .orElseThrow(() -> new MmsException("Location not found: " + locationId));
            resolvedLocationName = location.getLocationName();
        }

        // group_id -> {group_desc, items}  — LinkedHashMap preserves SQL ORDER BY g.group_desc
        Map<String, String> groupDescMap = new LinkedHashMap<>();
        Map<String, List<GodownSummaryDto>> groupItemsMap = new LinkedHashMap<>();

        // Items with movement in range (already ordered by group_desc, item_desc)
        List<Object[]> movementRows = reportRepository.findGodownSummary(branchId, locationId, fromDate, toDate);
        Set<String> seenItemIds = new HashSet<>();
        for (Object[] row : movementRows) {
            GodownSummaryDto dto = mapToGodownSummaryDto(row);
            String gId = row[4] != null ? (String) row[4] : "UNGROUPED";
            String gDesc = row[5] != null ? (String) row[5] : "Ungrouped";
            groupDescMap.put(gId, gDesc);
            groupItemsMap.computeIfAbsent(gId, k -> new ArrayList<>()).add(dto);
            seenItemIds.add(dto.getItemId());
        }

        // Items with opening balance but no movement in range
        List<Object[]> openingOnlyRows = reportRepository.findGodownSummaryOpeningOnly(branchId, locationId, fromDate, toDate);
        for (Object[] row : openingOnlyRows) {
            String itemId = (String) row[0];
            if (seenItemIds.contains(itemId)) {
                continue;
            }
            BigDecimal openingQty   = toBigDecimal(row[6]);
            BigDecimal openingRate  = toBigDecimal(row[7]);
            BigDecimal openingValue = openingQty.multiply(openingRate);
            String gId = row[4] != null ? (String) row[4] : "UNGROUPED";
            String gDesc = row[5] != null ? (String) row[5] : "Ungrouped";
            groupDescMap.put(gId, gDesc);
            groupItemsMap.computeIfAbsent(gId, k -> new ArrayList<>()).add(
                    GodownSummaryDto.builder()
                            .itemId(itemId)
                            .itemDesc((String) row[1])
                            .locationId((String) row[2])
                            .locationName((String) row[3])
                            .openingQty(openingQty)
                            .openingRate(openingRate)
                            .openingValue(openingValue)
                            .inwardQty(BigDecimal.ZERO)
                            .inwardRate(BigDecimal.ZERO)
                            .inwardValue(BigDecimal.ZERO)
                            .outwardQty(BigDecimal.ZERO)
                            .outwardRate(BigDecimal.ZERO)
                            .outwardValue(BigDecimal.ZERO)
                            .closingQty(openingQty)
                            .closingRate(openingRate)
                            .closingValue(openingValue)
                            .build());
        }

        // Build group rows and collect all items for grand total
        List<GodownSummaryResponse.GroupRow> groups = new ArrayList<>();
        List<GodownSummaryDto> allItems = new ArrayList<>();

        for (Map.Entry<String, List<GodownSummaryDto>> entry : groupItemsMap.entrySet()) {
            String gId = entry.getKey();
            List<GodownSummaryDto> gItems = entry.getValue();
            gItems.sort((a, b) -> a.getItemDesc().compareTo(b.getItemDesc()));
            allItems.addAll(gItems);
            groups.add(GodownSummaryResponse.GroupRow.builder()
                    .groupId(gId)
                    .groupDesc(groupDescMap.getOrDefault(gId, gId))
                    .items(gItems)
                    .subTotal(buildGodownTotal(gId, groupDescMap.getOrDefault(gId, gId), gItems))
                    .build());
        }

        return GodownSummaryResponse.builder()
                .branchId(branchId)
                .branchName(branch.getBranchName())
                .locationId(locationId)
                .locationName(resolvedLocationName)
                .fromDate(fromDate)
                .toDate(toDate)
                .groups(groups)
                .grandTotal(buildGodownTotal("GRAND_TOTAL", "Grand Total", allItems))
                .build();
    }

    /** Maps a movement-query row (columns shifted: group_id=[4], group_desc=[5], opening=[6..]) */
    private GodownSummaryDto mapToGodownSummaryDto(Object[] row) {
        BigDecimal openingQty   = toBigDecimal(row[6]);
        BigDecimal openingRate  = toBigDecimal(row[7]);
        BigDecimal openingValue = openingQty.multiply(openingRate);

        BigDecimal inwardQty    = toBigDecimal(row[8]);
        BigDecimal inwardValue  = toBigDecimal(row[9]);
        BigDecimal inwardRate   = inwardQty.compareTo(BigDecimal.ZERO) > 0
                ? inwardValue.divide(inwardQty, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        BigDecimal outwardQty   = toBigDecimal(row[10]);
        BigDecimal outwardValue = toBigDecimal(row[11]);
        BigDecimal outwardRate  = outwardQty.compareTo(BigDecimal.ZERO) > 0
                ? outwardValue.divide(outwardQty, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        BigDecimal closingQty   = openingQty.add(inwardQty).subtract(outwardQty);
        BigDecimal closingValue = openingValue.add(inwardValue).subtract(outwardValue);
        BigDecimal closingRate  = closingQty.compareTo(BigDecimal.ZERO) > 0
                ? closingValue.divide(closingQty, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        return GodownSummaryDto.builder()
                .itemId((String) row[0])
                .itemDesc((String) row[1])
                .locationId((String) row[2])
                .locationName((String) row[3])
                .openingQty(openingQty)
                .openingRate(openingRate)
                .openingValue(openingValue)
                .inwardQty(inwardQty)
                .inwardRate(inwardRate)
                .inwardValue(inwardValue)
                .outwardQty(outwardQty)
                .outwardRate(outwardRate)
                .outwardValue(outwardValue)
                .closingQty(closingQty)
                .closingRate(closingRate)
                .closingValue(closingValue)
                .build();
    }

    /** Builds a subtotal/grand-total GodownSummaryDto by summing qty/value fields across items. */
    private GodownSummaryDto buildGodownTotal(String id, String desc, List<GodownSummaryDto> items) {
        BigDecimal openingQty   = items.stream().map(GodownSummaryDto::getOpeningQty).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal openingValue = items.stream().map(GodownSummaryDto::getOpeningValue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal inwardQty    = items.stream().map(GodownSummaryDto::getInwardQty).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal inwardValue  = items.stream().map(GodownSummaryDto::getInwardValue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal outwardQty   = items.stream().map(GodownSummaryDto::getOutwardQty).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal outwardValue = items.stream().map(GodownSummaryDto::getOutwardValue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal closingQty   = openingQty.add(inwardQty).subtract(outwardQty);
        BigDecimal closingValue = openingValue.add(inwardValue).subtract(outwardValue);

        return GodownSummaryDto.builder()
                .itemId(id)
                .itemDesc(desc)
                .openingQty(openingQty)
                .openingRate(BigDecimal.ZERO)
                .openingValue(openingValue)
                .inwardQty(inwardQty)
                .inwardRate(BigDecimal.ZERO)
                .inwardValue(inwardValue)
                .outwardQty(outwardQty)
                .outwardRate(BigDecimal.ZERO)
                .outwardValue(outwardValue)
                .closingQty(closingQty)
                .closingRate(BigDecimal.ZERO)
                .closingValue(closingValue)
                .build();
    }

    // ========================================================================
    // BRANCH FILTER HELPERS
    // ========================================================================
    /**
     * Returns "Y" when branchIds has values (triggers the IN clause),
     * or null (skips the branch filter entirely).
     * Avoids MySQL "Operand should contain 1 column(s)" error when Hibernate
     * expands a List param inside a (list IS NULL) check.
     */
    private String branchFilter(List<String> ids) {
        return (ids != null && !ids.isEmpty()) ? "Y" : null;
    }

    /**
     * Returns the actual ids when filtering, or a dummy sentinel list
     * when not filtering (the sentinel never matches any real branch_id).
     */
    private List<String> safeIds(List<String> ids) {
        return (ids != null && !ids.isEmpty()) ? ids : List.of("__NO_BRANCH__");
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================
    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return new BigDecimal(value.toString());
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(value.toString());
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(value.toString());
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        if (value instanceof java.sql.Date) {
            return ((java.sql.Date) value).toLocalDate();
        }
        if (value instanceof java.util.Date) {
            return ((java.util.Date) value).toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
        }
        return LocalDate.parse(value.toString());
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        if (value instanceof Timestamp) {
            return ((Timestamp) value).toLocalDateTime();
        }
        return LocalDateTime.parse(value.toString());
    }

    // ========================================================================
    // GODOWN VOUCHER (TallyPrime-style per-item, per-location transaction ledger)
    // ========================================================================
    public GodownVoucherResponse getGodownVoucher(String itemId, String locationId,
                                                   LocalDate fromDate, LocalDate toDate) {
        log.info("Generating Godown Voucher - item: {}, location: {}, dates: {} to {}",
                itemId, locationId, fromDate, toDate);

        if (itemId == null || itemId.isBlank()) {
            throw new MmsException("itemId is required");
        }
        if (locationId == null || locationId.isBlank()) {
            throw new MmsException("godownId is required");
        }

        if (fromDate == null) {
            fromDate = LocalDate.now(IST).withDayOfMonth(1);
        }
        if (toDate == null) {
            toDate = LocalDate.now(IST);
        }

        // Opening balance
        List<Object[]> openingRows = reportRepository.findGodownVoucherOpening(itemId, locationId, fromDate);
        BigDecimal openingQty = BigDecimal.ZERO;
        BigDecimal openingRate = BigDecimal.ZERO;
        BigDecimal openingValue = BigDecimal.ZERO;
        String itemDesc = itemId;
        String godownName = locationId;

        if (!openingRows.isEmpty()) {
            Object[] ob = openingRows.get(0);
            openingQty  = toBigDecimal(ob[0]);
            openingRate = toBigDecimal(ob[1]);
            openingValue = openingQty.multiply(openingRate);
            if (ob[2] != null) {
                itemDesc   = (String) ob[2];
            }
            if (ob[3] != null) {
                godownName = (String) ob[3];
            }
        }

        // Transaction entries
        List<Object[]> rows = reportRepository.findGodownVoucherEntries(itemId, locationId, fromDate, toDate);

        BigDecimal totalInwardsQty   = BigDecimal.ZERO;
        BigDecimal totalInwardsValue = BigDecimal.ZERO;
        BigDecimal totalOutwardsQty  = BigDecimal.ZERO;
        BigDecimal totalOutwardsValue = BigDecimal.ZERO;

        List<GodownVoucherEntryDto> entries = new ArrayList<>();
        for (Object[] row : rows) {
            BigDecimal qtyIn      = toBigDecimal(row[5]);
            BigDecimal qtyOut     = toBigDecimal(row[6]);
            BigDecimal rate       = toBigDecimal(row[7]);
            BigDecimal balanceQty = toBigDecimal(row[8]);

            BigDecimal inwardsValue  = qtyIn.multiply(rate);
            BigDecimal outwardsValue = qtyOut.multiply(rate);
            BigDecimal closingValue  = balanceQty.multiply(rate);

            // pick up item/location names from first entry if opening had none
            if (row[9] != null) {
                itemDesc   = (String) row[9];
            }
            if (row[10] != null) {
                godownName = (String) row[10];
            }

            entries.add(GodownVoucherEntryDto.builder()
                    .ledgerId(toLong(row[0]))
                    .txnDate(toLocalDate(row[1]))
                    .vchType((String) row[2])
                    .vchNo((String) row[3])
                    .particulars((String) row[4])
                    .inwardsQty(qtyIn)
                    .inwardsRate(qtyIn.compareTo(BigDecimal.ZERO) > 0 ? rate : BigDecimal.ZERO)
                    .inwardsValue(inwardsValue)
                    .outwardsQty(qtyOut)
                    .outwardsRate(qtyOut.compareTo(BigDecimal.ZERO) > 0 ? rate : BigDecimal.ZERO)
                    .outwardsValue(outwardsValue)
                    .closingQty(balanceQty)
                    .closingRate(rate)
                    .closingValue(closingValue)
                    .build());

            totalInwardsQty   = totalInwardsQty.add(qtyIn);
            totalInwardsValue = totalInwardsValue.add(inwardsValue);
            totalOutwardsQty  = totalOutwardsQty.add(qtyOut);
            totalOutwardsValue = totalOutwardsValue.add(outwardsValue);
        }

        BigDecimal closingQty   = openingQty.add(totalInwardsQty).subtract(totalOutwardsQty);
        BigDecimal closingValue = openingValue.add(totalInwardsValue).subtract(totalOutwardsValue);

        BigDecimal totalInwardsRate  = totalInwardsQty.compareTo(BigDecimal.ZERO) > 0
                ? totalInwardsValue.divide(totalInwardsQty, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal totalOutwardsRate = totalOutwardsQty.compareTo(BigDecimal.ZERO) > 0
                ? totalOutwardsValue.divide(totalOutwardsQty, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal closingRate = closingQty.compareTo(BigDecimal.ZERO) > 0
                ? closingValue.divide(closingQty, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        return GodownVoucherResponse.builder()
                .itemId(itemId)
                .itemDesc(itemDesc)
                .godownId(locationId)
                .godownName(godownName)
                .fromDate(fromDate)
                .toDate(toDate)
                .openingQty(openingQty)
                .openingRate(openingRate)
                .openingValue(openingValue)
                .entries(entries)
                .totalInwardsQty(totalInwardsQty)
                .totalInwardsRate(totalInwardsRate)
                .totalInwardsValue(totalInwardsValue)
                .totalOutwardsQty(totalOutwardsQty)
                .totalOutwardsRate(totalOutwardsRate)
                .totalOutwardsValue(totalOutwardsValue)
                .closingQty(closingQty)
                .closingRate(closingRate)
                .closingValue(closingValue)
                .build();
    }

    // ========================================================================
    // SUPPLIER GODOWN SUMMARY
    // ========================================================================
    public SupplierGodownSummaryResponse getSupplierGodownSummary(String suppId, Long godownId,
                                                                   String itemId,
                                                                   LocalDate fromDate,
                                                                   LocalDate toDate) {
        log.info("Generating Supplier Godown Summary - suppId: {}, godownId: {}, itemId: {}, dates: {} to {}",
                suppId, godownId, itemId, fromDate, toDate);

        var supplier = supplierMasterRepository.findById(suppId)
                .orElseThrow(() -> new MmsException("Supplier not found: " + suppId));

        if (fromDate == null) {
            fromDate = LocalDate.now(IST).withDayOfMonth(1);
        }
        if (toDate == null) {
            toDate = LocalDate.now(IST);
        }

        Map<String, String> groupDescMap = new LinkedHashMap<>();
        Map<String, List<GodownSummaryDto>> groupItemsMap = new LinkedHashMap<>();

        List<Object[]> movementRows = reportRepository.findSupplierGodownSummary(suppId, godownId, itemId, fromDate, toDate);
        Set<String> seenItemIds = new HashSet<>();
        for (Object[] row : movementRows) {
            String gId   = row[2] != null ? (String) row[2] : "UNGROUPED";
            String gDesc = row[3] != null ? (String) row[3] : "Ungrouped";

            BigDecimal openingQty   = toBigDecimal(row[5]);
            BigDecimal inwardQty    = toBigDecimal(row[6]);
            BigDecimal inwardValue  = toBigDecimal(row[7]);
            BigDecimal outwardQty   = toBigDecimal(row[8]);
            BigDecimal outwardValue = toBigDecimal(row[9]);

            BigDecimal inwardRate  = inwardQty.compareTo(BigDecimal.ZERO) > 0
                    ? inwardValue.divide(inwardQty, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            BigDecimal outwardRate = outwardQty.compareTo(BigDecimal.ZERO) > 0
                    ? outwardValue.divide(outwardQty, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            BigDecimal closingQty   = openingQty.add(inwardQty).subtract(outwardQty);
            BigDecimal closingValue = inwardValue.subtract(outwardValue); // no opening value (cross-branch avg unknown)
            BigDecimal closingRate  = closingQty.compareTo(BigDecimal.ZERO) > 0
                    ? closingValue.divide(closingQty, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;

            GodownSummaryDto dto = GodownSummaryDto.builder()
                    .itemId((String) row[0])
                    .itemDesc((String) row[1])
                    .openingQty(openingQty)
                    .openingRate(BigDecimal.ZERO)
                    .openingValue(BigDecimal.ZERO)
                    .inwardQty(inwardQty)
                    .inwardRate(inwardRate)
                    .inwardValue(inwardValue)
                    .outwardQty(outwardQty)
                    .outwardRate(outwardRate)
                    .outwardValue(outwardValue)
                    .closingQty(closingQty)
                    .closingRate(closingRate)
                    .closingValue(closingValue)
                    .build();

            groupDescMap.put(gId, gDesc);
            groupItemsMap.computeIfAbsent(gId, k -> new ArrayList<>()).add(dto);
            seenItemIds.add(dto.getItemId());
        }

        List<Object[]> openingOnlyRows = reportRepository.findSupplierGodownOpeningOnly(suppId, godownId, itemId, fromDate, toDate);
        for (Object[] row : openingOnlyRows) {
            String rowItemId = (String) row[0];
            if (seenItemIds.contains(rowItemId)) {
                continue;
            }
            String gId   = row[2] != null ? (String) row[2] : "UNGROUPED";
            String gDesc = row[3] != null ? (String) row[3] : "Ungrouped";
            BigDecimal openingQty  = toBigDecimal(row[4]);
            BigDecimal openingRate = toBigDecimal(row[5]);
            groupDescMap.put(gId, gDesc);
            groupItemsMap.computeIfAbsent(gId, k -> new ArrayList<>()).add(
                    GodownSummaryDto.builder()
                            .itemId(rowItemId)
                            .itemDesc((String) row[1])
                            .openingQty(openingQty)
                            .openingRate(openingRate)
                            .openingValue(openingQty.multiply(openingRate))
                            .inwardQty(BigDecimal.ZERO)
                            .inwardRate(BigDecimal.ZERO)
                            .inwardValue(BigDecimal.ZERO)
                            .outwardQty(BigDecimal.ZERO)
                            .outwardRate(BigDecimal.ZERO)
                            .outwardValue(BigDecimal.ZERO)
                            .closingQty(openingQty)
                            .closingRate(openingRate)
                            .closingValue(openingQty.multiply(openingRate))
                            .build());
        }

        List<GodownSummaryResponse.GroupRow> groups = new ArrayList<>();
        List<GodownSummaryDto> allItems = new ArrayList<>();
        for (Map.Entry<String, List<GodownSummaryDto>> entry : groupItemsMap.entrySet()) {
            String gId = entry.getKey();
            List<GodownSummaryDto> gItems = entry.getValue();
            gItems.sort((a, b) -> a.getItemDesc().compareTo(b.getItemDesc()));
            allItems.addAll(gItems);
            groups.add(GodownSummaryResponse.GroupRow.builder()
                    .groupId(gId)
                    .groupDesc(groupDescMap.getOrDefault(gId, gId))
                    .items(gItems)
                    .subTotal(buildGodownTotal(gId, groupDescMap.getOrDefault(gId, gId), gItems))
                    .build());
        }

        return SupplierGodownSummaryResponse.builder()
                .suppId(suppId)
                .suppName(supplier.getSuppName())
                .godownId(godownId)
                .fromDate(fromDate)
                .toDate(toDate)
                .groups(groups)
                .grandTotal(buildGodownTotal("GRAND_TOTAL", "Grand Total", allItems))
                .build();
    }

    // ========================================================================
    // SUPPLIER GODOWN VOUCHER
    // ========================================================================
    public SupplierGodownVoucherResponse getSupplierGodownVoucher(String suppId, Long godownId,
                                                                   String itemId,
                                                                   LocalDate fromDate, LocalDate toDate) {
        log.info("Generating Supplier Godown Voucher - suppId: {}, godownId: {}, itemId: {}, dates: {} to {}",
                suppId, godownId, itemId, fromDate, toDate);

        var supplier = supplierMasterRepository.findById(suppId)
                .orElseThrow(() -> new MmsException("Supplier not found: " + suppId));

        if (fromDate == null) {
            fromDate = LocalDate.now(IST).withDayOfMonth(1);
        }
        if (toDate == null) {
            toDate = LocalDate.now(IST);
        }

        // Opening balance
        List<Object[]> openingRows = reportRepository.findSupplierGodownVoucherOpening(suppId, godownId, itemId, fromDate);
        BigDecimal openingQty   = BigDecimal.ZERO;
        BigDecimal openingRate  = BigDecimal.ZERO;
        if (!openingRows.isEmpty()) {
            Object[] ob = openingRows.get(0);
            openingQty  = toBigDecimal(ob[0]);
            openingRate = toBigDecimal(ob[1]);
        }
        BigDecimal openingValue = openingQty.multiply(openingRate);

        // Transaction entries
        List<Object[]> rows = reportRepository.findSupplierGodownVoucherEntries(suppId, godownId, itemId, fromDate, toDate);

        BigDecimal totalInwardsQty    = BigDecimal.ZERO;
        BigDecimal totalInwardsValue  = BigDecimal.ZERO;
        BigDecimal totalOutwardsQty   = BigDecimal.ZERO;
        BigDecimal totalOutwardsValue = BigDecimal.ZERO;
        BigDecimal runningQty = openingQty;

        String resolvedItemDesc = itemId;
        List<GodownVoucherEntryDto> entries = new ArrayList<>();
        for (Object[] row : rows) {
            BigDecimal inwardQty  = toBigDecimal(row[5]);
            BigDecimal outwardQty = toBigDecimal(row[6]);
            BigDecimal rate       = toBigDecimal(row[7]);

            runningQty = runningQty.add(inwardQty).subtract(outwardQty);

            BigDecimal inwardValue  = inwardQty.multiply(rate);
            BigDecimal outwardValue = outwardQty.multiply(rate);
            BigDecimal closingValue = runningQty.multiply(rate);

            entries.add(GodownVoucherEntryDto.builder()
                    .ledgerId(toLong(row[0]))
                    .txnDate(toLocalDate(row[1]))
                    .vchType((String) row[2])
                    .vchNo((String) row[3])
                    .particulars((String) row[4])
                    .inwardsQty(inwardQty)
                    .inwardsRate(inwardQty.compareTo(BigDecimal.ZERO) > 0 ? rate : BigDecimal.ZERO)
                    .inwardsValue(inwardValue)
                    .outwardsQty(outwardQty)
                    .outwardsRate(outwardQty.compareTo(BigDecimal.ZERO) > 0 ? rate : BigDecimal.ZERO)
                    .outwardsValue(outwardValue)
                    .closingQty(runningQty)
                    .closingRate(rate)
                    .closingValue(closingValue)
                    .build());

            totalInwardsQty    = totalInwardsQty.add(inwardQty);
            totalInwardsValue  = totalInwardsValue.add(inwardValue);
            totalOutwardsQty   = totalOutwardsQty.add(outwardQty);
            totalOutwardsValue = totalOutwardsValue.add(outwardValue);
        }

        BigDecimal closingQty   = openingQty.add(totalInwardsQty).subtract(totalOutwardsQty);
        BigDecimal closingValue = openingValue.add(totalInwardsValue).subtract(totalOutwardsValue);
        BigDecimal closingRate  = closingQty.compareTo(BigDecimal.ZERO) > 0
                ? closingValue.divide(closingQty, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        return SupplierGodownVoucherResponse.builder()
                .suppId(suppId)
                .suppName(supplier.getSuppName())
                .itemId(itemId)
                .itemDesc(resolvedItemDesc)
                .fromDate(fromDate)
                .toDate(toDate)
                .openingQty(openingQty)
                .openingRate(openingRate)
                .openingValue(openingValue)
                .entries(entries)
                .totalInwardsQty(totalInwardsQty)
                .totalInwardsValue(totalInwardsValue)
                .totalOutwardsQty(totalOutwardsQty)
                .totalOutwardsValue(totalOutwardsValue)
                .closingQty(closingQty)
                .closingRate(closingRate)
                .closingValue(closingValue)
                .build();
    }
}
