package com.countrydelight.mms.repository.master;

import com.countrydelight.mms.entity.master.SupplierGodownMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierGodownMapRepository extends JpaRepository<SupplierGodownMap, Long> {

    List<SupplierGodownMap> findBySuppId(String suppId);
    Page<SupplierGodownMap> findBySuppId(String suppId, Pageable pageable);
}
