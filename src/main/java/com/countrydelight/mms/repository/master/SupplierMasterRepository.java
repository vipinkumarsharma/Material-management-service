package com.countrydelight.mms.repository.master;

import com.countrydelight.mms.entity.master.SupplierMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierMasterRepository extends JpaRepository<SupplierMaster, String> {

    @Query("""
            SELECT s FROM SupplierMaster s WHERE
            (:suppId IS NULL OR s.suppId LIKE CONCAT('%', :suppId, '%')) AND
            (:suppName IS NULL OR s.suppName LIKE CONCAT('%', :suppName, '%'))
            """)
    Page<SupplierMaster> findByFilters(@Param("suppId") String suppId,
                                       @Param("suppName") String suppName,
                                       Pageable pageable);
}
