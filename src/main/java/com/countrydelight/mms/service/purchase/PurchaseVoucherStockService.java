package com.countrydelight.mms.service.purchase;

import com.countrydelight.mms.entity.purchase.PurchaseVoucherDetail;
import com.countrydelight.mms.entity.purchase.PurchaseVoucherDetailTo;
import com.countrydelight.mms.entity.purchase.PurchaseVoucherHeader;
import com.countrydelight.mms.entity.stock.BranchMaterialStock;
import com.countrydelight.mms.repository.stock.BranchMaterialStockRepository;
import com.countrydelight.mms.service.master.GodownItemStockService;
import com.countrydelight.mms.service.stock.StockLedgerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Applies category-driven stock movements for Purchase Vouchers.
 *
 * Each voucher category maps to a specific stock action (IN / OUT / none).
 * Call applyStock(pv, false) on create, and applyStock(oldPv, true) before update.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseVoucherStockService {

    private final StockLedgerService stockLedgerService;
    private final GodownItemStockService godownItemStockService;
    private final BranchMaterialStockRepository branchStockRepository;

    /**
     * Apply (or reverse) stock movements based on the voucher's category.
     *
     * @param pv         the purchase voucher header (with details loaded)
     * @param isReversal true → reverse a previous posting; false → normal posting
     */
    @Transactional
    public void applyStock(PurchaseVoucherHeader pv, boolean isReversal) {
        applyDetailsToStock(pv, isReversal);

        String category = pv.getVoucherCategory();
        if (category == null || category.isBlank()) {
            log.debug("PV {} has no voucher category, skipping stock movement", pv.getPvId());
            return;
        }

        switch (category) {
            case "Purchase Order"     -> { /* no stock movement */ }
            case "Receipt Note"       -> applyFixed(pv, isReversal, StockLedgerService.TXN_PV_RECEIPT_NOTE,   true);
            case "Delivery Note"      -> applyFixed(pv, isReversal, StockLedgerService.TXN_PV_DELIVERY_NOTE,  false);
            case "Material In"        -> applyFixed(pv, isReversal, StockLedgerService.TXN_PV_MATERIAL_IN,    true);
            case "Material Out"       -> applyFixed(pv, isReversal, StockLedgerService.TXN_PV_MATERIAL_OUT,   false);
            case "Rejections In"      -> applyFixed(pv, isReversal, StockLedgerService.TXN_PV_REJECTION_IN,   true);
            case "Rejections Out"     -> applyFixed(pv, isReversal, StockLedgerService.TXN_PV_REJECTION_OUT,  false);
            case "Job Work Out Order" -> applyJobWork(pv, isReversal, false);
            case "Job Work In Order"  -> applyJobWork(pv, isReversal, true);
            case "Physical Stock"     -> applyVariance(pv, isReversal, StockLedgerService.TXN_PV_PHYSICAL_STOCK);
            case "Stock Journal"      -> applyVariance(pv, isReversal, StockLedgerService.TXN_PV_STOCK_JOURNAL);
            default -> log.warn("Unknown voucher category '{}' for PV {}, skipping stock movement",
                    category, pv.getPvId());
        }
    }

    /**
     * Fixed-direction categories: always IN or always OUT per detail line.
     *
     * @param normallyIn true if the category is normally a stock inward
     */
    private void applyFixed(PurchaseVoucherHeader pv, boolean isReversal,
                             String baseTxnType, boolean normallyIn) {
        boolean doIn = isReversal != normallyIn; // XOR: reversal swaps direction
        String txnType = isReversal ? baseTxnType + "_REVERSAL" : baseTxnType;
        LocalDate txnDate = effectiveDate(pv);

        for (PurchaseVoucherDetail detail : pv.getDetails()) {
            if (doIn) {
                stockLedgerService.recordStockIn(
                        pv.getBranchId(), detail.getItemId(), detail.getLocationId(),
                        txnDate, txnType, pv.getPvId(), detail.getQty(), detail.getRate());
            } else {
                BigDecimal rate = getAvgCost(pv.getBranchId(), detail.getItemId(), detail.getLocationId());
                stockLedgerService.recordStockOut(
                        pv.getBranchId(), detail.getItemId(), detail.getLocationId(),
                        txnDate, txnType, pv.getPvId(), detail.getQty(), rate);
            }
        }
    }

    /**
     * Job Work: location stock moves opposite to godown stock.
     *
     * Job Work In  Order → location IN  + source godown OUT
     * Job Work Out Order → location OUT + destination godown IN
     *
     * @param isJobWorkIn true for "Job Work In Order", false for "Job Work Out Order"
     */
    private void applyJobWork(PurchaseVoucherHeader pv, boolean isReversal, boolean isJobWorkIn) {
        // Normal:   locationIn = isJobWorkIn,  godownIn = !isJobWorkIn
        // Reversal: locationIn = !isJobWorkIn, godownIn =  isJobWorkIn
        boolean locationIn = isReversal != isJobWorkIn; // XOR
        boolean godownIn   = isReversal == isJobWorkIn; // XNOR

        String baseTxnType = isJobWorkIn ? StockLedgerService.TXN_PV_JW_IN : StockLedgerService.TXN_PV_JW_OUT;
        String txnType = isReversal ? baseTxnType + "_REVERSAL" : baseTxnType;
        LocalDate txnDate = effectiveDate(pv);

        Long godownId = isJobWorkIn ? pv.getSourceGodownId() : pv.getDestinationGodownId();

        for (PurchaseVoucherDetail detail : pv.getDetails()) {
            if (locationIn) {
                stockLedgerService.recordStockIn(
                        pv.getBranchId(), detail.getItemId(), detail.getLocationId(),
                        txnDate, txnType, pv.getPvId(), detail.getQty(), detail.getRate());
            } else {
                BigDecimal rate = getAvgCost(pv.getBranchId(), detail.getItemId(), detail.getLocationId());
                stockLedgerService.recordStockOut(
                        pv.getBranchId(), detail.getItemId(), detail.getLocationId(),
                        txnDate, txnType, pv.getPvId(), detail.getQty(), rate);
            }

            if (godownId != null) {
                if (godownIn) {
                    godownItemStockService.addStock(godownId, detail.getItemId(), detail.getQty());
                } else {
                    godownItemStockService.removeStock(godownId, detail.getItemId(), detail.getQty());
                }
            }
        }
    }

    /**
     * Variance categories (Physical Stock, Stock Journal):
     * compare detail.qty against current balance to determine IN or OUT.
     *
     * For reversal: swap IN↔OUT so the previous movement is undone.
     */
    private void applyVariance(PurchaseVoucherHeader pv, boolean isReversal, String baseTxnType) {
        String txnType = isReversal ? baseTxnType + "_REVERSAL" : baseTxnType;
        LocalDate txnDate = effectiveDate(pv);

        for (PurchaseVoucherDetail detail : pv.getDetails()) {
            BigDecimal currentBalance = stockLedgerService.getCurrentBalance(
                    pv.getBranchId(), detail.getItemId(), detail.getLocationId());
            BigDecimal diff = detail.getQty().subtract(currentBalance);

            if (diff.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            // Normal: positive diff → IN (surplus), negative diff → OUT (deficit)
            // Reversal: swap direction
            boolean doIn = isReversal ? diff.compareTo(BigDecimal.ZERO) < 0
                                      : diff.compareTo(BigDecimal.ZERO) > 0;

            if (doIn) {
                stockLedgerService.recordStockIn(
                        pv.getBranchId(), detail.getItemId(), detail.getLocationId(),
                        txnDate, txnType, pv.getPvId(), diff.abs(), detail.getRate());
            } else {
                BigDecimal rate = getAvgCost(pv.getBranchId(), detail.getItemId(), detail.getLocationId());
                stockLedgerService.recordStockOut(
                        pv.getBranchId(), detail.getItemId(), detail.getLocationId(),
                        txnDate, txnType, pv.getPvId(), diff.abs(), rate);
            }
        }
    }

    /**
     * Apply (or reverse) stock movements for detailsTo — the destination lines.
     * For each line in details: record OUT from source location.
     * For each line in detailsTo: record IN at destination location.
     */
    private void applyDetailsToStock(PurchaseVoucherHeader pv, boolean isReversal) {
        if (pv.getDetailsTo() == null || pv.getDetailsTo().isEmpty()) {
            return;
        }

        String outTxnType = isReversal
                ? StockLedgerService.TXN_PV_DETAILS_TRANSFER_OUT + "_REVERSAL"
                : StockLedgerService.TXN_PV_DETAILS_TRANSFER_OUT;
        String inTxnType = isReversal
                ? StockLedgerService.TXN_PV_DETAILS_TRANSFER_IN + "_REVERSAL"
                : StockLedgerService.TXN_PV_DETAILS_TRANSFER_IN;
        LocalDate txnDate = effectiveDate(pv);

        for (PurchaseVoucherDetail detail : pv.getDetails()) {
            BigDecimal avgCost = getAvgCost(pv.getBranchId(), detail.getItemId(), detail.getLocationId());
            stockLedgerService.recordStockOut(
                    pv.getBranchId(), detail.getItemId(), detail.getLocationId(),
                    txnDate, outTxnType, pv.getPvId(), detail.getQty(), avgCost);
        }

        for (PurchaseVoucherDetailTo detail : pv.getDetailsTo()) {
            stockLedgerService.recordStockIn(
                    pv.getBranchId(), detail.getItemId(), detail.getLocationId(),
                    txnDate, inTxnType, pv.getPvId(), detail.getQty(), detail.getRate());
        }
    }

    private LocalDate effectiveDate(PurchaseVoucherHeader pv) {
        return pv.getEffectiveDate() != null ? pv.getEffectiveDate() : pv.getPvDate();
    }

    private BigDecimal getAvgCost(String branchId, String itemId, String locationId) {
        return branchStockRepository.findByBranchIdAndItemIdAndLocationId(branchId, itemId, locationId)
                .map(BranchMaterialStock::getAvgCost)
                .orElse(BigDecimal.ZERO);
    }
}
