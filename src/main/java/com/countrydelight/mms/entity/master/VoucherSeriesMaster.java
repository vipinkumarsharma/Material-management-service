package com.countrydelight.mms.entity.master;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Table(name = "voucher_series_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherSeriesMaster {

    @Id
    @Column(name = "series_id", length = 20)
    private String seriesId;

    @Column(name = "series_name", nullable = false, length = 100)
    private String seriesName;

    @Column(name = "voucher_type_id", nullable = false, length = 20)
    private String voucherTypeId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_type_id", insertable = false, updatable = false)
    private VoucherTypeMaster voucherType;

    @Column(name = "starting_number")
    @Builder.Default
    private Integer startingNumber = 1;

    @Column(name = "current_number")
    @Builder.Default
    private Integer currentNumber = 1;

    @Column(name = "number_width")
    @Builder.Default
    private Integer numberWidth = 6;

    @Column(name = "prefill_with_zero")
    @Builder.Default
    private boolean prefillWithZero = true;

    @Column(name = "prefix_details", length = 100)
    @Builder.Default
    private String prefixDetails = "";

    @Column(name = "suffix_details", length = 100)
    @Builder.Default
    private String suffixDetails = "";

    @Column(name = "restart_periodicity", length = 20)
    @Builder.Default
    private String restartPeriodicity = "ANNUALLY";

    @Column(name = "last_reset_date")
    private LocalDate lastResetDate;

    @Column(name = "next_restart_date")
    private LocalDate nextRestartDate;

    @Column(name = "is_default")
    @Builder.Default
    private boolean defaultSeries = false;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
