package com.countrydelight.mms.dto.master;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ItemBulkUploadResult {
    private int totalRows;
    private int savedCount;
    private int skippedCount;
    private List<RowError> errors;

    @Getter
    @Builder
    public static class RowError {
        private int rowNumber;
        private String itemId;
        private String reason;
    }
}
