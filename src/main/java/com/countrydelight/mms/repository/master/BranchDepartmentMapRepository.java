package com.countrydelight.mms.repository.master;

import com.countrydelight.mms.entity.master.BranchDepartmentMap;
import com.countrydelight.mms.entity.master.BranchDepartmentMapId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchDepartmentMapRepository extends JpaRepository<BranchDepartmentMap, BranchDepartmentMapId> {
    List<BranchDepartmentMap> findByBranchId(String branchId);
    List<BranchDepartmentMap> findByDeptId(Integer deptId);
    Page<BranchDepartmentMap> findByBranchId(String branchId, Pageable pageable);
    Page<BranchDepartmentMap> findByDeptId(Integer deptId, Pageable pageable);
    boolean existsByBranchIdAndDeptId(String branchId, Integer deptId);
}
