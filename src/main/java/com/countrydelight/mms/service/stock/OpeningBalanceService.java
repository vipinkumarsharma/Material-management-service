package com.countrydelight.mms.service.stock;

import com.countrydelight.mms.dto.stock.LocationObRequest;
import com.countrydelight.mms.dto.stock.LocationObRequest.ItemObEntry;
import com.countrydelight.mms.dto.stock.LocationObResponse;
import com.countrydelight.mms.dto.stock.OpeningBalanceRequest;
import com.countrydelight.mms.dto.stock.OpeningBalanceRequest.OpeningBalanceEntry;
import com.countrydelight.mms.dto.stock.OpeningBalanceSummary;
import com.countrydelight.mms.entity.master.ItemMaster;
import com.countrydelight.mms.entity.stock.BranchMaterialStock;
import com.countrydelight.mms.entity.stock.MaterialStockLedger;
import com.countrydelight.mms.entity.stock.ObLocationHeader;
import com.countrydelight.mms.exception.MmsException;
import com.countrydelight.mms.repository.master.BranchMasterRepository;
import com.countrydelight.mms.repository.master.ItemMasterRepository;
import com.countrydelight.mms.repository.stock.BranchMaterialStockRepository;
import com.countrydelight.mms.repository.stock.MaterialStockLedgerRepository;
import com.countrydelight.mms.repository.stock.ObLocationHeaderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import java.io.InputStream;
import java.util.Locale;
import java.io.IOException;
import org.apache.poi.EncryptedDocumentException;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpeningBalanceService {

    private static final String DEFAULT_LOCATION = "DEFAULT";
    // private static final int OB_EDIT_WINDOW_DAYS = 5; // TODO: re-enable locking when ready
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private final MaterialStockLedgerRepository ledgerRepository;
    private final BranchMaterialStockRepository branchStockRepository;
    private final BranchMasterRepository branchMasterRepository;
    private final ItemMasterRepository itemMasterRepository;
    private final ObLocationHeaderRepository obLocationHeaderRepository;

    @Transactional
    public OpeningBalanceSummary uploadOpeningBalance(OpeningBalanceRequest request) {
        // Safety check: reject only entries whose specific (branch, item, location) already has non-OB transactions
        List<String> blocked = new ArrayList<>();
        for (OpeningBalanceEntry e : request.getEntries()) {
            String locId = (e.getLocationId() != null && !e.getLocationId().isBlank())
                    ? e.getLocationId() : DEFAULT_LOCATION;
            if (ledgerRepository.hasNonObEntriesForBranchItemLocation(
                    e.getBranchId(), e.getItemId(), locId, StockLedgerService.TXN_OPENING_BALANCE)) {
                blocked.add(e.getBranchId() + "/" + e.getItemId() + "/" + locId);
            }
        }
        if (!blocked.isEmpty()) {
            throw new MmsException("OPENING_BALANCE_BLOCKED",
                    "Transactions already exist for: " + String.join(", ", blocked)
                    + ". Cannot set opening balance after transactions have started.");
        }

        // Clear previous OB data only for the (branch, item, location) tuples present in this upload
        for (OpeningBalanceEntry e : request.getEntries()) {
            String locId = (e.getLocationId() != null && !e.getLocationId().isBlank())
                    ? e.getLocationId() : DEFAULT_LOCATION;
            ledgerRepository.deleteObEntriesForBranchLocationItem(
                    e.getBranchId(), locId, e.getItemId(), StockLedgerService.TXN_OPENING_BALANCE);
            branchStockRepository.deleteByBranchIdAndLocationIdAndItemId(e.getBranchId(), locId, e.getItemId());
        }
        log.info("Cleared previous opening balance data for {} entries in this upload", request.getEntries().size());

        // Validate all entries
        validateEntries(request.getEntries());

        // Build itemId -> costPrice map
        Set<String> itemIds = request.getEntries().stream()
                .map(OpeningBalanceEntry::getItemId).collect(Collectors.toSet());
        Map<String, BigDecimal> itemCostPriceMap = buildCostPriceMap(itemIds);

        // Aggregate entries by (branchId, itemId, locationId) to handle duplicates
        Map<String, AggregatedEntry> aggregated = aggregateEntries(request.getEntries(), itemCostPriceMap);

        // Insert ledger entries and upsert summary table
        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal totalValue = BigDecimal.ZERO;

        for (AggregatedEntry entry : aggregated.values()) {
            String locationId = entry.locationId != null && !entry.locationId.isBlank()
                    ? entry.locationId : DEFAULT_LOCATION;

            // Create ledger entry
            MaterialStockLedger ledger = MaterialStockLedger.builder()
                    .branchId(entry.branchId)
                    .itemId(entry.itemId)
                    .locationId(locationId)
                    .deptId(entry.deptId)
                    .txnDate(request.getCutoffDate())
                    .txnType(StockLedgerService.TXN_OPENING_BALANCE)
                    .refId(null)
                    .qtyIn(entry.qty)
                    .qtyOut(BigDecimal.ZERO)
                    .rate(entry.rate)
                    .balanceQty(entry.qty)
                    .build();
            ledgerRepository.save(ledger);

            // Create summary entry
            BranchMaterialStock stock = BranchMaterialStock.builder()
                    .branchId(entry.branchId)
                    .itemId(entry.itemId)
                    .locationId(locationId)
                    .qtyOnHand(entry.qty)
                    .avgCost(entry.rate)
                    .build();
            branchStockRepository.save(stock);

            totalQty = totalQty.add(entry.qty);
            totalValue = totalValue.add(entry.qty.multiply(entry.rate));
        }

        log.info("Opening balance uploaded: {} entries, totalQty={}, totalValue={}",
                aggregated.size(), totalQty, totalValue);

        return OpeningBalanceSummary.builder()
                .totalEntries(aggregated.size())
                .totalQty(totalQty)
                .totalValue(totalValue.setScale(4, RoundingMode.HALF_UP))
                .cutoffDate(request.getCutoffDate())
                .build();
    }

    @Transactional
    public OpeningBalanceSummary uploadOpeningBalanceCsv(MultipartFile file, LocalDate cutoffDate) {
        List<OpeningBalanceEntry> entries = parseCsv(file);
        OpeningBalanceRequest request = OpeningBalanceRequest.builder()
                .cutoffDate(cutoffDate)
                .entries(entries)
                .build();
        return uploadOpeningBalance(request);
    }

    @Transactional
    public OpeningBalanceSummary uploadLocationOpeningBalance(LocationObRequest request) {
        String branchId = request.getBranchId();
        String locationId = request.getLocationId();

        // TODO: re-enable 5-day edit window lock when ready
        // if (existing.isPresent()) {
        //     LocalDateTime createdAt = existing.get().getCreatedAt();
        //     if (createdAt != null
        //             && createdAt.isBefore(LocalDateTime.now(IST).minusDays(OB_EDIT_WINDOW_DAYS))) {
        //         throw new MmsException(
        //                 "Opening balance for location '" + locationId
        //                 + "' is locked (created more than " + OB_EDIT_WINDOW_DAYS + " days ago).");
        //     }
        // }
        Optional<ObLocationHeader> existing = obLocationHeaderRepository
                .findByBranchIdAndLocationId(branchId, locationId);

        // 2. Block only items that already have non-OB transactions at this branch+location
        List<String> blocked = new ArrayList<>();
        for (ItemObEntry item : request.getItems()) {
            if (ledgerRepository.hasNonObEntriesForBranchItemLocation(
                    branchId, item.getItemId(), locationId, StockLedgerService.TXN_OPENING_BALANCE)) {
                blocked.add(item.getItemId());
            }
        }
        if (!blocked.isEmpty()) {
            throw new MmsException(
                    "Transactions already exist for item(s): " + String.join(", ", blocked)
                    + " at location '" + locationId + "'. Cannot set opening balance after transactions have started.");
        }

        // 3. Validate items exist and build costPrice map
        Set<String> itemIds = request.getItems().stream()
                .map(ItemObEntry::getItemId).collect(Collectors.toSet());
        Map<String, BigDecimal> itemCostPriceMap = buildCostPriceMap(itemIds);

        // 4. Clear previous OB data for this location (if re-upload within window)
        if (existing.isPresent()) {
            // Only remove entries for the items present in this request. Previously we removed all
            // items for the location which caused other items' opening balances to be lost.
            for (ItemObEntry item : request.getItems()) {
                String itemId = item.getItemId();
                ledgerRepository.deleteObEntriesForBranchLocationItem(
                        branchId, locationId, itemId, StockLedgerService.TXN_OPENING_BALANCE);
                branchStockRepository.deleteByBranchIdAndLocationIdAndItemId(branchId, locationId, itemId);
            }
        }

        // 5. Save/update header first to get ob_id
        ObLocationHeader header = existing.orElse(
                ObLocationHeader.builder()
                        .branchId(branchId)
                        .locationId(locationId)
                        .build());
        header.setCutoffDate(request.getCutoffDate());
        header = obLocationHeaderRepository.save(header);
        Long obId = header.getObId();

        // 6. Create ledger entries and summary entries
        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal totalValue = BigDecimal.ZERO;

        for (ItemObEntry entry : request.getItems()) {
            BigDecimal qty = entry.getQty();
            BigDecimal rate = itemCostPriceMap.get(entry.getItemId());

            ledgerRepository.save(MaterialStockLedger.builder()
                    .branchId(branchId)
                    .itemId(entry.getItemId())
                    .locationId(locationId)
                    .txnDate(request.getCutoffDate())
                    .txnType(StockLedgerService.TXN_OPENING_BALANCE)
                    .refId(obId)
                    .qtyIn(qty)
                    .qtyOut(BigDecimal.ZERO)
                    .rate(rate)
                    .balanceQty(qty)
                    .build());

            branchStockRepository.save(BranchMaterialStock.builder()
                    .branchId(branchId)
                    .itemId(entry.getItemId())
                    .locationId(locationId)
                    .qtyOnHand(qty)
                    .avgCost(rate)
                    .build());

            totalQty = totalQty.add(qty);
            totalValue = totalValue.add(qty.multiply(rate));
        }

        log.info("Location OB uploaded: branch={}, location={}, items={}", branchId, locationId, request.getItems().size());

        return OpeningBalanceSummary.builder()
                .totalEntries(request.getItems().size())
                .totalQty(totalQty)
                .totalValue(totalValue.setScale(4, RoundingMode.HALF_UP))
                .cutoffDate(request.getCutoffDate())
                .build();
    }

    private void validateEntries(List<OpeningBalanceEntry> entries) {
        // Collect all unique branchIds and itemIds
        Set<String> branchIds = entries.stream()
                .map(OpeningBalanceEntry::getBranchId)
                .collect(Collectors.toSet());
        Set<String> itemIds = entries.stream()
                .map(OpeningBalanceEntry::getItemId)
                .collect(Collectors.toSet());

        // Validate branches exist
        Set<String> existingBranches = branchMasterRepository.findAllById(branchIds).stream()
                .map(b -> b.getBranchId())
                .collect(Collectors.toSet());
        Set<String> invalidBranches = branchIds.stream()
                .filter(id -> !existingBranches.contains(id))
                .collect(Collectors.toSet());
        if (!invalidBranches.isEmpty()) {
            throw new MmsException("INVALID_BRANCH",
                    "Invalid branch IDs: " + String.join(", ", invalidBranches));
        }

        // Validate items exist
        Set<String> existingItems = itemMasterRepository.findAllById(itemIds).stream()
                .map(i -> i.getItemId())
                .collect(Collectors.toSet());
        Set<String> invalidItems = itemIds.stream()
                .filter(id -> !existingItems.contains(id))
                .collect(Collectors.toSet());
        if (!invalidItems.isEmpty()) {
            throw new MmsException("INVALID_ITEM",
                    "Invalid item IDs: " + String.join(", ", invalidItems));
        }
    }

    private Map<String, AggregatedEntry> aggregateEntries(
            List<OpeningBalanceEntry> entries, Map<String, BigDecimal> costPriceMap) {
        Map<String, AggregatedEntry> map = new HashMap<>();

        for (OpeningBalanceEntry entry : entries) {
            String locationId = entry.getLocationId() != null && !entry.getLocationId().isBlank()
                    ? entry.getLocationId() : DEFAULT_LOCATION;
            String key = entry.getBranchId() + "|" + entry.getItemId() + "|" + locationId;
            BigDecimal rate = costPriceMap.get(entry.getItemId());

            map.merge(key, new AggregatedEntry(entry.getBranchId(), entry.getItemId(),
                    locationId, entry.getDeptId(), entry.getQty(), rate),
                    (existing, incoming) -> {
                         // Weighted average for rate when aggregating
                         BigDecimal existingValue = existing.qty.multiply(existing.rate);
                         BigDecimal incomingValue = incoming.qty.multiply(incoming.rate);
                         BigDecimal totalQty = existing.qty.add(incoming.qty);
                         BigDecimal avgRate = totalQty.compareTo(BigDecimal.ZERO) > 0
                                 ? existingValue.add(incomingValue)
                                     .divide(totalQty, 4, RoundingMode.HALF_UP)
                                 : BigDecimal.ZERO;
                         existing.qty = totalQty;
                         existing.rate = avgRate;
                         return existing;
                     });
        }

        return map;
    }

    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    private List<OpeningBalanceEntry> parseCsv(MultipartFile file) {
        // Read content into memory once so we can attempt Excel parsing even if filename is missing
        byte[] data;
        try {
            data = file.getBytes();
        } catch (IOException e) {
            throw new MmsException("CSV_PARSE_ERROR", "Failed to read uploaded file: " + e.getMessage());
        }

        // Try parsing as Excel first (handles .xls/.xlsx and files without extension)
        try {
            List<OpeningBalanceEntry> excelEntries = parseExcelBytes(data);
            // If parseExcelBytes succeeded and returned entries, use it
            if (excelEntries != null && !excelEntries.isEmpty()) {
                return excelEntries;
            }
        } catch (MmsException ignored) {
            // Not an Excel file or parsing failed — fall back to CSV below
        }

        List<OpeningBalanceEntry> entries = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new java.io.ByteArrayInputStream(data), StandardCharsets.UTF_8))) {

            String line;
            int lineNum = 0;

            while ((line = reader.readLine()) != null) {
                lineNum++;

                // Skip header row
                if (lineNum == 1) {
                    continue;
                }

                // Skip empty lines
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                String[] parts = line.split(",", -1);
                if (parts.length < 3) {
                    throw new MmsException("CSV_PARSE_ERROR",
                            "Line " + lineNum + ": Expected at least 3 columns "
                                    + "(branch_id, item_id, qty) or (branch_id, item_id, location_id, qty), found "
                                    + parts.length);
                }

                // Flexible mapping: support 3-col (branch,item,qty), 4-col (branch,item,location,qty),
                // or 5+ col (branch,item,location,dept,qty)
                String branchId = parts[0].trim();
                String itemId = parts[1].trim();
                String locationId = null;
                String deptIdStr = null;
                String qtyStr = null;

                if (parts.length == 3) {
                    qtyStr = parts[2].trim();
                } else if (parts.length == 4) {
                    locationId = parts[2].trim();
                    qtyStr = parts[3].trim();
                } else {
                    locationId = parts[2].trim();
                    deptIdStr = parts[3].trim();
                    qtyStr = parts[4].trim();
                }

                if (branchId.isEmpty() || itemId.isEmpty()) {
                    throw new MmsException("CSV_PARSE_ERROR",
                            "Line " + lineNum + ": branch_id and item_id are required");
                }

                if (qtyStr == null || qtyStr.isEmpty()) {
                    throw new MmsException("CSV_PARSE_ERROR",
                            "Line " + lineNum + ": qty is required");
                }

                try {
                    OpeningBalanceEntry entry = OpeningBalanceEntry.builder()
                            .branchId(branchId)
                            .itemId(itemId)
                            .locationId(locationId != null && !locationId.isEmpty() ? locationId : null)
                            .deptId(deptIdStr != null && !deptIdStr.isEmpty()
                                    ? Integer.parseInt(deptIdStr) : null)
                            .qty(new BigDecimal(qtyStr))
                            .build();
                    entries.add(entry);
                } catch (NumberFormatException e) {
                    throw new MmsException("CSV_PARSE_ERROR",
                            "Line " + lineNum + ": Invalid number format - " + e.getMessage());
                }
            }

        } catch (MmsException e) {
            throw e;
        } catch (java.io.IOException e) {
            throw new MmsException("CSV_PARSE_ERROR", "Failed to read CSV file: " + e.getMessage());
        }

        if (entries.isEmpty()) {
            throw new MmsException("CSV_PARSE_ERROR", "CSV file contains no data entries");
        }

        return entries;
    }

    /**
     * Attempt to parse Excel from raw bytes. Returns parsed entries or throws on fatal errors.
     */
    private List<OpeningBalanceEntry> parseExcelBytes(byte[] data) {
        List<OpeningBalanceEntry> entries = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (InputStream in = new java.io.ByteArrayInputStream(data); Workbook workbook = WorkbookFactory.create(in)) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                throw new MmsException("CSV_PARSE_ERROR", "Excel file contains no sheets");
            }

            int rowNum = 0;
            boolean headerProcessed = false;
            int idxBranch = 0, idxItem = 1, idxLocation = 2, idxDept = 3, idxQty = 4;

            for (Row row : sheet) {
                rowNum++;
                // Skip completely empty rows
                boolean emptyRow = true;
                for (int c = 0; c <= 4; c++) {
                    Cell cell = row.getCell(c);
                    if (cell != null && !formatter.formatCellValue(cell).isBlank()) {
                        emptyRow = false;
                        break;
                    }
                }
                if (emptyRow) {
                    continue;
                }

                if (!headerProcessed) {
                    // Check if this row looks like a header (contains non-numeric text in first few columns)
                    String first = getCellString(row, 0, formatter);
                    String second = getCellString(row, 1, formatter);
                    boolean maybeHeader = false;
                    if ((first != null && first.matches(".*[A-Za-z].*")) || (second != null && second.matches(".*[A-Za-z].*"))) {
                        maybeHeader = true;
                    }

                    if (maybeHeader) {
                        // map header names to indices
                        for (Cell cell : row) {
                            String h = formatter.formatCellValue(cell).trim().toLowerCase(Locale.ROOT);
                            int ci = cell.getColumnIndex();
                            if (h.contains("branch")) {
                                idxBranch = ci;
                            } else if (h.contains("item")) {
                                idxItem = ci;
                            } else if (h.contains("location")) {
                                idxLocation = ci;
                            } else if (h.contains("dept")) {
                                idxDept = ci;
                            } else if (h.contains("qty") || h.contains("quantity")) {
                                idxQty = ci;
                            }
                         }
                         headerProcessed = true;
                         continue; // move to next row (data starts after header)
                     } else {
                         // No header found; keep default positional mapping and treat this row as data
                         headerProcessed = true;
                         // fall through to process current row as data below
                     }
                 }

                // Read values using determined indices; be tolerant when dept column is missing
                String branchId = getCellString(row, idxBranch, formatter);
                String itemId = getCellString(row, idxItem, formatter);
                String locationId = getCellString(row, idxLocation, formatter);
                String deptIdStr = getCellString(row, idxDept, formatter);
                String qtyStr = getCellString(row, idxQty, formatter);

                // If qty is missing, attempt to infer layout based on non-empty cells in the row
                if (qtyStr == null || qtyStr.isBlank()) {
                    List<String> values = new ArrayList<>();
                    for (int c = 0; c <= row.getLastCellNum(); c++) {
                        String v = getCellString(row, c, formatter);
                        if (v != null && !v.isBlank()) {
                            values.add(v);
                        }
                    }
                    // values could be [branch,item,qty] or [branch,item,location,qty] or full [branch,item,location,dept,qty]
                    if (values.size() == 3) {
                        branchId = values.get(0);
                        itemId = values.get(1);
                        qtyStr = values.get(2);
                        locationId = null;
                        deptIdStr = null;
                    } else if (values.size() == 4) {
                        branchId = values.get(0);
                        itemId = values.get(1);
                        locationId = values.get(2);
                        qtyStr = values.get(3);
                        deptIdStr = null;
                    } else if (values.size() >= 5) {
                        branchId = values.get(0);
                        itemId = values.get(1);
                        locationId = values.get(2);
                        deptIdStr = values.get(3);
                        qtyStr = values.get(4);
                    }
                }

                if ((branchId == null || branchId.isBlank()) && (itemId == null || itemId.isBlank())
                        && (locationId == null || locationId.isBlank())
                        && (deptIdStr == null || deptIdStr.isBlank())
                        && (qtyStr == null || qtyStr.isBlank())) {
                    continue;
                }

                if (branchId == null || branchId.isBlank() || itemId == null || itemId.isBlank()) {
                    throw new MmsException("CSV_PARSE_ERROR",
                            "Line " + rowNum + ": branch_id and item_id are required");
                }

                if (qtyStr == null || qtyStr.isBlank()) {
                    throw new MmsException("CSV_PARSE_ERROR",
                            "Line " + rowNum + ": qty is required");
                }

                try {
                    OpeningBalanceEntry entry = OpeningBalanceEntry.builder()
                            .branchId(branchId.trim())
                            .itemId(itemId.trim())
                            .locationId(locationId != null && !locationId.isBlank() ? locationId.trim() : null)
                            .deptId(deptIdStr != null && !deptIdStr.isBlank() ? Integer.parseInt(deptIdStr.trim()) : null)
                            .qty(new BigDecimal(qtyStr.trim()))
                            .build();
                    entries.add(entry);
                } catch (NumberFormatException e) {
                    throw new MmsException("CSV_PARSE_ERROR",
                            "Line " + rowNum + ": Invalid number format - " + e.getMessage());
                }
             }

        } catch (MmsException e) {
            throw e;
        } catch (IOException | EncryptedDocumentException e) {
            throw new MmsException("CSV_PARSE_ERROR", "Failed to read Excel file: " + e.getMessage());
        }

        return entries;
    }

    private String getCellString(Row row, int index, DataFormatter formatter) {
        Cell cell = row.getCell(index);
        if (cell == null) {
            return null;
        }
        String v = formatter.formatCellValue(cell);
        return v == null ? null : v;
    }

    private Map<String, BigDecimal> buildCostPriceMap(Set<String> itemIds) {
        List<ItemMaster> items = itemMasterRepository.findAllById(itemIds);

        Set<String> existingIds = items.stream().map(ItemMaster::getItemId).collect(Collectors.toSet());
        Set<String> invalidIds = itemIds.stream()
                .filter(id -> !existingIds.contains(id)).collect(Collectors.toSet());
        if (!invalidIds.isEmpty()) {
            throw new MmsException("Invalid item IDs: " + String.join(", ", invalidIds));
        }

        Map<String, BigDecimal> map = new HashMap<>();
        for (ItemMaster item : items) {
            if (item.getCostPrice() == null || item.getCostPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new MmsException("Item '" + item.getItemId()
                        + "' has no cost price set. Please update the item master before uploading opening balance.");
            }
            map.put(item.getItemId(), item.getCostPrice());
        }
        return map;
    }

    public LocationObResponse getLocationOpeningBalance(String branchId, String locationId) {
        ObLocationHeader header = obLocationHeaderRepository
                .findByBranchIdAndLocationId(branchId, locationId)
                .orElseThrow(() -> new MmsException(
                        "No opening balance found for branch '" + branchId
                        + "' and location '" + locationId + "'"));

        // TODO: re-enable locking when ready
        // boolean locked = header.getCreatedAt() != null
        //         && header.getCreatedAt().isBefore(LocalDateTime.now(IST).minusDays(OB_EDIT_WINDOW_DAYS));
        boolean locked = false;

        List<MaterialStockLedger> entries = ledgerRepository
                .findByBranchIdAndLocationIdAndTxnType(
                        branchId, locationId, StockLedgerService.TXN_OPENING_BALANCE);

        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal totalValue = BigDecimal.ZERO;
        List<LocationObResponse.ItemObLine> items = new ArrayList<>();

        for (MaterialStockLedger e : entries) {
            BigDecimal lineValue = e.getQtyIn().multiply(e.getRate());
            items.add(LocationObResponse.ItemObLine.builder()
                    .itemId(e.getItemId())
                    .qty(e.getQtyIn())
                    .rate(e.getRate())
                    .totalValue(lineValue.setScale(4, RoundingMode.HALF_UP))
                    .build());
            totalQty = totalQty.add(e.getQtyIn());
            totalValue = totalValue.add(lineValue);
        }

        return LocationObResponse.builder()
                .obId(header.getObId())
                .branchId(branchId)
                .locationId(locationId)
                .cutoffDate(header.getCutoffDate())
                .createdAt(header.getCreatedAt())
                .locked(locked)
                .totalEntries(items.size())
                .totalQty(totalQty)
                .totalValue(totalValue.setScale(4, RoundingMode.HALF_UP))
                .items(items)
                .build();
    }

    private static class AggregatedEntry {
        String branchId;
        String itemId;
        String locationId;
        Integer deptId;
        BigDecimal qty;
        BigDecimal rate;

        AggregatedEntry(String branchId, String itemId, String locationId,
                        Integer deptId, BigDecimal qty, BigDecimal rate) {
            this.branchId = branchId;
            this.itemId = itemId;
            this.locationId = locationId;
            this.deptId = deptId;
            this.qty = qty;
            this.rate = rate;
        }
    }
}
