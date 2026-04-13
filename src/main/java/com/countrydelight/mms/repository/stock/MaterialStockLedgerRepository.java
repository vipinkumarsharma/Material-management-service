package com.countrydelight.mms.repository.stock;

import com.countrydelight.mms.entity.stock.MaterialStockLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MaterialStockLedgerRepository extends JpaRepository<MaterialStockLedger, Long> {

    @Query("""
            SELECT l FROM MaterialStockLedger l
            WHERE l.branchId = :branchId AND l.itemId = :itemId
            ORDER BY l.createdOn DESC
            """)
    List<MaterialStockLedger> findByItemAndBranch(String branchId, String itemId);

    @Query("""
            SELECT l FROM MaterialStockLedger l
            WHERE l.branchId = :branchId AND l.itemId = :itemId
            AND l.locationId = :locationId
            ORDER BY l.createdOn DESC
            """)
    List<MaterialStockLedger> findByItemBranchLocation(
            String branchId, String itemId, String locationId);

    @Query("""
        SELECT l FROM MaterialStockLedger l
        WHERE l.branchId = :branchId AND l.itemId = :itemId
        AND l.locationId = :locationId
        ORDER BY l.createdOn DESC
        LIMIT 1
        """)
    MaterialStockLedger findLatestLedgerEntry(String branchId, String itemId, String locationId);

    /**
     * Get last GRN rate for an item at a branch (for price suggestion during GRN entry)
     */
    @Query("""
        SELECT l.rate FROM MaterialStockLedger l
        WHERE l.branchId = :branchId
        AND l.itemId = :itemId
        AND l.txnType = 'GRN'
        ORDER BY l.createdOn DESC
        LIMIT 1
        """)
    BigDecimal findLastGrnRateForItem(String branchId, String itemId);

    /**
     * Get ledger entries for aging calculation
     */
    @Query("""
        SELECT l FROM MaterialStockLedger l
        WHERE l.branchId = :branchId
        AND l.itemId = :itemId
        AND l.txnType = 'GRN'
        AND l.txnDate <= :asOfDate
        ORDER BY l.txnDate ASC
        """)
    List<MaterialStockLedger> findGrnEntriesForAging(String branchId, String itemId, LocalDate asOfDate);

    /**
     * Find ledger entries by transaction type and reference
     */
    List<MaterialStockLedger> findByTxnTypeAndRefId(String txnType, Long refId);

    @Query("""
            SELECT l FROM MaterialStockLedger l
            WHERE l.branchId = :branchId AND l.itemId = :itemId
            AND l.locationId = :locationId
            ORDER BY l.createdOn ASC
            """)
    List<MaterialStockLedger> findAllByBranchItemLocationOrdered(
            String branchId, String itemId, String locationId);

    boolean existsByTxnTypeNot(String txnType);

    @Query("SELECT COUNT(m) > 0 FROM MaterialStockLedger m WHERE m.branchId = :branchId AND m.locationId = :locationId AND m.txnType <> :txnType")
    boolean hasNonObEntriesForLocation(@Param("branchId") String branchId, @Param("locationId") String locationId, @Param("txnType") String txnType);

    @Query("SELECT COUNT(m) > 0 FROM MaterialStockLedger m WHERE m.branchId = :branchId AND m.itemId = :itemId AND m.locationId = :locationId AND m.txnType <> :txnType")
    boolean hasNonObEntriesForBranchItemLocation(@Param("branchId") String branchId, @Param("itemId") String itemId, @Param("locationId") String locationId, @Param("txnType") String txnType);

    @Modifying
    @Query("DELETE FROM MaterialStockLedger m WHERE m.txnType = :txnType")
    void deleteByTxnType(@Param("txnType") String txnType);

    @Modifying
    @Query("DELETE FROM MaterialStockLedger m WHERE m.branchId = :branchId AND m.locationId = :locationId AND m.txnType = :txnType")
    void deleteByBranchIdAndLocationIdAndTxnType(
            @Param("branchId") String branchId,
            @Param("locationId") String locationId,
            @Param("txnType") String txnType);

    // Delete ledger entries for a specific branch+location+item for the given txn type
    @Modifying
    @Query("DELETE FROM MaterialStockLedger m WHERE m.branchId = :branchId AND m.locationId = :locationId AND m.itemId = :itemId AND m.txnType = :txnType")
    void deleteObEntriesForBranchLocationItem(
            @Param("branchId") String branchId,
            @Param("locationId") String locationId,
            @Param("itemId") String itemId,
            @Param("txnType") String txnType);

    List<MaterialStockLedger> findByBranchIdAndLocationIdAndTxnType(
            String branchId, String locationId, String txnType);
}
