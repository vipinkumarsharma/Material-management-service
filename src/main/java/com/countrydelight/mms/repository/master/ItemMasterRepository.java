package com.countrydelight.mms.repository.master;

import com.countrydelight.mms.entity.master.ItemMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemMasterRepository extends JpaRepository<ItemMaster, String> {

    @Query("""
            SELECT i FROM ItemMaster i WHERE
            (:itemId IS NULL OR i.itemId LIKE CONCAT('%', :itemId, '%')) AND
            (:name IS NULL OR i.itemDesc LIKE CONCAT('%', :name, '%')) AND
            (:suppId IS NULL OR i.suppId = :suppId) AND
            (:companyId IS NULL OR i.companyId = :companyId)
            """)
    // Sub-group filtering temporarily disabled: groupId, subGroupId params removed
    Page<ItemMaster> findByFilters(@Param("itemId") String itemId,
                                   @Param("name") String name,
                                   @Param("suppId") String suppId,
                                   @Param("companyId") String companyId,
                                   Pageable pageable);
}
