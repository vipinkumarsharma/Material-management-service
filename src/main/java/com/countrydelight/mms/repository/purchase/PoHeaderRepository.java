package com.countrydelight.mms.repository.purchase;

import com.countrydelight.mms.entity.purchase.PoHeader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PoHeaderRepository extends JpaRepository<PoHeader, Long> {

    @Query("SELECT p FROM PoHeader p WHERE p.branchId = :branchId")
    Page<PoHeader> findByBranchId(@Param("branchId") String branchId, Pageable pageable);

    @Query("SELECT p FROM PoHeader p WHERE p.suppId = :suppId")
    Page<PoHeader> findBySuppId(@Param("suppId") String suppId, Pageable pageable);

    @Query("SELECT p FROM PoHeader p WHERE p.branchId = :branchId AND p.status = :status")
    Page<PoHeader> findByBranchIdAndStatus(@Param("branchId") String branchId, @Param("status") String status, Pageable pageable);

    @Query("SELECT p FROM PoHeader p WHERE p.poDate BETWEEN :startDate AND :endDate")
    Page<PoHeader> findByPoDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);

    @Query("""
            SELECT p FROM PoHeader p
            WHERE p.branchId = :branchId AND p.suppId = :suppId
            AND p.status IN ('OPEN', 'PARTIAL')
            ORDER BY p.poId DESC
            """)
    List<PoHeader> findOpenPosByBranchAndSupplier(@Param("branchId") String branchId, @Param("suppId") String suppId);
}
