package com.countrydelight.mms.repository.master;

import com.countrydelight.mms.entity.master.GroupMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupMasterRepository extends JpaRepository<GroupMaster, String> {

    @Query("""
            SELECT g FROM GroupMaster g WHERE
            (:groupId IS NULL OR g.groupId LIKE CONCAT('%', :groupId, '%')) AND
            (:groupDesc IS NULL OR g.groupDesc LIKE CONCAT('%', :groupDesc, '%'))
            """)
    Page<GroupMaster> findByFilters(@Param("groupId") String groupId,
                                    @Param("groupDesc") String groupDesc,
                                    Pageable pageable);
}
