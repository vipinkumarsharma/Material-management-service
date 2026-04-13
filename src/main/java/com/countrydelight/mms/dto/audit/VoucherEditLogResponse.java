package com.countrydelight.mms.dto.audit;

import com.countrydelight.mms.entity.audit.VoucherEditLog;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class VoucherEditLogResponse {

    private Long logId;
    private String entityType;
    private Long entityId;
    private String voucherNumber;
    private String voucherTypeId;
    private String changeType;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private String changedBy;
    private LocalDateTime changedAt;
    private String remarks;

    public static VoucherEditLogResponse from(VoucherEditLog e) {
        return VoucherEditLogResponse.builder()
                .logId(e.getLogId())
                .entityType(e.getEntityType())
                .entityId(e.getEntityId())
                .voucherNumber(e.getVoucherNumber())
                .voucherTypeId(e.getVoucherTypeId())
                .changeType(e.getChangeType())
                .fieldName(e.getFieldName())
                .oldValue(e.getOldValue())
                .newValue(e.getNewValue())
                .changedBy(e.getChangedBy())
                .changedAt(e.getChangedAt())
                .remarks(e.getRemarks())
                .build();
    }
}
