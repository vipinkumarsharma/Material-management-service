package com.countrydelight.mms.service.stock;

import com.countrydelight.mms.dto.stock.ItemMovementAnalysisResponse;
import com.countrydelight.mms.dto.stock.ItemMovementAnalysisResponse.MovementCategoryDto;
import com.countrydelight.mms.dto.stock.ItemMovementAnalysisResponse.MovementLineDto;
import com.countrydelight.mms.dto.stock.ItemMovementAnalysisResponse.MovementSectionDto;
import com.countrydelight.mms.dto.stock.ItemStockPositionResponse;
import com.countrydelight.mms.dto.stock.StockAmountDto;
import com.countrydelight.mms.dto.stock.StockVoucherLineDto;
import com.countrydelight.mms.entity.master.BranchMaster;
import com.countrydelight.mms.entity.master.ItemMaster;
import com.countrydelight.mms.exception.MmsException;
import com.countrydelight.mms.repository.master.BranchMasterRepository;
import com.countrydelight.mms.repository.master.ItemMasterRepository;
import com.countrydelight.mms.repository.report.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemStockPositionService {

    private final ReportRepository reportRepository;
    private final ItemMasterRepository itemMasterRepository;
    private final BranchMasterRepository branchMasterRepository;

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    public ItemStockPositionResponse getItemStockPosition(
            String branchId, String itemId,
            LocalDate fromDate, LocalDate toDate, String locationId) {

        if (itemId == null || itemId.isBlank()) {
            throw new MmsException("itemId is required");
        }
        if (fromDate == null) {
            fromDate = LocalDate.now(IST).withDayOfMonth(1);
        }
        if (toDate == null) {
            toDate = LocalDate.now(IST);
        }

        if (fromDate.isAfter(toDate)) {
            throw new MmsException("fromDate must be on or before toDate");
        }

        ItemMaster item = itemMasterRepository.findById(itemId)
                .orElseThrow(() -> new MmsException("Item not found: " + itemId));
        BranchMaster branch = branchMasterRepository.findById(branchId)
                .orElseThrow(() -> new MmsException("Branch not found: " + branchId));

        // 1. Opening balance
        List<Object[]> openingRows = reportRepository.findOpeningBalanceForItemPosition(
                branchId, itemId, locationId, fromDate);
        BigDecimal openingQty = BigDecimal.ZERO;
        BigDecimal openingRate = BigDecimal.ZERO;
        if (!openingRows.isEmpty() && openingRows.get(0) != null) {
            Object[] row = openingRows.get(0);
            openingQty = toBigDecimal(row[0]);
            openingRate = toBigDecimal(row[1]);
        }
        BigDecimal openingValue = openingQty.multiply(openingRate).setScale(2, RoundingMode.HALF_UP);
        StockAmountDto openingBalance = StockAmountDto.builder()
                .qty(openingQty)
                .rate(openingRate.setScale(2, RoundingMode.HALF_UP))
                .value(openingValue)
                .build();

        // 2. Transactions in range
        List<Object[]> txnRows = reportRepository.findItemStockPositionTransactions(
                branchId, itemId, locationId, fromDate, toDate);

        // 3. Build running balance and accumulate totals
        BigDecimal runningQty = openingQty;
        BigDecimal totalInQty = BigDecimal.ZERO;
        BigDecimal totalInValue = BigDecimal.ZERO;
        BigDecimal totalOutQty = BigDecimal.ZERO;
        BigDecimal totalOutValue = BigDecimal.ZERO;
        BigDecimal lastRate = openingRate;

        List<StockVoucherLineDto> transactions = new ArrayList<>();

        for (Object[] row : txnRows) {
            // [0]=ledger_id, [1]=txn_date, [2]=txn_type, [3]=qty_in, [4]=qty_out,
            // [5]=rate, [6]=particulars, [7]=vch_type_name, [8]=vch_no
            LocalDate txnDate = toLocalDate(row[1]);
            BigDecimal qtyIn = toBigDecimal(row[3]);
            BigDecimal qtyOut = toBigDecimal(row[4]);
            BigDecimal rate = toBigDecimal(row[5]);
            String particulars = row[6] != null ? row[6].toString() : null;
            String vchTypeName = row[7] != null ? row[7].toString() : null;
            String vchNo = row[8] != null ? row[8].toString() : null;

            runningQty = runningQty.add(qtyIn).subtract(qtyOut);
            lastRate = rate;

            BigDecimal closingValue = runningQty.multiply(rate).setScale(2, RoundingMode.HALF_UP);
            StockAmountDto closing = StockAmountDto.builder()
                    .qty(runningQty)
                    .rate(rate.setScale(2, RoundingMode.HALF_UP))
                    .value(closingValue)
                    .build();

            StockAmountDto inwardDto = null;
            StockAmountDto outwardDto = null;
            if (qtyIn.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal inValue = qtyIn.multiply(rate).setScale(2, RoundingMode.HALF_UP);
                inwardDto = StockAmountDto.builder()
                        .qty(qtyIn)
                        .rate(rate.setScale(2, RoundingMode.HALF_UP))
                        .value(inValue)
                        .build();
                totalInQty = totalInQty.add(qtyIn);
                totalInValue = totalInValue.add(inValue);
            }
            if (qtyOut.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal outValue = qtyOut.multiply(rate).setScale(2, RoundingMode.HALF_UP);
                outwardDto = StockAmountDto.builder()
                        .qty(qtyOut)
                        .rate(rate.setScale(2, RoundingMode.HALF_UP))
                        .value(outValue)
                        .build();
                totalOutQty = totalOutQty.add(qtyOut);
                totalOutValue = totalOutValue.add(outValue);
            }

            transactions.add(StockVoucherLineDto.builder()
                    .date(txnDate)
                    .particulars(particulars)
                    .vchType(vchTypeName)
                    .vchNo(vchNo)
                    .inward(inwardDto)
                    .outward(outwardDto)
                    .closing(closing)
                    .build());
        }

        // 4. Build totals
        BigDecimal closingQty = runningQty;
        BigDecimal closingValue = closingQty.multiply(lastRate).setScale(2, RoundingMode.HALF_UP);
        ItemStockPositionResponse.TotalsDto totals = ItemStockPositionResponse.TotalsDto.builder()
                .inward(StockAmountDto.builder()
                        .qty(totalInQty)
                        .rate(BigDecimal.ZERO)
                        .value(totalInValue)
                        .build())
                .outward(StockAmountDto.builder()
                        .qty(totalOutQty)
                        .rate(BigDecimal.ZERO)
                        .value(totalOutValue)
                        .build())
                .closing(StockAmountDto.builder()
                        .qty(closingQty)
                        .rate(lastRate.setScale(2, RoundingMode.HALF_UP))
                        .value(closingValue)
                        .build())
                .build();

        return ItemStockPositionResponse.builder()
                .itemId(item.getItemId())
                .itemDesc(item.getItemDesc())
                .branchId(branch.getBranchId())
                .branchName(branch.getBranchName())
                .fromDate(fromDate)
                .toDate(toDate)
                .openingBalance(openingBalance)
                .transactions(transactions)
                .totals(totals)
                .build();
    }

    public ItemMovementAnalysisResponse getItemMovementAnalysis(
            String branchId, String itemId, LocalDate forDate) {

        LocalDate fromDate = forDate.withDayOfMonth(1);
        LocalDate toDate = fromDate.with(TemporalAdjusters.lastDayOfMonth());

        ItemMaster item = itemMasterRepository.findById(itemId)
                .orElseThrow(() -> new MmsException("Item not found: " + itemId));
        BranchMaster branch = branchMasterRepository.findById(branchId)
                .orElseThrow(() -> new MmsException("Branch not found: " + branchId));

        List<Object[]> rows = reportRepository.findItemMovementAnalysis(branchId, itemId, fromDate, toDate);

        // [0]=txn_type [1]=particulars [2]=total_qty [3]=basic_rate [4]=effective_rate [5]=total_value
        // inward categories: LinkedHashMap preserves insertion order
        Map<String, List<MovementLineDto>> inwardCategories = new LinkedHashMap<>();
        Map<String, List<MovementLineDto>> outwardCategories = new LinkedHashMap<>();

        for (Object[] row : rows) {
            String txnType    = row[0] != null ? row[0].toString() : "";
            String particulars = row[1] != null ? row[1].toString() : txnType;
            BigDecimal qty    = toBigDecimal(row[2]).setScale(4, RoundingMode.HALF_UP);
            BigDecimal basicRate = toBigDecimal(row[3]).setScale(4, RoundingMode.HALF_UP);
            BigDecimal effectiveRate = toBigDecimal(row[4]).setScale(4, RoundingMode.HALF_UP);
            BigDecimal value  = toBigDecimal(row[5]).setScale(2, RoundingMode.HALF_UP);

            MovementLineDto line = MovementLineDto.builder()
                    .particulars(particulars)
                    .qty(qty)
                    .basicRate(basicRate)
                    .effectiveRate(effectiveRate)
                    .value(value)
                    .build();

            String categoryName = getCategoryName(txnType);
            if (INWARD_TYPES.contains(txnType) || txnType.isEmpty()) {
                inwardCategories.computeIfAbsent(categoryName, k -> new ArrayList<>()).add(line);
            } else {
                outwardCategories.computeIfAbsent(categoryName, k -> new ArrayList<>()).add(line);
            }
        }

        MovementSectionDto inward = buildSection(inwardCategories);
        MovementSectionDto outward = buildSection(outwardCategories);

        return ItemMovementAnalysisResponse.builder()
                .itemId(item.getItemId())
                .itemDesc(item.getItemDesc())
                .branchId(branch.getBranchId())
                .branchName(branch.getBranchName())
                .fromDate(fromDate)
                .toDate(toDate)
                .inward(inward)
                .outward(outward)
                .build();
    }

    private MovementSectionDto buildSection(Map<String, List<MovementLineDto>> categories) {
        List<MovementCategoryDto> categoryDtos = new ArrayList<>();
        BigDecimal sectionQty = BigDecimal.ZERO;
        BigDecimal sectionValue = BigDecimal.ZERO;

        for (Map.Entry<String, List<MovementLineDto>> entry : categories.entrySet()) {
            BigDecimal catQty = BigDecimal.ZERO;
            BigDecimal catValue = BigDecimal.ZERO;
            for (MovementLineDto line : entry.getValue()) {
                catQty = catQty.add(line.getQty());
                catValue = catValue.add(line.getValue());
            }
            categoryDtos.add(MovementCategoryDto.builder()
                    .categoryName(entry.getKey())
                    .lines(entry.getValue())
                    .totalQty(catQty.setScale(4, RoundingMode.HALF_UP))
                    .totalValue(catValue.setScale(2, RoundingMode.HALF_UP))
                    .build());
            sectionQty = sectionQty.add(catQty);
            sectionValue = sectionValue.add(catValue);
        }

        return MovementSectionDto.builder()
                .categories(categoryDtos)
                .totalQty(sectionQty.setScale(4, RoundingMode.HALF_UP))
                .totalValue(sectionValue.setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    private static final Set<String> INWARD_TYPES = Set.of(
            "GRN", "GRN_REVERSAL", "GRN_CORRECTION",
            "TRANSFER_IN", "TRANSFER_IN_REVERSAL", "TRANSFER_IN_CORRECTION",
            "DEPT_TRANSFER_IN", "DEPT_TRANSFER_IN_CORRECTION",
            "OPENING_BALANCE", "OB_REVERSAL", "OB_CORRECTION",
            "PV_RECEIPT_NOTE", "PV_MATERIAL_IN", "PV_REJECTION_IN",
            "PV_JW_IN", "PV_PHYSICAL_STOCK", "PV_STOCK_JOURNAL",
            "PV_DETAILS_TRANSFER_IN"
    );

    private static final Map<String, String> CATEGORY_MAP = Map.ofEntries(
            Map.entry("GRN",                      "Suppliers"),
            Map.entry("GRN_REVERSAL",              "Suppliers"),
            Map.entry("GRN_CORRECTION",            "Suppliers"),
            Map.entry("ISSUE",                     "Issued To"),
            Map.entry("ISSUE_REVERSAL",            "Issued To"),
            Map.entry("ISSUE_CORRECTION",          "Issued To"),
            Map.entry("TRANSFER_IN",               "Transfers Inward"),
            Map.entry("TRANSFER_IN_REVERSAL",      "Transfers Inward"),
            Map.entry("TRANSFER_IN_CORRECTION",    "Transfers Inward"),
            Map.entry("TRANSFER_OUT",              "Transfers Outward"),
            Map.entry("TRANSFER_OUT_REVERSAL",     "Transfers Outward"),
            Map.entry("TRANSFER_OUT_CORRECTION",   "Transfers Outward"),
            Map.entry("DEPT_TRANSFER_IN",          "Dept Transfers Inward"),
            Map.entry("DEPT_TRANSFER_IN_CORRECTION", "Dept Transfers Inward"),
            Map.entry("DEPT_TRANSFER_OUT",         "Dept Transfers Outward"),
            Map.entry("DEPT_TRANSFER_OUT_CORRECTION", "Dept Transfers Outward"),
            Map.entry("OPENING_BALANCE",           "Opening Balance"),
            Map.entry("OB_REVERSAL",               "Opening Balance"),
            Map.entry("OB_CORRECTION",             "Opening Balance"),
            Map.entry("PV_RECEIPT_NOTE",           "Receipt Note"),
            Map.entry("PV_DELIVERY_NOTE",          "Delivery Note"),
            Map.entry("PV_MATERIAL_IN",            "Material In"),
            Map.entry("PV_MATERIAL_OUT",           "Material Out"),
            Map.entry("PV_REJECTION_IN",           "Rejection In"),
            Map.entry("PV_REJECTION_OUT",          "Rejection Out"),
            Map.entry("PV_JW_IN",                  "Job Work In"),
            Map.entry("PV_JW_OUT",                 "Job Work Out"),
            Map.entry("PV_PHYSICAL_STOCK",         "Physical Stock"),
            Map.entry("PV_STOCK_JOURNAL",          "Stock Journal"),
            Map.entry("PV_DETAILS_TRANSFER_IN",    "Transfers Inward (PV)"),
            Map.entry("PV_DETAILS_TRANSFER_OUT",   "Transfers Outward (PV)")
    );

    private String getCategoryName(String txnType) {
        return CATEGORY_MAP.getOrDefault(txnType, txnType);
    }

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
}
