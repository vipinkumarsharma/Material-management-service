package com.countrydelight.mms.service.master;

import com.countrydelight.mms.dto.master.RoleRequest;
import com.countrydelight.mms.dto.master.UserRoleRequest;
import com.countrydelight.mms.entity.master.RoleMaster;
import com.countrydelight.mms.entity.master.UserRoleMap;
import com.countrydelight.mms.entity.master.UserRoleMapId;
import com.countrydelight.mms.exception.MmsException;
import com.countrydelight.mms.repository.master.RoleMasterRepository;
import com.countrydelight.mms.repository.master.UserRoleMapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class RoleMasterService {

    private final RoleMasterRepository roleMasterRepository;
    private final UserRoleMapRepository userRoleMapRepository;

    // ---- Role ----

    public Page<RoleMaster> getAllRoles(String name, int page, int size) {
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        if (StringUtils.hasText(name)) {
            return roleMasterRepository.findByRoleNameContainingIgnoreCase(name, pageable);
        }
        return roleMasterRepository.findAll(pageable);
    }

    public RoleMaster getRoleById(String roleId) {
        return roleMasterRepository.findById(roleId)
                .orElseThrow(() -> new MmsException("Role not found: " + roleId));
    }

    @Transactional
    public RoleMaster createRole(RoleRequest request) {
        if (roleMasterRepository.existsById(request.getRoleId())) {
            throw new MmsException("Role already exists: " + request.getRoleId());
        }
        RoleMaster role = RoleMaster.builder()
                .roleId(request.getRoleId())
                .roleName(request.getRoleName())
                .build();
        return roleMasterRepository.save(role);
    }

    @Transactional
    public RoleMaster updateRole(String roleId, RoleRequest request) {
        RoleMaster existing = getRoleById(roleId);
        existing.setRoleName(request.getRoleName());
        return roleMasterRepository.save(existing);
    }

    // ---- UserRoleMap ----

    public Page<UserRoleMap> getUserRoles(String userId, int page, int size) {
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        if (StringUtils.hasText(userId)) {
            return userRoleMapRepository.findByUserId(userId, pageable);
        }
        return userRoleMapRepository.findAll(pageable);
    }

    @Transactional
    public UserRoleMap assignRole(UserRoleRequest request) {
        // Validate role exists
        getRoleById(request.getRoleId());
        if (userRoleMapRepository.existsByUserIdAndRoleId(request.getUserId(), request.getRoleId())) {
            throw new MmsException("User already has role: " + request.getUserId() + "/" + request.getRoleId());
        }
        UserRoleMap userRoleMap = UserRoleMap.builder()
                .userId(request.getUserId())
                .roleId(request.getRoleId())
                .build();
        return userRoleMapRepository.save(userRoleMap);
    }

    @Transactional
    public void removeRole(String userId, String roleId) {
        UserRoleMapId id = new UserRoleMapId(userId, roleId);
        if (!userRoleMapRepository.existsById(id)) {
            throw new MmsException("User role mapping not found: " + userId + "/" + roleId);
        }
        userRoleMapRepository.deleteById(id);
    }
}
