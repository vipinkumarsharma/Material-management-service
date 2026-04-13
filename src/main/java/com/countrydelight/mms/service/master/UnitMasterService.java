package com.countrydelight.mms.service.master;

import com.countrydelight.mms.dto.master.UnitRequest;
import com.countrydelight.mms.entity.master.UnitMaster;
import com.countrydelight.mms.exception.MmsException;
import com.countrydelight.mms.repository.master.UnitMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UnitMasterService {

    private final UnitMasterRepository unitMasterRepository;

    public Page<UnitMaster> getAll(String desc, int page, int size) {
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        if (StringUtils.hasText(desc)) {
            return unitMasterRepository.findByUnitDescContainingIgnoreCase(desc, pageable);
        }
        return unitMasterRepository.findAll(pageable);
    }

    public UnitMaster getById(String unitId) {
        return unitMasterRepository.findById(unitId)
                .orElseThrow(() -> new MmsException("Unit not found: " + unitId));
    }

    @Transactional
    public UnitMaster create(UnitRequest request) {
        if (unitMasterRepository.existsById(request.getUnitId())) {
            throw new MmsException("Unit already exists: " + request.getUnitId());
        }
        UnitMaster unit = UnitMaster.builder()
                .unitId(request.getUnitId())
                .unitDesc(request.getUnitDesc())
                .build();
        return unitMasterRepository.save(unit);
    }

    @Transactional
    public UnitMaster update(String unitId, UnitRequest request) {
        UnitMaster existing = getById(unitId);
        existing.setUnitDesc(request.getUnitDesc());
        return unitMasterRepository.save(existing);
    }
}
