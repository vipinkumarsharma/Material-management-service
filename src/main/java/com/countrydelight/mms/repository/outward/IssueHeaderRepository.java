package com.countrydelight.mms.repository.outward;

import com.countrydelight.mms.entity.outward.IssueHeader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IssueHeaderRepository extends JpaRepository<IssueHeader, Long> {

    @Query("SELECT i FROM IssueHeader i WHERE i.branchId = :branchId")
    Page<IssueHeader> findByBranchId(@Param("branchId") String branchId, Pageable pageable);

    @Query("SELECT i FROM IssueHeader i WHERE i.branchId = :branchId AND i.status = :status")
    Page<IssueHeader> findByBranchIdAndStatus(@Param("branchId") String branchId, @Param("status") String status, Pageable pageable);

    @Query("SELECT i FROM IssueHeader i WHERE i.issueDate BETWEEN :startDate AND :endDate")
    Page<IssueHeader> findByIssueDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);

    @Query("""
            SELECT i FROM IssueHeader i
            WHERE i.status = 'PENDING_APPROVAL'
            ORDER BY i.issueId DESC
            """)
    List<IssueHeader> findPendingApproval();
}
