package com.countrydelight.mms.service.stock;

import com.countrydelight.mms.dto.stock.AgingReportResponse;
import com.countrydelight.mms.dto.stock.AgingReportResponse.AgingBucket;
import com.countrydelight.mms.dto.stock.AgingReportResponse.ItemAgingDetail;
import com.countrydelight.mms.entity.inward.GrnDetail;
import com.countrydelight.mms.entity.master.BranchMaster;
import com.countrydelight.mms.entity.master.ItemMaster;
import com.countrydelight.mms.entity.master.LocationMaster;
import com.countrydelight.mms.repository.inward.GrnDetailRepository;
import com.countrydelight.mms.repository.master.BranchMasterRepository;
import com.countrydelight.mms.repository.master.ItemMasterRepository;
import com.countrydelight.mms.repository.master.LocationMasterRepository;
import com.countrydelight.mms.repository.stock.BranchMaterialStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aging Service - Calculates stock aging from ledger data.
 *
 * IMPORTANT: Aging is DERIVED from ledger txn_date, NEVER stored.
 * Aging buckets: 0-30, 31-60, 61-90, 90+ days
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgingService {

    private final GrnDetailRepository grnDetailRepository;
    private final BranchMaterialStockRepository branchStockRepository;
    private final BranchMasterRepository branchMasterRepository;
    private final ItemMasterRepository itemMasterRepository;
    private final LocationMasterRepository locationMasterRepository;

    /**
     * Generate aging report for a branch as of a specific date.
     * Aging is derived from GRN dates, never stored.
     */
    public AgingReportResponse generateAgingReport(String branchId, LocalDate asOfDate) {
        BranchMaster branch = branchMasterRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found: " + branchId));

        // Get all items with stock at this branch
        var stockList = branchStockRepository.findAllWithStockByBranch(branchId, Pageable.unpaged()).getContent();

        // Group by item and location
        Map<String, ItemAgingDetail> agingMap = new HashMap<>();

        for (var stock : stockList) {
            String key = stock.getItemId() + "|" + stock.getLocationId();

            // Get GRN batches with remaining stock for this item/location
            List<GrnDetail> batches = grnDetailRepository.findAvailableStockForFifo(
                    branchId, stock.getItemId(), stock.getLocationId());

            if (batches.isEmpty()) {
                continue;
            }

            // Calculate aging for each batch
            BigDecimal total0To30 = BigDecimal.ZERO;
            BigDecimal total31To60 = BigDecimal.ZERO;
            BigDecimal total61To90 = BigDecimal.ZERO;
            BigDecimal total90Plus = BigDecimal.ZERO;

            BigDecimal value0To30 = BigDecimal.ZERO;
            BigDecimal value31To60 = BigDecimal.ZERO;
            BigDecimal value61To90 = BigDecimal.ZERO;
            BigDecimal value90Plus = BigDecimal.ZERO;

            for (GrnDetail batch : batches) {
                LocalDate grnDate = batch.getGrnHeader().getGrnDate();
                long daysOld = ChronoUnit.DAYS.between(grnDate, asOfDate);
                BigDecimal qty = batch.getQtyRemaining();
                BigDecimal value = qty.multiply(batch.getRate());

                if (daysOld <= 30) {
                    total0To30 = total0To30.add(qty);
                    value0To30 = value0To30.add(value);
                } else if (daysOld <= 60) {
                    total31To60 = total31To60.add(qty);
                    value31To60 = value31To60.add(value);
                } else if (daysOld <= 90) {
                    total61To90 = total61To90.add(qty);
                    value61To90 = value61To90.add(value);
                } else {
                    total90Plus = total90Plus.add(qty);
                    value90Plus = value90Plus.add(value);
                }
            }

            BigDecimal totalQty = total0To30.add(total31To60).add(total61To90).add(total90Plus);
            BigDecimal totalValue = value0To30.add(value31To60).add(value61To90).add(value90Plus);

            ItemMaster item = itemMasterRepository.findById(stock.getItemId()).orElse(null);
            LocationMaster location = locationMasterRepository.findById(stock.getLocationId()).orElse(null);

            ItemAgingDetail detail = ItemAgingDetail.builder()
                    .itemId(stock.getItemId())
                    .itemDesc(item != null ? item.getItemDesc() : "Unknown")
                    .locationId(stock.getLocationId())
                    .locationName(location != null ? location.getLocationName() : "Unknown")
                    .totalQty(totalQty)
                    .totalValue(totalValue)
                    .bucket0To30(buildBucket(total0To30, value0To30, totalQty))
                    .bucket31To60(buildBucket(total31To60, value31To60, totalQty))
                    .bucket61To90(buildBucket(total61To90, value61To90, totalQty))
                    .bucket90Plus(buildBucket(total90Plus, value90Plus, totalQty))
                    .build();

            agingMap.put(key, detail);
        }

        return AgingReportResponse.builder()
                .branchId(branchId)
                .branchName(branch.getBranchName())
                .asOfDate(asOfDate)
                .items(new ArrayList<>(agingMap.values()))
                .build();
    }

    private AgingBucket buildBucket(BigDecimal qty, BigDecimal value, BigDecimal totalQty) {
        BigDecimal percentage = totalQty.compareTo(BigDecimal.ZERO) > 0
                ? qty.divide(totalQty, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return AgingBucket.builder()
                .qty(qty)
                .value(value)
                .percentage(percentage)
                .build();
    }
}
