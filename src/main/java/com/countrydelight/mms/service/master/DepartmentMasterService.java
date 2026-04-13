package com.countrydelight.mms.service.master;

import com.countrydelight.mms.dto.master.BranchDepartmentRequest;
import com.countrydelight.mms.dto.master.DepartmentRequest;
import com.countrydelight.mms.dto.master.DepartmentResponse;
import com.countrydelight.mms.entity.master.BranchDepartmentMap;
import com.countrydelight.mms.entity.master.BranchDepartmentMapId;
import com.countrydelight.mms.entity.master.BranchMaster;
import com.countrydelight.mms.entity.master.DepartmentMaster;
import com.countrydelight.mms.exception.MmsException;
import com.countrydelight.mms.repository.master.BranchDepartmentMapRepository;
import com.countrydelight.mms.repository.master.BranchMasterRepository;
import com.countrydelight.mms.repository.master.DepartmentMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentMasterService {

    private final DepartmentMasterRepository departmentRepository;
    private final BranchDepartmentMapRepository branchDeptMapRepository;
    private final BranchMasterRepository branchMasterRepository;

    // ---- Department CRUD ----

    private DepartmentResponse toResponse(DepartmentMaster dept) {
        List<BranchDepartmentMap> mappings = branchDeptMapRepository.findByDeptId(dept.getDeptId());
        List<DepartmentResponse.BranchInfo> branches = mappings.stream()
                .map(m -> {
                    BranchMaster branch = branchMasterRepository.findById(m.getBranchId()).orElse(null);
                    return DepartmentResponse.BranchInfo.builder()
                            .branchId(m.getBranchId())
                            .branchName(branch != null ? branch.getBranchName() : null)
                            .build();
                })
                .collect(Collectors.toList());

        return DepartmentResponse.builder()
                .deptId(dept.getDeptId())
                .deptName(dept.getDeptName())
                .branches(branches)
                .createdAt(dept.getCreatedAt())
                .updatedAt(dept.getUpdatedAt())
                .build();
    }

    public Page<DepartmentResponse> getAllDepartments(String name, String branchId, Integer deptId,
                                                      int page, int size) {
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<DepartmentMaster> departments;
        if (StringUtils.hasText(branchId)) {
            List<Integer> deptIds = branchDeptMapRepository.findByBranchId(branchId).stream()
                    .map(BranchDepartmentMap::getDeptId)
                    .collect(Collectors.toList());
            departments = deptIds.isEmpty()
                    ? new PageImpl<>(List.of(), pageable, 0)
                    : departmentRepository.findByDeptIdIn(deptIds, pageable);
        } else if (deptId != null) {
            departments = departmentRepository.findById(deptId)
                    .map(d -> (Page<DepartmentMaster>) new PageImpl<>(List.of(d), pageable, 1))
                    .orElse(new PageImpl<>(List.of(), pageable, 0));
        } else if (StringUtils.hasText(name)) {
            departments = departmentRepository.findByDeptNameContainingIgnoreCase(name, pageable);
        } else {
            departments = departmentRepository.findAll(pageable);
        }
        List<DepartmentResponse> content = departments.getContent().stream()
                .map(this::toResponse).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, departments.getTotalElements());
    }

    public DepartmentResponse getDepartmentById(Integer deptId) {
        return toResponse(findDeptEntityById(deptId));
    }

    private DepartmentMaster findDeptEntityById(Integer deptId) {
        return departmentRepository.findById(deptId)
                .orElseThrow(() -> new MmsException("Department not found: " + deptId));
    }

    @Transactional
    public DepartmentMaster createDepartment(DepartmentRequest request) {
        DepartmentMaster dept = DepartmentMaster.builder()
                .deptName(request.getDeptName())
                .build();
        return departmentRepository.save(dept);
    }

    @Transactional
    public DepartmentMaster updateDepartment(Integer deptId, DepartmentRequest request) {
        DepartmentMaster existing = findDeptEntityById(deptId);
        existing.setDeptName(request.getDeptName());
        return departmentRepository.save(existing);
    }

    // ---- Branch-Department Mapping ----

    public Page<BranchDepartmentMap> getBranchDeptMappings(String branchId, Integer deptId,
                                                           int page, int size) {
        PageRequest pageable = PageRequest.of(page - 1, size);
        if (StringUtils.hasText(branchId)) {
            return branchDeptMapRepository.findByBranchId(branchId, pageable);
        }
        if (deptId != null) {
            return branchDeptMapRepository.findByDeptId(deptId, pageable);
        }
        return branchDeptMapRepository.findAll(pageable);
    }

    @Transactional
    public BranchDepartmentMap mapDeptToBranch(BranchDepartmentRequest request) {
        // Validate branch exists
        if (!branchMasterRepository.existsById(request.getBranchId())) {
            throw new MmsException("Branch not found: " + request.getBranchId());
        }
        // Validate dept exists
        findDeptEntityById(request.getDeptId());

        if (branchDeptMapRepository.existsByBranchIdAndDeptId(request.getBranchId(), request.getDeptId())) {
            throw new MmsException("Department already mapped to branch: " + request.getBranchId() + "/" + request.getDeptId());
        }

        BranchDepartmentMap mapping = BranchDepartmentMap.builder()
                .branchId(request.getBranchId())
                .deptId(request.getDeptId())
                .build();
        return branchDeptMapRepository.save(mapping);
    }

    @Transactional
    public void unmapDeptFromBranch(String branchId, Integer deptId) {
        BranchDepartmentMapId id = new BranchDepartmentMapId(branchId, deptId);
        if (!branchDeptMapRepository.existsById(id)) {
            throw new MmsException("Branch-department mapping not found: " + branchId + "/" + deptId);
        }
        branchDeptMapRepository.deleteById(id);
    }
}
