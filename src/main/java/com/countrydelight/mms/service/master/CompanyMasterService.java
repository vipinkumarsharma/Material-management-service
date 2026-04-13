package com.countrydelight.mms.service.master;

import com.countrydelight.mms.dto.master.CompanyRequest;
import com.countrydelight.mms.entity.master.CompanyMaster;
import com.countrydelight.mms.exception.MmsException;
import com.countrydelight.mms.repository.master.CompanyMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CompanyMasterService {

    private final CompanyMasterRepository companyMasterRepository;

    public Page<CompanyMaster> getAll(String companyId, String name, int page, int size) {
        return companyMasterRepository.findByFilters(
                StringUtils.hasText(companyId) ? companyId : null,
                StringUtils.hasText(name) ? name : null,
                PageRequest.of(page - 1, size)
        );
    }

    public CompanyMaster getById(String companyId) {
        return companyMasterRepository.findById(companyId)
                .orElseThrow(() -> new MmsException("Company not found: " + companyId));
    }

    @Transactional
    public CompanyMaster create(CompanyRequest request) {
        if (companyMasterRepository.existsById(request.getCompanyId())) {
            throw new MmsException("Company already exists: " + request.getCompanyId());
        }
        CompanyMaster company = CompanyMaster.builder()
                .companyId(request.getCompanyId())
                .companyName(request.getCompanyName())
                .build();
        return companyMasterRepository.save(company);
    }

    @Transactional
    public CompanyMaster update(String companyId, CompanyRequest request) {
        CompanyMaster existing = getById(companyId);
        existing.setCompanyName(request.getCompanyName());
        return companyMasterRepository.save(existing);
    }
}
