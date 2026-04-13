package com.countrydelight.mms.entity.master;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "voucher_series_restart_schedule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherSeriesRestartSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "restart_id")
    private Long restartId;

    @Column(name = "series_id", nullable = false, length = 20)
    private String seriesId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id", insertable = false, updatable = false)
    private VoucherSeriesMaster series;

    @Column(name = "applicable_from_date", nullable = false)
    private LocalDate applicableFromDate;

    @Column(name = "starting_number")
    @Builder.Default
    private Integer startingNumber = 1;

    @Column(name = "prefix_override", length = 100)
    private String prefixOverride;

    @Column(name = "suffix_override", length = 100)
    private String suffixOverride;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
