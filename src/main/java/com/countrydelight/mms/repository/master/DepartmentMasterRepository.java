package com.countrydelight.mms.repository.master;

import com.countrydelight.mms.entity.master.DepartmentMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentMasterRepository extends JpaRepository<DepartmentMaster, Integer> {
    List<DepartmentMaster> findByDeptNameContainingIgnoreCase(String name);
    Page<DepartmentMaster> findByDeptNameContainingIgnoreCase(String name, Pageable pageable);
    Page<DepartmentMaster> findByDeptIdIn(List<Integer> ids, Pageable pageable);
}
