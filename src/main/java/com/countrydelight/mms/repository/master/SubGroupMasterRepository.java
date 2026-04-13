package com.countrydelight.mms.repository.master;

import com.countrydelight.mms.entity.master.SubGroupMaster;
import com.countrydelight.mms.entity.master.SubGroupMasterId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SubGroupMasterRepository extends JpaRepository<SubGroupMaster, SubGroupMasterId> {

    @Query("SELECT s FROM SubGroupMaster s WHERE s.groupId = :groupId")
    Page<SubGroupMaster> findByGroupId(@Param("groupId") String groupId, Pageable pageable);
}
