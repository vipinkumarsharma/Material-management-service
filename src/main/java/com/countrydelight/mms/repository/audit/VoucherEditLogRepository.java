package com.countrydelight.mms.repository.audit;

import com.countrydelight.mms.entity.audit.VoucherEditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoucherEditLogRepository extends JpaRepository<VoucherEditLog, Long> {

    @Query("""
            SELECT v FROM VoucherEditLog v
            WHERE v.entityType = :type AND v.entityId = :id
            ORDER BY v.changedAt ASC
            """)
    Page<VoucherEditLog> findByEntity(
            @Param("type") String entityType, @Param("id") Long entityId, Pageable pageable);

    @Query("""
            SELECT v FROM VoucherEditLog v
            WHERE v.changedBy = :by
            ORDER BY v.changedAt DESC
            LIMIT 200
            """)
    List<VoucherEditLog> findByChanger(@Param("by") String changedBy);
}
