package com.countrydelight.mms.service.master;

import com.countrydelight.mms.dto.master.LocationRequest;
import com.countrydelight.mms.entity.master.LocationMaster;
import com.countrydelight.mms.exception.MmsException;
import com.countrydelight.mms.repository.master.LocationMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class LocationMasterService {

    private final LocationMasterRepository locationMasterRepository;

    public Page<LocationMaster> getAll(String branchId, String parentId, int page, int size) {
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        if (StringUtils.hasText(branchId)) {
            return locationMasterRepository.findByBranchId(branchId, pageable);
        }
        if (StringUtils.hasText(parentId)) {
            return locationMasterRepository.findByParentId(parentId, pageable);
        }
        return locationMasterRepository.findAll(pageable);
    }

    public LocationMaster getById(String locationId) {
        return locationMasterRepository.findById(locationId)
                .orElseThrow(() -> new MmsException("Location not found: " + locationId));
    }

    @Transactional
    public LocationMaster create(LocationRequest request) {
        if (locationMasterRepository.existsById(request.getLocationId())) {
            throw new MmsException("Location already exists: " + request.getLocationId());
        }
        LocationMaster location = LocationMaster.builder()
                .locationId(request.getLocationId())
                .branchId(request.getBranchId())
                .locationName(request.getLocationName())
                .parentId(request.getParentId())
                .build();
        return locationMasterRepository.save(location);
    }

    @Transactional
    public LocationMaster update(String locationId, LocationRequest request) {
        LocationMaster existing = getById(locationId);
        existing.setBranchId(request.getBranchId());
        existing.setLocationName(request.getLocationName());
        existing.setParentId(request.getParentId());
        return locationMasterRepository.save(existing);
    }
}
