package com.countrydelight.mms.service.stock;

import com.countrydelight.mms.dto.stock.StockCorrectionRequest;
import com.countrydelight.mms.dto.stock.StockCorrectionResponse;
import com.countrydelight.mms.entity.stock.BranchMaterialStock;
import com.countrydelight.mms.entity.stock.MaterialStockLedger;
import com.countrydelight.mms.exception.MmsException;
import com.countrydelight.mms.repository.stock.BranchMaterialStockRepository;
import com.countrydelight.mms.repository.stock.MaterialStockLedgerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.countrydelight.mms.service.stock.StockLedgerService.CORRECTION_WINDOW_DAYS;
import static com.countrydelight.mms.service.stock.StockLedgerService.TXN_GRN;
import static com.countrydelight.mms.service.stock.StockLedgerService.TXN_GRN_CORRECTION;
import static com.countrydelight.mms.service.stock.StockLedgerService.TXN_GRN_REVERSAL;
import static com.countrydelight.mms.service.stock.StockLedgerService.TXN_ISSUE;
import static com.countrydelight.mms.service.stock.StockLedgerService.TXN_ISSUE_CORRECTION;
import static com.countrydelight.mms.service.stock.StockLedgerService.TXN_ISSUE_REVERSAL;
import static com.countrydelight.mms.service.stock.StockLedgerService.TXN_OB_CORRECTION;
import static com.countrydelight.mms.service.stock.StockLedgerService.TXN_OB_REVERSAL;
import static com.countrydelight.mms.service.stock.StockLedgerService.TXN_OPENING_BALANCE;
import static com.countrydelight.mms.service.stock.StockLedgerService.TXN_TRANSFER_IN;
import static com.countrydelight.mms.service.stock.StockLedgerService.TXN_TRANSFER_IN_CORRECTION;
import static com.countrydelight.mms.service.stock.StockLedgerService.TXN_TRANSFER_IN_REVERSAL;
import static com.countrydelight.mms.service.stock.StockLedgerService.TXN_TRANSFER_OUT;
import static com.countrydelight.mms.service.stock.StockLedgerService.TXN_TRANSFER_OUT_CORRECTION;
import static com.countrydelight.mms.service.stock.StockLedgerService.TXN_TRANSFER_OUT_REVERSAL;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockCorrectionService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private static final Map<String, String> REVERSAL_TYPE_MAP = Map.of(
            TXN_GRN, TXN_GRN_REVERSAL,
            TXN_ISSUE, TXN_ISSUE_REVERSAL,
            TXN_TRANSFER_IN, TXN_TRANSFER_IN_REVERSAL,
            TXN_TRANSFER_OUT, TXN_TRANSFER_OUT_REVERSAL,
            TXN_OPENING_BALANCE, TXN_OB_REVERSAL
    );

    private static final Map<String, String> CORRECTION_TYPE_MAP = Map.of(
            TXN_GRN, TXN_GRN_CORRECTION,
            TXN_ISSUE, TXN_ISSUE_CORRECTION,
            TXN_TRANSFER_IN, TXN_TRANSFER_IN_CORRECTION,
            TXN_TRANSFER_OUT, TXN_TRANSFER_OUT_CORRECTION,
            TXN_OPENING_BALANCE, TXN_OB_CORRECTION
    );

    private static final Set<String> INWARD_TYPES = Set.of(
            TXN_GRN, TXN_TRANSFER_IN, TXN_OPENING_BALANCE,
            TXN_GRN_CORRECTION, TXN_TRANSFER_IN_CORRECTION, TXN_OB_CORRECTION
    );

    private final MaterialStockLedgerRepository ledgerRepository;
    private final BranchMaterialStockRepository branchStockRepository;

    @Transactional
    public StockCorrectionResponse correctEntry(StockCorrectionRequest request) {
        MaterialStockLedger original = ledgerRepository.findById(request.getLedgerId())
                .orElseThrow(() -> new MmsException("LEDGER_NOT_FOUND",
                        "Ledger entry not found: " + request.getLedgerId()));

        validateCorrectionAllowed(original, request);

        String reversalType = REVERSAL_TYPE_MAP.get(original.getTxnType());
        String correctionType = CORRECTION_TYPE_MAP.get(original.getTxnType());

        boolean isInward = original.getQtyIn().compareTo(BigDecimal.ZERO) > 0;
        BigDecimal originalQty = isInward ? original.getQtyIn() : original.getQtyOut();
        BigDecimal correctedRate = request.getCorrectedRate() != null
                ? request.getCorrectedRate() : original.getRate();

        // Create reversal entry
        MaterialStockLedger reversal = MaterialStockLedger.builder()
                .branchId(original.getBranchId())
                .itemId(original.getItemId())
                .locationId(original.getLocationId())
                .deptId(original.getDeptId())
                .txnDate(original.getTxnDate())
                .txnType(reversalType)
                .refId(original.getLedgerId())
                .qtyIn(isInward ? BigDecimal.ZERO : originalQty)
                .qtyOut(isInward ? originalQty : BigDecimal.ZERO)
                .rate(original.getRate())
                .balanceQty(BigDecimal.ZERO)
                .remarks("Reversal: " + request.getReason())
                .build();
        reversal = ledgerRepository.save(reversal);

        // Create correction entry
        MaterialStockLedger correction = MaterialStockLedger.builder()
                .branchId(original.getBranchId())
                .itemId(original.getItemId())
                .locationId(original.getLocationId())
                .deptId(original.getDeptId())
                .txnDate(original.getTxnDate())
                .txnType(correctionType)
                .refId(original.getLedgerId())
                .qtyIn(isInward ? request.getCorrectedQty() : BigDecimal.ZERO)
                .qtyOut(isInward ? BigDecimal.ZERO : request.getCorrectedQty())
                .rate(correctedRate)
                .balanceQty(BigDecimal.ZERO)
                .remarks("Correction: " + request.getReason())
                .build();
        correction = ledgerRepository.save(correction);

        // Recalculate all balances
        int recalcCount = recalculateBalances(
                original.getBranchId(), original.getItemId(), original.getLocationId());

        // Update summary table
        updateBranchStockFromLedger(
                original.getBranchId(), original.getItemId(), original.getLocationId());

        log.info("Stock correction applied: original={}, reversal={}, correction={}, "
                        + "oldQty={}, newQty={}, recalculated={}",
                original.getLedgerId(), reversal.getLedgerId(), correction.getLedgerId(),
                originalQty, request.getCorrectedQty(), recalcCount);

        return StockCorrectionResponse.builder()
                .originalLedgerId(original.getLedgerId())
                .reversalLedgerId(reversal.getLedgerId())
                .correctionLedgerId(correction.getLedgerId())
                .txnType(original.getTxnType())
                .oldQty(originalQty)
                .newQty(request.getCorrectedQty())
                .oldRate(original.getRate())
                .newRate(correctedRate)
                .balancesRecalculated(recalcCount)
                .build();
    }

    private void validateCorrectionAllowed(MaterialStockLedger original,
                                           StockCorrectionRequest request) {
        // Cannot correct a reversal or correction entry
        if (!REVERSAL_TYPE_MAP.containsKey(original.getTxnType())) {
            throw new MmsException("INVALID_CORRECTION_TARGET",
                    "Cannot correct a reversal/correction entry. "
                            + "Original txn_type: " + original.getTxnType());
        }

        // Check 5-day window
        LocalDate today = LocalDate.now(IST);
        long daysSinceTxn = ChronoUnit.DAYS.between(original.getTxnDate(), today);
        if (daysSinceTxn > CORRECTION_WINDOW_DAYS) {
            throw new MmsException("CORRECTION_WINDOW_EXPIRED",
                    "Correction window expired. Transaction date: " + original.getTxnDate()
                            + ", days elapsed: " + daysSinceTxn
                            + ", allowed: " + CORRECTION_WINDOW_DAYS);
        }

        // Check for no change
        boolean isInward = original.getQtyIn().compareTo(BigDecimal.ZERO) > 0;
        BigDecimal originalQty = isInward ? original.getQtyIn() : original.getQtyOut();
        BigDecimal correctedRate = request.getCorrectedRate() != null
                ? request.getCorrectedRate() : original.getRate();

        if (originalQty.compareTo(request.getCorrectedQty()) == 0
                && original.getRate().compareTo(correctedRate) == 0) {
            throw new MmsException("NO_CHANGE_DETECTED",
                    "Corrected qty and rate are the same as original. No correction needed.");
        }

        // Reject zero qty
        if (request.getCorrectedQty().compareTo(BigDecimal.ZERO) <= 0) {
            throw new MmsException("INVALID_CORRECTION_QTY",
                    "Corrected quantity must be greater than zero.");
        }
    }

    private int recalculateBalances(String branchId, String itemId, String locationId) {
        List<MaterialStockLedger> entries = ledgerRepository
                .findAllByBranchItemLocationOrdered(branchId, itemId, locationId);

        BigDecimal runningBalance = BigDecimal.ZERO;
        int count = 0;

        for (MaterialStockLedger entry : entries) {
            runningBalance = runningBalance.add(entry.getQtyIn()).subtract(entry.getQtyOut());

            if (runningBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new MmsException("NEGATIVE_BALANCE",
                        "Correction would cause negative balance at ledger entry "
                                + entry.getLedgerId() + " (balance: " + runningBalance + ")");
            }

            if (entry.getBalanceQty().compareTo(runningBalance) != 0) {
                entry.setBalanceQty(runningBalance);
                ledgerRepository.save(entry);
                count++;
            }
        }

        return count;
    }

    private void updateBranchStockFromLedger(String branchId, String itemId, String locationId) {
        List<MaterialStockLedger> allEntries = ledgerRepository
                .findAllByBranchItemLocationOrdered(branchId, itemId, locationId);

        if (allEntries.isEmpty()) {
            return;
        }

        // Final balance is from the last entry
        BigDecimal finalBalance = allEntries.get(allEntries.size() - 1).getBalanceQty();

        // Compute weighted average cost from all inward entries
        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal totalQtyIn = BigDecimal.ZERO;

        for (MaterialStockLedger entry : allEntries) {
            if (isInwardEntry(entry)) {
                totalValue = totalValue.add(entry.getQtyIn().multiply(entry.getRate()));
                totalQtyIn = totalQtyIn.add(entry.getQtyIn());
            }
        }

        BigDecimal avgCost = totalQtyIn.compareTo(BigDecimal.ZERO) > 0
                ? totalValue.divide(totalQtyIn, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BranchMaterialStock stock = branchStockRepository
                .findStockForUpdate(branchId, itemId, locationId)
                .orElse(BranchMaterialStock.builder()
                        .branchId(branchId)
                        .itemId(itemId)
                        .locationId(locationId)
                        .qtyOnHand(BigDecimal.ZERO)
                        .avgCost(BigDecimal.ZERO)
                        .build());

        stock.setQtyOnHand(finalBalance);
        stock.setAvgCost(avgCost);
        branchStockRepository.save(stock);
    }

    private boolean isInwardEntry(MaterialStockLedger entry) {
        return INWARD_TYPES.contains(entry.getTxnType())
                && entry.getQtyIn().compareTo(BigDecimal.ZERO) > 0;
    }
}
