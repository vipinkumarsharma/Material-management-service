package com.countrydelight.mms.repository.master;

import com.countrydelight.mms.entity.master.UnitMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UnitMasterRepository extends JpaRepository<UnitMaster, String> {

    @Query("SELECT u FROM UnitMaster u WHERE LOWER(u.unitDesc) LIKE LOWER(CONCAT('%', :desc, '%'))")
    Page<UnitMaster> findByUnitDescContainingIgnoreCase(@Param("desc") String desc, Pageable pageable);
}
