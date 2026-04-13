package com.countrydelight.mms.repository.inward;

import com.countrydelight.mms.entity.inward.GrnDetail;
import com.countrydelight.mms.entity.inward.GrnDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;

@Repository
public interface GrnDetailRepository extends JpaRepository<GrnDetail, GrnDetailId> {
    List<GrnDetail> findByGrnId(Long grnId);

    /**
     * Find GRN details with remaining stock for FIFO consumption.
     * Orders by GRN date (oldest first) for FIFO.
     * Uses pessimistic lock to prevent concurrent consumption issues.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT gd FROM GrnDetail gd
        JOIN gd.grnHeader gh
        WHERE gd.itemId = :itemId
        AND gd.locationId = :locationId
        AND gh.branchId = :branchId
        AND gh.status = 'POSTED'
        AND gd.qtyRemaining > 0
        ORDER BY gh.grnDate ASC, gh.grnId ASC
        """)
    List<GrnDetail> findAvailableStockForFifo(String branchId, String itemId, String locationId);

    /**
     * Get the last GRN rate for an item at a branch (for price suggestion)
     */
    @Query("""
        SELECT gd FROM GrnDetail gd
        JOIN gd.grnHeader gh
        WHERE gd.itemId = :itemId
        AND gh.branchId = :branchId
        AND gh.status = 'POSTED'
        ORDER BY gh.grnDate DESC, gh.grnId DESC
        LIMIT 1
        """)
    GrnDetail findLastGrnForItem(String branchId, String itemId);
}
