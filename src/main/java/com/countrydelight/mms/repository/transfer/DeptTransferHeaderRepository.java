package com.countrydelight.mms.repository.transfer;

import com.countrydelight.mms.entity.transfer.DeptTransferHeader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeptTransferHeaderRepository extends JpaRepository<DeptTransferHeader, Long> {

    @Query("""
            SELECT DISTINCT d FROM DeptTransferHeader d
            LEFT JOIN FETCH d.details
            WHERE (:branchId IS NULL OR d.fromBranchId = :branchId OR d.toBranchId = :branchId)
            AND (:deptId IS NULL OR d.fromDeptId = :deptId OR d.toDeptId = :deptId)
            ORDER BY d.deptTransferId DESC
            """)
    List<DeptTransferHeader> findByFilters(@Param("branchId") String branchId, @Param("deptId") Integer deptId);

    @Query("""
            SELECT d FROM DeptTransferHeader d
            WHERE (:branchId IS NULL OR d.fromBranchId = :branchId OR d.toBranchId = :branchId)
            AND (:deptId IS NULL OR d.fromDeptId = :deptId OR d.toDeptId = :deptId)
            """)
    Page<DeptTransferHeader> findByFiltersPaged(
            @Param("branchId") String branchId,
            @Param("deptId") Integer deptId,
            Pageable pageable);

    @Query("""
            SELECT d FROM DeptTransferHeader d
            LEFT JOIN FETCH d.details
            WHERE d.deptTransferId = :id
            """)
    Optional<DeptTransferHeader> findByIdWithDetails(@Param("id") Long id);
}
