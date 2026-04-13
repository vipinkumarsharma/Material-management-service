package com.countrydelight.mms.repository.master;

import com.countrydelight.mms.entity.master.LocationMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationMasterRepository extends JpaRepository<LocationMaster, String> {

    @Query("SELECT l FROM LocationMaster l WHERE l.branchId = :branchId")
    Page<LocationMaster> findByBranchId(@Param("branchId") String branchId, Pageable pageable);

    @Query("SELECT l FROM LocationMaster l WHERE l.parentId = :parentId")
    Page<LocationMaster> findByParentId(@Param("parentId") String parentId, Pageable pageable);
}
