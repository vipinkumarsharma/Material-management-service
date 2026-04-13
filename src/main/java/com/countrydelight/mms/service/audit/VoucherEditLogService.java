package com.countrydelight.mms.service.audit;

import com.countrydelight.mms.entity.audit.VoucherEditLog;
import com.countrydelight.mms.repository.audit.VoucherEditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoucherEditLogService {

    private final VoucherEditLogRepository editLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logCreate(String entityType, Long entityId, String voucherNumber,
                          String voucherTypeId, String changedBy, String summary) {
        VoucherEditLog entry = VoucherEditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .voucherNumber(voucherNumber)
                .voucherTypeId(voucherTypeId)
                .changeType("CREATE")
                .newValue(summary)
                .changedBy(changedBy)
                .changedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")))
                .build();
        editLogRepository.save(entry);
        log.debug("EditLog CREATE: {}/{}", entityType, entityId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logStatusChange(String entityType, Long entityId, String voucherNumber,
                                String voucherTypeId, String oldStatus, String newStatus,
                                String changedBy) {
        VoucherEditLog entry = VoucherEditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .voucherNumber(voucherNumber)
                .voucherTypeId(voucherTypeId)
                .changeType("STATUS_CHANGE")
                .fieldName("status")
                .oldValue(oldStatus)
                .newValue(newStatus)
                .changedBy(changedBy)
                .changedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")))
                .build();
        editLogRepository.save(entry);
        log.debug("EditLog STATUS_CHANGE: {}/{} {} -> {}", entityType, entityId, oldStatus, newStatus);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logApprove(String entityType, Long entityId, String voucherNumber,
                           String voucherTypeId, String approvedBy) {
        VoucherEditLog entry = VoucherEditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .voucherNumber(voucherNumber)
                .voucherTypeId(voucherTypeId)
                .changeType("APPROVE")
                .changedBy(approvedBy)
                .changedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")))
                .build();
        editLogRepository.save(entry);
    }

    /** Holds the field-level change details for {@link #logFieldUpdate}. */
    public record FieldChange(String fieldName, String oldValue, String newValue) {}

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFieldUpdate(String entityType, Long entityId, String voucherNumber,
                               String voucherTypeId, FieldChange change, String changedBy) {
        VoucherEditLog entry = VoucherEditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .voucherNumber(voucherNumber)
                .voucherTypeId(voucherTypeId)
                .changeType("UPDATE")
                .fieldName(change.fieldName())
                .oldValue(change.oldValue())
                .newValue(change.newValue())
                .changedBy(changedBy)
                .changedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")))
                .build();
        editLogRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public Page<VoucherEditLog> getLog(String entityType, Long entityId, int page, int size) {
        return editLogRepository.findByEntity(entityType, entityId, PageRequest.of(page - 1, size));
    }
}
