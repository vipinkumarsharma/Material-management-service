package com.countrydelight.mms.exception;

import lombok.Getter;
import java.math.BigDecimal;
import java.util.Locale;

@Getter
public class InsufficientStockException extends MmsException {
    private final String itemId;
    private final String locationId;
    private final BigDecimal requestedQty;
    private final BigDecimal availableQty;

    public InsufficientStockException(String itemId, String locationId,
                                       BigDecimal requestedQty, BigDecimal availableQty) {
        super("INSUFFICIENT_STOCK",
                String.format(Locale.ROOT,
                        "Insufficient stock for item %s at location %s. Requested: %s, Available: %s",
                        itemId, locationId, requestedQty, availableQty));
        this.itemId = itemId;
        this.locationId = locationId;
        this.requestedQty = requestedQty;
        this.availableQty = availableQty;
    }
}
