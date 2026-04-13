package com.countrydelight.mms.repository.stock;

import com.countrydelight.mms.entity.stock.BranchMaterialStock;
import com.countrydelight.mms.entity.stock.BranchMaterialStockId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchMaterialStockRepository extends JpaRepository<BranchMaterialStock, BranchMaterialStockId> {

    List<BranchMaterialStock> findByBranchId(String branchId);

    List<BranchMaterialStock> findByBranchIdAndItemId(String branchId, String itemId);

    Optional<BranchMaterialStock> findByBranchIdAndItemIdAndLocationId(
            String branchId, String itemId, String locationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT b FROM BranchMaterialStock b
            WHERE b.branchId = :branchId
            AND b.itemId = :itemId
            AND b.locationId = :locationId
            """)
    Optional<BranchMaterialStock> findStockForUpdate(
            String branchId, String itemId, String locationId);

    @Query("""
        SELECT b FROM BranchMaterialStock b
        WHERE b.branchId = :branchId
        AND b.qtyOnHand > 0
        ORDER BY b.itemId
        """)
    Page<BranchMaterialStock> findAllWithStockByBranch(String branchId, Pageable pageable);

    @Query("""
        SELECT b FROM BranchMaterialStock b
        WHERE b.branchId = :branchId
        AND b.locationId = :locationId
        AND b.qtyOnHand > 0
        """)
    List<BranchMaterialStock> findAllWithStockByBranchAndLocation(String branchId, String locationId);

    void deleteByBranchIdAndLocationId(String branchId, String locationId);

    // Delete a specific item's summary record for a branch+location
    void deleteByBranchIdAndLocationIdAndItemId(String branchId, String locationId, String itemId);
}
