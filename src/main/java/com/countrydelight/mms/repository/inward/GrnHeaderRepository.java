package com.countrydelight.mms.repository.inward;

import com.countrydelight.mms.entity.inward.GrnHeader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GrnHeaderRepository extends JpaRepository<GrnHeader, Long> {

    @Query("SELECT g FROM GrnHeader g WHERE g.branchId = :branchId")
    Page<GrnHeader> findByBranchId(@Param("branchId") String branchId, Pageable pageable);

    @Query("SELECT g FROM GrnHeader g WHERE g.branchId = :branchId AND g.status = :status")
    Page<GrnHeader> findByBranchIdAndStatus(@Param("branchId") String branchId, @Param("status") String status, Pageable pageable);

    @Query("SELECT g FROM GrnHeader g WHERE g.suppId = :suppId")
    Page<GrnHeader> findBySuppId(@Param("suppId") String suppId, Pageable pageable);

    @Query("SELECT g FROM GrnHeader g WHERE g.pvId = :pvId") // backend-guard:ignore Optional return type limits to 1 result; Spring Data throws IncorrectResultSizeDataAccessException if multiple rows match
    Optional<GrnHeader> findByPvId(@Param("pvId") Long pvId);

    @Query("SELECT g FROM GrnHeader g WHERE g.pvId = :pvId AND g.status = :status")
    List<GrnHeader> findByPvIdAndStatus(@Param("pvId") Long pvId, @Param("status") String status);

    @Query("SELECT g FROM GrnHeader g WHERE g.grnDate BETWEEN :startDate AND :endDate")
    Page<GrnHeader> findByGrnDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);

    @Query("""
            SELECT g FROM GrnHeader g
            WHERE g.status = 'PENDING_APPROVAL'
            ORDER BY g.grnId DESC
            """)
    Page<GrnHeader> findPendingApproval(Pageable pageable);

    @Query("""
            SELECT DISTINCT g FROM GrnHeader g
            LEFT JOIN FETCH g.details
            WHERE g.grnId = :grnId
            """)
    Optional<GrnHeader> findByIdWithDetails(@Param("grnId") Long grnId);

    @Query(value = """
            SELECT DISTINCT g FROM GrnHeader g
            LEFT JOIN FETCH g.details
            WHERE (:grnId IS NULL OR g.grnId = :grnId)
            AND (:branchId IS NULL OR g.branchId = :branchId)
            AND (:pvId IS NULL OR g.pvId = :pvId)
            ORDER BY g.grnId DESC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT g) FROM GrnHeader g
            WHERE (:grnId IS NULL OR g.grnId = :grnId)
            AND (:branchId IS NULL OR g.branchId = :branchId)
            AND (:pvId IS NULL OR g.pvId = :pvId)
            """)
    Page<GrnHeader> findByFilters(@Param("grnId") Long grnId,
                                   @Param("branchId") String branchId,
                                   @Param("pvId") Long pvId,
                                   Pageable pageable);
}
