package com.countrydelight.mms.repository.transfer;

import com.countrydelight.mms.entity.transfer.StockTransferHeader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface StockTransferHeaderRepository extends JpaRepository<StockTransferHeader, Long> {

    @Query("SELECT t FROM StockTransferHeader t WHERE t.fromBranch = :fromBranch")
    Page<StockTransferHeader> findByFromBranch(@Param("fromBranch") String fromBranch, Pageable pageable);

    @Query("SELECT t FROM StockTransferHeader t WHERE t.toBranch = :toBranch")
    Page<StockTransferHeader> findByToBranch(@Param("toBranch") String toBranch, Pageable pageable);

    @Query("SELECT t FROM StockTransferHeader t WHERE t.status = :status")
    Page<StockTransferHeader> findByStatus(@Param("status") String status, Pageable pageable);

    @Query("SELECT t FROM StockTransferHeader t WHERE t.transferDate BETWEEN :startDate AND :endDate")
    Page<StockTransferHeader> findByTransferDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);

    @Query("""
            SELECT t FROM StockTransferHeader t
            WHERE t.toBranch = :branchId AND t.status = 'IN_TRANSIT'
            ORDER BY t.transferId DESC
            """)
    Page<StockTransferHeader> findPendingReceiptForBranch(@Param("branchId") String branchId, Pageable pageable);

    @Query("""
            SELECT t FROM StockTransferHeader t
            WHERE t.fromBranch = :branchId AND t.status = 'CREATED'
            ORDER BY t.transferId DESC
            """)
    Page<StockTransferHeader> findPendingDispatchForBranch(@Param("branchId") String branchId, Pageable pageable);
}
