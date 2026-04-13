package com.countrydelight.mms.dto.master;

import com.countrydelight.mms.entity.master.VoucherSeriesMaster;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class VoucherSeriesResponse {

    private String seriesId;
    private String seriesName;
    private String voucherTypeId;
    private Integer startingNumber;
    private Integer currentNumber;
    private Integer numberWidth;
    private boolean prefillWithZero;
    private String prefixDetails;
    private String suffixDetails;
    private String restartPeriodicity;
    private LocalDate lastResetDate;
    private LocalDate nextRestartDate;
    private boolean defaultSeries;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static VoucherSeriesResponse from(VoucherSeriesMaster e) {
        return VoucherSeriesResponse.builder()
                .seriesId(e.getSeriesId())
                .seriesName(e.getSeriesName())
                .voucherTypeId(e.getVoucherTypeId())
                .startingNumber(e.getStartingNumber())
                .currentNumber(e.getCurrentNumber())
                .numberWidth(e.getNumberWidth())
                .prefillWithZero(e.isPrefillWithZero())
                .prefixDetails(e.getPrefixDetails())
                .suffixDetails(e.getSuffixDetails())
                .restartPeriodicity(e.getRestartPeriodicity())
                .lastResetDate(e.getLastResetDate())
                .nextRestartDate(e.getNextRestartDate())
                .defaultSeries(e.isDefaultSeries())
                .active(e.isActive())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
