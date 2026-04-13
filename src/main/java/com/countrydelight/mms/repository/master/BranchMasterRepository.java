package com.countrydelight.mms.repository.master;

import com.countrydelight.mms.entity.master.BranchMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchMasterRepository extends JpaRepository<BranchMaster, String> {

    @Query("""
            SELECT b FROM BranchMaster b WHERE
            (:branchId IS NULL OR b.branchId LIKE CONCAT('%', :branchId, '%')) AND
            (:name IS NULL OR b.branchName LIKE CONCAT('%', :name, '%')) AND
            (:pincode IS NULL OR b.pincode = :pincode) AND
            (:companyId IS NULL OR b.companyId = :companyId)
            """)
    Page<BranchMaster> findByFilters(@Param("branchId") String branchId,
                                     @Param("name") String name,
                                     @Param("pincode") String pincode,
                                     @Param("companyId") String companyId,
                                     Pageable pageable);
}
