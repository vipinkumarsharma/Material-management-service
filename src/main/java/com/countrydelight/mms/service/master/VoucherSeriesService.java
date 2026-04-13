package com.countrydelight.mms.service.master;

import com.countrydelight.mms.dto.master.VoucherSeriesRequest;
import com.countrydelight.mms.entity.master.VoucherSeriesMaster;
import com.countrydelight.mms.entity.master.VoucherSeriesRestartSchedule;
import com.countrydelight.mms.exception.MmsException;
import com.countrydelight.mms.repository.master.VoucherSeriesMasterRepository;
import com.countrydelight.mms.repository.master.VoucherSeriesRestartScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VoucherSeriesService {

    private final VoucherSeriesMasterRepository seriesRepository;
    private final VoucherSeriesRestartScheduleRepository restartScheduleRepository;
    private final VoucherNumberService voucherNumberService;

    @Transactional
    public VoucherSeriesMaster create(VoucherSeriesRequest request) {
        if (request.getSeriesId() == null || request.getSeriesId().isBlank()) {
            request.setSeriesId(UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        }
        if (request.getVoucherTypeId() == null || request.getVoucherTypeId().isBlank()) {
            throw new MmsException("voucherTypeId is required");
        }
        if (seriesRepository.existsById(request.getSeriesId())) {
            throw new MmsException("Series already exists: " + request.getSeriesId());
        }
        VoucherSeriesMaster series = VoucherSeriesMaster.builder()
                .seriesId(request.getSeriesId())
                .seriesName(request.getSeriesName())
                .voucherTypeId(request.getVoucherTypeId())
                .startingNumber(request.getStartingNumber())
                .currentNumber(request.getStartingNumber())
                .numberWidth(request.getNumberWidth())
                .prefillWithZero(request.isPrefillWithZero())
                .prefixDetails(request.getPrefixDetails())
                .suffixDetails(request.getSuffixDetails())
                .restartPeriodicity(request.getRestartPeriodicity())
                .defaultSeries(request.isDefaultSeries())
                .build();
        return seriesRepository.save(series);
    }

    @Transactional
    public VoucherSeriesMaster update(String id, VoucherSeriesRequest request) {
        VoucherSeriesMaster series = seriesRepository.findById(id)
                .orElseThrow(() -> new MmsException("Series not found: " + id));
        series.setSeriesName(request.getSeriesName());
        series.setNumberWidth(request.getNumberWidth());
        series.setPrefillWithZero(request.isPrefillWithZero());
        series.setPrefixDetails(request.getPrefixDetails());
        series.setSuffixDetails(request.getSuffixDetails());
        series.setRestartPeriodicity(request.getRestartPeriodicity());
        series.setDefaultSeries(request.isDefaultSeries());
        return seriesRepository.save(series);
    }

    @Transactional(readOnly = true)
    public Page<VoucherSeriesMaster> list(String voucherTypeId, int page, int size) {
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by("seriesId").ascending());
        if (voucherTypeId != null) {
            return seriesRepository.findByVoucherTypeId(voucherTypeId, pageable);
        }
        return seriesRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public VoucherSeriesMaster getById(String id) {
        return seriesRepository.findById(id)
                .orElseThrow(() -> new MmsException("Series not found: " + id));
    }

    @Transactional
    public VoucherSeriesRestartSchedule addRestartSchedule(String seriesId, LocalDate applicableFromDate,
                                                           Integer startingNumber, String prefixOverride,
                                                           String suffixOverride) {
        if (!seriesRepository.existsById(seriesId)) {
            throw new MmsException("Series not found: " + seriesId);
        }
        VoucherSeriesRestartSchedule schedule = VoucherSeriesRestartSchedule.builder()
                .seriesId(seriesId)
                .applicableFromDate(applicableFromDate)
                .startingNumber(startingNumber != null ? startingNumber : 1)
                .prefixOverride(prefixOverride)
                .suffixOverride(suffixOverride)
                .build();
        return restartScheduleRepository.save(schedule);
    }

    @Transactional(readOnly = true)
    public String previewNextNumber(String seriesId, String branchId) {
        return voucherNumberService.previewNextNumber(seriesId, branchId);
    }

    @Transactional(readOnly = true)
    public List<VoucherSeriesRestartSchedule> getRestartSchedules(String seriesId) {
        return restartScheduleRepository.findBySeriesAsc(seriesId);
    }
}
