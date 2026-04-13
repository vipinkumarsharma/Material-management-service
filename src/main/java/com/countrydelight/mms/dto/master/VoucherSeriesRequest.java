package com.countrydelight.mms.dto.master;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoucherSeriesRequest {

    @Size(max = 20)
    private String seriesId;

    @NotBlank
    @Size(max = 100)
    private String seriesName;

    @Size(max = 20)
    private String voucherTypeId;

    private Integer startingNumber = 1;
    private Integer numberWidth = 6;
    private boolean prefillWithZero = true;
    private String prefixDetails = "";
    private String suffixDetails = "";
    private String restartPeriodicity = "ANNUALLY";
    private boolean defaultSeries = false;
}
