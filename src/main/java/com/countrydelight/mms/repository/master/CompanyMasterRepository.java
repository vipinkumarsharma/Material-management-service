package com.countrydelight.mms.repository.master;

import com.countrydelight.mms.entity.master.CompanyMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyMasterRepository extends JpaRepository<CompanyMaster, String> {

    @Query("""
            SELECT c FROM CompanyMaster c WHERE
            (:companyId IS NULL OR c.companyId LIKE CONCAT('%', :companyId, '%')) AND
            (:name IS NULL OR c.companyName LIKE CONCAT('%', :name, '%'))
            """)
    Page<CompanyMaster> findByFilters(@Param("companyId") String companyId,
                                      @Param("name") String name,
                                      Pageable pageable);
}
