package com.countrydelight.mms.repository.master;

import com.countrydelight.mms.entity.master.UserRoleMap;
import com.countrydelight.mms.entity.master.UserRoleMapId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface UserRoleMapRepository extends JpaRepository<UserRoleMap, UserRoleMapId> {

    @Query("SELECT u FROM UserRoleMap u WHERE u.userId = :userId")
    Page<UserRoleMap> findByUserId(@Param("userId") String userId, Pageable pageable);

    @Query("""
            SELECT urm.roleId FROM UserRoleMap urm
            WHERE urm.userId = :userId
            """)
    Set<String> findRoleIdsByUserId(@Param("userId") String userId);

    boolean existsByUserIdAndRoleId(String userId, String roleId);
}
