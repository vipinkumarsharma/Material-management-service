package com.countrydelight.mms.service.stock;

import com.countrydelight.mms.entity.stock.BranchMaterialStock;
import com.countrydelight.mms.entity.stock.MaterialStockLedger;
import com.countrydelight.mms.repository.stock.BranchMaterialStockRepository;
import com.countrydelight.mms.repository.stock.MaterialStockLedgerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Stock Ledger Service - Manages the single source of truth.
 *
 * IMPORTANT RULES:
 * 1. Ledger entries are INSERT-ONLY (no update, no delete)
 * 2. GRN is the ONLY way to increase stock
 * 3. All stock movements must go through this service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockLedgerService {

    private final MaterialStockLedgerRepository ledgerRepository;
    private final BranchMaterialStockRepository branchStockRepository;

    public static final String TXN_GRN = "GRN";
    public static final String TXN_ISSUE = "ISSUE";
    public static final String TXN_TRANSFER_IN = "TRANSFER_IN";
    public static final String TXN_TRANSFER_OUT = "TRANSFER_OUT";
    public static final String TXN_DEPT_TRANSFER_IN = "DEPT_TRANSFER_IN";
    public static final String TXN_DEPT_TRANSFER_OUT = "DEPT_TRANSFER_OUT";
    public static final String TXN_OPENING_BALANCE = "OPENING_BALANCE";

    // Correction types (reversal negates original, correction has new values)
    public static final String TXN_GRN_REVERSAL = "GRN_REVERSAL";
    public static final String TXN_GRN_CORRECTION = "GRN_CORRECTION";
    public static final String TXN_ISSUE_REVERSAL = "ISSUE_REVERSAL";
    public static final String TXN_ISSUE_CORRECTION = "ISSUE_CORRECTION";
    public static final String TXN_TRANSFER_IN_REVERSAL = "TRANSFER_IN_REVERSAL";
    public static final String TXN_TRANSFER_IN_CORRECTION = "TRANSFER_IN_CORRECTION";
    public static final String TXN_TRANSFER_OUT_REVERSAL = "TRANSFER_OUT_REVERSAL";
    public static final String TXN_TRANSFER_OUT_CORRECTION = "TRANSFER_OUT_CORRECTION";
    public static final String TXN_OB_REVERSAL = "OB_REVERSAL";
    public static final String TXN_OB_CORRECTION = "OB_CORRECTION";

    public static final int CORRECTION_WINDOW_DAYS = 5;

    // Purchase Voucher transaction types
    public static final String TXN_PV_RECEIPT_NOTE   = "PV_RECEIPT_NOTE";
    public static final String TXN_PV_DELIVERY_NOTE  = "PV_DELIVERY_NOTE";
    public static final String TXN_PV_MATERIAL_IN    = "PV_MATERIAL_IN";
    public static final String TXN_PV_MATERIAL_OUT   = "PV_MATERIAL_OUT";
    public static final String TXN_PV_REJECTION_IN   = "PV_REJECTION_IN";
    public static final String TXN_PV_REJECTION_OUT  = "PV_REJECTION_OUT";
    public static final String TXN_PV_JW_IN          = "PV_JW_IN";
    public static final String TXN_PV_JW_OUT         = "PV_JW_OUT";
    public static final String TXN_PV_PHYSICAL_STOCK = "PV_PHYSICAL_STOCK";
    public static final String TXN_PV_STOCK_JOURNAL  = "PV_STOCK_JOURNAL";
    public static final String TXN_PV_DETAILS_TRANSFER_OUT = "PV_DETAILS_TRANSFER_OUT";
    public static final String TXN_PV_DETAILS_TRANSFER_IN  = "PV_DETAILS_TRANSFER_IN";

    /**
     * Record stock inward (GRN or Transfer In).
     * This is the ONLY way to increase stock.
     */
    @SuppressWarnings("checkstyle:ParameterNumber")
    @Transactional
    public MaterialStockLedger recordStockIn(String branchId, String itemId, String locationId,
                                              LocalDate txnDate, String txnType, Long refId,
                                              BigDecimal qtyIn, BigDecimal rate) {
        return recordStockIn(branchId, itemId, locationId, txnDate, txnType, refId, qtyIn, rate, null);
    }

    /**
     * Record stock inward with optional department tagging.
     */
    @SuppressWarnings("checkstyle:ParameterNumber")
    @Transactional
    public MaterialStockLedger recordStockIn(String branchId, String itemId, String locationId,
                                              LocalDate txnDate, String txnType, Long refId,
                                              BigDecimal qtyIn, BigDecimal rate, Integer deptId) {
        // Get current balance
        BigDecimal currentBalance = getCurrentBalance(branchId, itemId, locationId);
        BigDecimal newBalance = currentBalance.add(qtyIn);

        // Create ledger entry (INSERT ONLY - no update/delete)
        MaterialStockLedger ledger = MaterialStockLedger.builder()
                .branchId(branchId)
                .itemId(itemId)
                .locationId(locationId)
                .deptId(deptId)
                .txnDate(txnDate)
                .txnType(txnType)
                .refId(refId)
                .qtyIn(qtyIn)
                .qtyOut(BigDecimal.ZERO)
                .rate(rate)
                .balanceQty(newBalance)
                .build();

        ledger = ledgerRepository.save(ledger);
        log.info("Stock IN recorded: Branch={}, Item={}, Location={}, Dept={}, Qty={}, Rate={}, Balance={}",
                branchId, itemId, locationId, deptId, qtyIn, rate, newBalance);

        // Update summary table
        updateBranchStock(branchId, itemId, locationId, qtyIn, rate, true);

        return ledger;
    }

    /**
     * Record stock outward (Issue or Transfer Out).
     */
    @SuppressWarnings("checkstyle:ParameterNumber")
    @Transactional
    public MaterialStockLedger recordStockOut(String branchId, String itemId, String locationId,
                                               LocalDate txnDate, String txnType, Long refId,
                                               BigDecimal qtyOut, BigDecimal rate) {
        return recordStockOut(branchId, itemId, locationId, txnDate, txnType, refId, qtyOut, rate, null);
    }

    /**
     * Record stock outward with optional department tagging.
     */
    @SuppressWarnings("checkstyle:ParameterNumber")
    @Transactional
    public MaterialStockLedger recordStockOut(String branchId, String itemId, String locationId,
                                               LocalDate txnDate, String txnType, Long refId,
                                               BigDecimal qtyOut, BigDecimal rate, Integer deptId) {
        // Get current balance
        BigDecimal currentBalance = getCurrentBalance(branchId, itemId, locationId);
        BigDecimal newBalance = currentBalance.subtract(qtyOut);

        // Create ledger entry (INSERT ONLY)
        MaterialStockLedger ledger = MaterialStockLedger.builder()
                .branchId(branchId)
                .itemId(itemId)
                .locationId(locationId)
                .deptId(deptId)
                .txnDate(txnDate)
                .txnType(txnType)
                .refId(refId)
                .qtyIn(BigDecimal.ZERO)
                .qtyOut(qtyOut)
                .rate(rate)
                .balanceQty(newBalance)
                .build();

        ledger = ledgerRepository.save(ledger);
        log.info("Stock OUT recorded: Branch={}, Item={}, Location={}, Dept={}, Qty={}, Rate={}, Balance={}",
                branchId, itemId, locationId, deptId, qtyOut, rate, newBalance);

        // Update summary table
        updateBranchStock(branchId, itemId, locationId, qtyOut, rate, false);

        return ledger;
    }

    /**
     * Get current balance from the latest ledger entry.
     */
    public BigDecimal getCurrentBalance(String branchId, String itemId, String locationId) {
        MaterialStockLedger latest = ledgerRepository.findLatestLedgerEntry(branchId, itemId, locationId);
        return latest != null ? latest.getBalanceQty() : BigDecimal.ZERO;
    }

    /**
     * Get last GRN rate for an item at a branch (for price suggestion).
     */
    public BigDecimal getLastGrnRate(String branchId, String itemId) {
        return ledgerRepository.findLastGrnRateForItem(branchId, itemId);
    }

    /**
     * Update the branch material stock summary.
     */
    private void updateBranchStock(String branchId, String itemId, String locationId,
                                    BigDecimal qty, BigDecimal rate, boolean isInward) {
        BranchMaterialStock stock = branchStockRepository
                .findStockForUpdate(branchId, itemId, locationId)
                .orElse(BranchMaterialStock.builder()
                        .branchId(branchId)
                        .itemId(itemId)
                        .locationId(locationId)
                        .qtyOnHand(BigDecimal.ZERO)
                        .avgCost(BigDecimal.ZERO)
                        .build());

        BigDecimal oldQty = stock.getQtyOnHand();
        BigDecimal oldAvgCost = stock.getAvgCost();

        if (isInward) {
            // Calculate new weighted average cost
            BigDecimal oldValue = oldQty.multiply(oldAvgCost);
            BigDecimal newValue = qty.multiply(rate);
            BigDecimal totalQty = oldQty.add(qty);

            BigDecimal newAvgCost = totalQty.compareTo(BigDecimal.ZERO) > 0
                    ? oldValue.add(newValue).divide(totalQty, 4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            stock.setQtyOnHand(totalQty);
            stock.setAvgCost(newAvgCost);
        } else {
            // For outward, just reduce quantity (avg cost remains same)
            stock.setQtyOnHand(oldQty.subtract(qty));
        }

        branchStockRepository.save(stock);
    }
}
