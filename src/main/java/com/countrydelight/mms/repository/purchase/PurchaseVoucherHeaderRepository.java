package com.countrydelight.mms.repository.purchase;

import com.countrydelight.mms.entity.purchase.PurchaseVoucherHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseVoucherHeaderRepository extends JpaRepository<PurchaseVoucherHeader, Long> {

    Optional<PurchaseVoucherHeader> findByVoucherNumber(String voucherNumber);

    List<PurchaseVoucherHeader> findTop200ByBranchIdOrderByPvIdDesc(String branchId);

    @Query("""
            SELECT p FROM PurchaseVoucherHeader p
            WHERE p.branchId = :branchId AND p.status = :status
            ORDER BY p.pvId DESC
            LIMIT 200
            """)
    List<PurchaseVoucherHeader> findByBranchAndStatus(
            @Param("branchId") String branchId, @Param("status") String status);

    @Query("""
            SELECT p FROM PurchaseVoucherHeader p
            WHERE (:branchId IS NULL OR p.branchId = :branchId)
            AND (:supplierFromId IS NULL OR p.supplierFromId = :supplierFromId)
            AND (:status IS NULL OR p.status = :status)
            AND (:voucherCategory IS NULL OR p.voucherCategory = :voucherCategory)
            AND (:voucherTypeId IS NULL OR p.voucherTypeId = :voucherTypeId)
            ORDER BY p.pvId DESC
            """)
    Page<PurchaseVoucherHeader> findByFilters(
            @Param("branchId") String branchId,
            @Param("supplierFromId") String supplierFromId,
            @Param("status") String status,
            @Param("voucherCategory") String voucherCategory,
            @Param("voucherTypeId") String voucherTypeId,
            Pageable pageable);

    @Query("""
            SELECT p FROM PurchaseVoucherHeader p
            WHERE (:branchId IS NULL OR p.branchId = :branchId)
            AND (:voucherTypeId IS NULL OR p.voucherTypeId = :voucherTypeId)
            AND (:fromDate IS NULL OR p.pvDate >= :fromDate)
            AND (:toDate IS NULL OR p.pvDate <= :toDate)
            ORDER BY p.pvId DESC
            """)
    Page<PurchaseVoucherHeader> findForRegister(
            @Param("branchId") String branchId,
            @Param("voucherTypeId") String voucherTypeId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable);

}
