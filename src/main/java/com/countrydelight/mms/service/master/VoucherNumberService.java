package com.countrydelight.mms.service.master;

import com.countrydelight.mms.entity.master.BranchMaster;
import com.countrydelight.mms.entity.master.VoucherSeriesMaster;
import com.countrydelight.mms.entity.master.VoucherSeriesRestartSchedule;
import com.countrydelight.mms.exception.MmsException;
import com.countrydelight.mms.repository.master.BranchMasterRepository;
import com.countrydelight.mms.repository.master.VoucherSeriesMasterRepository;
import com.countrydelight.mms.repository.master.VoucherSeriesRestartScheduleRepository;
import com.countrydelight.mms.repository.master.VoucherTypeMasterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoucherNumberService {

    private final VoucherSeriesMasterRepository seriesRepository;
    private final VoucherSeriesRestartScheduleRepository restartScheduleRepository;
    private final VoucherTypeMasterRepository voucherTypeMasterRepository;
    private final BranchMasterRepository branchMasterRepository;

    /**
     * Generate the next voucher number for the given type + branch.
     * Uses PESSIMISTIC_WRITE lock to prevent duplicates under concurrent writes.
     */
    @Transactional
    public String generateVoucherNumber(String voucherTypeId, String branchId) {
        VoucherSeriesMaster series = seriesRepository.findDefaultSeriesForUpdate(voucherTypeId)
                .orElseThrow(() -> new MmsException(
                        "No default active series found for type=" + voucherTypeId + ", branch=" + branchId));

        LocalDate today = LocalDate.now(java.time.ZoneId.of("Asia/Kolkata"));

        // 1. Check custom restart schedule first
        boolean restarted = applyRestartScheduleIfNeeded(series, today);

        // 2. If no custom schedule applied, check periodicity-based restart
        if (!restarted) {
            applyPeriodicityRestartIfNeeded(series, today);
        }

        String branchCode = branchMasterRepository.findById(branchId)
                .map(BranchMaster::getBranchCode)
                .orElse(null);

        var voucherType = voucherTypeMasterRepository.findById(voucherTypeId).orElse(null);
        boolean simpleNumbering = voucherType == null || "No".equals(voucherType.getSetAlterNumbering());

        // Reuse the previously-generated number if it was never saved to any transaction
        int previousNum = series.getCurrentNumber() - 1;
        if (previousNum >= series.getStartingNumber()) {
            String previousFormatted = simpleNumbering
                    ? String.valueOf(previousNum)
                    : formatNumberFor(series, previousNum, branchCode);
            if (seriesRepository.countVoucherNumberUsage(previousFormatted) == 0) {
                log.debug("Re-using unused voucher number {} for type={}", previousFormatted, voucherTypeId);
                return previousFormatted;
            }
        }

        String formatted = simpleNumbering
                ? String.valueOf(series.getCurrentNumber())
                : formatNumber(series, branchCode);
        series.setCurrentNumber(series.getCurrentNumber() + 1);
        seriesRepository.save(series);

        log.debug("Generated voucher number: {} for type={}, branch={}", formatted, voucherTypeId, branchId);
        return formatted;
    }

    /**
     * Preview next number without incrementing the counter.
     */
    @Transactional(readOnly = true)
    public String previewNextNumber(String seriesId, String branchId) {
        VoucherSeriesMaster series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new MmsException("Series not found: " + seriesId));
        var voucherType = voucherTypeMasterRepository.findById(series.getVoucherTypeId()).orElse(null);
        boolean simpleNumbering = voucherType == null || "No".equals(voucherType.getSetAlterNumbering());
        if (simpleNumbering) {
            return String.valueOf(series.getCurrentNumber());
        }
        String branchCode = branchId != null
                ? branchMasterRepository.findById(branchId).map(BranchMaster::getBranchCode).orElse(null)
                : null;
        return formatNumber(series, branchCode);
    }

    private boolean applyRestartScheduleIfNeeded(VoucherSeriesMaster series, LocalDate today) {
        List<VoucherSeriesRestartSchedule> schedules =
                restartScheduleRepository.findApplicableSchedules(series.getSeriesId(), today);
        if (schedules.isEmpty()) {
            return false;
        }

        VoucherSeriesRestartSchedule latest = schedules.get(0);
        // Only apply if last reset was before this schedule's applicableFromDate
        if (series.getLastResetDate() == null || series.getLastResetDate().isBefore(latest.getApplicableFromDate())) {
            series.setCurrentNumber(latest.getStartingNumber());
            series.setLastResetDate(today);
            if (latest.getPrefixOverride() != null) {
                series.setPrefixDetails(latest.getPrefixOverride());
            }
            if (latest.getSuffixOverride() != null) {
                series.setSuffixDetails(latest.getSuffixOverride());
            }
            log.info("Applied restart schedule for series {}: reset to {}", series.getSeriesId(), latest.getStartingNumber());
            return true;
        }
        return false;
    }

    private void applyPeriodicityRestartIfNeeded(VoucherSeriesMaster series, LocalDate today) {
        String periodicity = series.getRestartPeriodicity();
        if ("NONE".equals(periodicity)) {
            return;
        }

        LocalDate lastReset = series.getLastResetDate();
        boolean shouldReset = false;

        switch (periodicity) {
            case "MONTHLY" -> {
                if (lastReset == null || !sameMonth(lastReset, today)) {
                    shouldReset = true;
                }
            }
            case "QUARTERLY" -> {
                if (lastReset == null || !sameQuarter(lastReset, today)) {
                    shouldReset = true;
                }
            }
            case "ANNUALLY" -> {
                if (lastReset == null || lastReset.getYear() != today.getYear()) {
                    shouldReset = true;
                }
            }
            default -> {
                // CUSTOM handled by restart schedule; do nothing here
            }
        }

        if (shouldReset) {
            series.setCurrentNumber(series.getStartingNumber());
            series.setLastResetDate(today);
            log.info("Applied {} periodicity restart for series {}", periodicity, series.getSeriesId());
        }
    }

    /**
     * Return (undo) the last generated voucher number for a type.
     * Runs in its own transaction (REQUIRES_NEW) so it commits even when the
     * caller's transaction is rolling back.
     *
     * Only decrements if the supplied voucherNumber matches the last number
     * that was issued (currentNumber - 1).  This prevents accidental rollback
     * when a caller passes a custom / unrelated string.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void returnVoucherNumber(String voucherTypeId, String voucherNumber, String branchId) {
        if (voucherNumber == null || voucherNumber.isBlank() || voucherTypeId == null || branchId == null) {
            return;
        }
        try {
            VoucherSeriesMaster series = seriesRepository
                    .findDefaultSeries(voucherTypeId).orElse(null);
            if (series == null) {
                return;
            }

            int previousNumber = series.getCurrentNumber() - 1;
            if (previousNumber < series.getStartingNumber()) {
                return;
            }

            String branchCode = branchMasterRepository.findById(branchId)
                    .map(BranchMaster::getBranchCode)
                    .orElse(null);

            var voucherType = voucherTypeMasterRepository.findById(voucherTypeId).orElse(null);
            boolean simpleNumbering = voucherType == null || "No".equals(voucherType.getSetAlterNumbering());
            String expected = simpleNumbering
                    ? String.valueOf(previousNumber)
                    : formatNumberFor(series, previousNumber, branchCode);

            if (voucherNumber.equals(expected)) {
                series.setCurrentNumber(previousNumber);
                seriesRepository.save(series);
                log.info("Returned voucher number {} — counter decremented to {} for type={}",
                        voucherNumber, previousNumber, voucherTypeId);
            } else {
                log.debug("Voucher number {} does not match last generated number for type={} — no decrement",
                        voucherNumber, voucherTypeId);
            }
        } catch (RuntimeException e) {
            log.warn("Could not return voucher number {} for type={}: {}", voucherNumber, voucherTypeId, e.getMessage());
        }
    }

    private String formatNumber(VoucherSeriesMaster series, String branchCode) {
        return formatNumberFor(series, series.getCurrentNumber(), branchCode);
    }

    private String formatNumberFor(VoucherSeriesMaster series, int number, String branchCode) {
        String numStr = series.isPrefillWithZero()
                ? String.format(java.util.Locale.ROOT, "%0" + series.getNumberWidth() + "d", number)
                : String.valueOf(number);
        String code = (branchCode != null && !branchCode.isBlank()) ? branchCode + "/" : "";
        return series.getPrefixDetails() + code + numStr + series.getSuffixDetails();
    }

    private boolean sameMonth(LocalDate a, LocalDate b) {
        return a.getYear() == b.getYear() && a.getMonth() == b.getMonth();
    }

    private boolean sameQuarter(LocalDate a, LocalDate b) {
        return a.getYear() == b.getYear() && getQuarter(a.getMonth()) == getQuarter(b.getMonth());
    }

    private int getQuarter(Month month) {
        return (month.getValue() - 1) / 3 + 1;
    }
}
