package com.countrydelight.mms.exception;

import com.countrydelight.mms.dto.inward.PriceVarianceInfo;
import lombok.Getter;
import java.util.List;
import java.util.Locale;

@Getter
public class ApprovalRequiredException extends MmsException {
    private final String txnType;
    private final List<PriceVarianceInfo> priceVariances;

    public ApprovalRequiredException(String txnType, List<PriceVarianceInfo> priceVariances) {
        super("APPROVAL_REQUIRED",
                String.format(Locale.ROOT,
                        "Transaction requires approval due to price variance. Transaction type: %s", txnType));
        this.txnType = txnType;
        this.priceVariances = priceVariances;
    }
}
