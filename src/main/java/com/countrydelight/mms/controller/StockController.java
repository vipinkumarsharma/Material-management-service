package com.countrydelight.mms.controller;

import com.countrydelight.mms.dto.common.ApiResponse;
import com.countrydelight.mms.dto.stock.AgingReportResponse;
import com.countrydelight.mms.dto.stock.ItemLocationStockResponse;
import com.countrydelight.mms.dto.stock.ItemMovementAnalysisResponse;
import com.countrydelight.mms.dto.stock.ItemStockPositionResponse;
import com.countrydelight.mms.dto.stock.StockSummaryResponse;
import com.countrydelight.mms.entity.stock.BranchMaterialStock;
import com.countrydelight.mms.repository.stock.BranchMaterialStockRepository;
import com.countrydelight.mms.service.stock.AgingService;
import com.countrydelight.mms.service.stock.ItemStockPositionService;
import com.countrydelight.mms.service.stock.StockLedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockLedgerService stockLedgerService;
    private final AgingService agingService;
    private final BranchMaterialStockRepository branchStockRepository;
    private final ItemStockPositionService itemStockPositionService;

    /**
     * Get stock summary for a branch.
     */
    @GetMapping("/summary/{branchId}")
    public ResponseEntity<ApiResponse<List<StockSummaryResponse>>> getStockSummary(
            @PathVariable String branchId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<BranchMaterialStock> stocks = branchStockRepository.findAllWithStockByBranch(
                branchId, PageRequest.of(page - 1, size));

        List<StockSummaryResponse> summaries = stocks.getContent().stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(summaries, stocks.getNumber() + 1, stocks.getTotalElements()));
    }

    /**
     * Get current balance for an item at a location.
     */
    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<BigDecimal>> getCurrentBalance(
            @RequestParam String branchId,
            @RequestParam String itemId,
            @RequestParam String locationId) {
        BigDecimal balance = stockLedgerService.getCurrentBalance(branchId, itemId, locationId);
        return ResponseEntity.ok(ApiResponse.success(balance));
    }

    /**
     * Generate aging report for a branch.
     * Aging is derived from ledger txn_date, never stored.
     * Buckets: 0-30, 31-60, 61-90, 90+ days
     */
    @GetMapping("/aging/{branchId}")
    public ResponseEntity<ApiResponse<AgingReportResponse>> getAgingReport(
            @PathVariable String branchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        if (asOfDate == null) {
            asOfDate = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        }
        AgingReportResponse report = agingService.generateAgingReport(branchId, asOfDate);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/movement-analysis")
    public ResponseEntity<ApiResponse<ItemMovementAnalysisResponse>> getItemMovementAnalysis(
            @RequestParam String branchId,
            @RequestParam String itemId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate forDate) {
        return ResponseEntity.ok(ApiResponse.success(
                itemStockPositionService.getItemMovementAnalysis(branchId, itemId, forDate)));
    }

    @GetMapping("/item-locations")
    public ResponseEntity<ApiResponse<List<ItemLocationStockResponse>>> getItemLocationStock(
            @RequestParam String branchId,
            @RequestParam String itemId) {
        List<ItemLocationStockResponse> result = branchStockRepository
                .findByBranchIdAndItemId(branchId, itemId)
                .stream()
                .filter(s -> s.getQtyOnHand().compareTo(BigDecimal.ZERO) > 0)
                .map(s -> ItemLocationStockResponse.builder()
                        .locationId(s.getLocationId())
                        .locationName(s.getLocation() != null ? s.getLocation().getLocationName() : null)
                        .qty(s.getQtyOnHand())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/item-position")
    public ResponseEntity<ApiResponse<ItemStockPositionResponse>> getItemStockPosition(
            @RequestParam String branchId,
            @RequestParam(required = false) String itemId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String locationId) {
        return ResponseEntity.ok(ApiResponse.success(
                itemStockPositionService.getItemStockPosition(branchId, itemId, fromDate, toDate, locationId)));
    }

    private StockSummaryResponse toSummaryResponse(BranchMaterialStock stock) {
        return StockSummaryResponse.builder()
                .branchId(stock.getBranchId())
                .branchName(stock.getBranch() != null ? stock.getBranch().getBranchName() : null)
                .itemId(stock.getItemId())
                .itemDesc(stock.getItem() != null ? stock.getItem().getItemDesc() : null)
                .locationId(stock.getLocationId())
                .locationName(stock.getLocation() != null ? stock.getLocation().getLocationName() : null)
                .qtyOnHand(stock.getQtyOnHand())
                .avgCost(stock.getAvgCost())
                .totalValue(stock.getQtyOnHand().multiply(stock.getAvgCost()))
                .lastUpdated(stock.getLastUpdated())
                .build();
    }
}
