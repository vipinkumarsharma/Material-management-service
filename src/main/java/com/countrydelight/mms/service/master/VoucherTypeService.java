package com.countrydelight.mms.service.master;

import com.countrydelight.mms.dto.master.VoucherSeriesRequest;
import com.countrydelight.mms.dto.master.VoucherTypeRequest;
import com.countrydelight.mms.entity.master.VoucherSeriesMaster;
import com.countrydelight.mms.entity.master.VoucherTypeMaster;
import com.countrydelight.mms.exception.MmsException;
import com.countrydelight.mms.repository.master.VoucherSeriesMasterRepository;
import com.countrydelight.mms.repository.master.VoucherTypeMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VoucherTypeService {

    private final VoucherTypeMasterRepository voucherTypeRepository;
    private final VoucherSeriesMasterRepository voucherSeriesRepository;
    private final VoucherSeriesService voucherSeriesService;

    @Transactional
    public VoucherTypeMaster create(VoucherTypeRequest request) {
        if (request.getVoucherTypeId() == null || request.getVoucherTypeId().isBlank()) {
            request.setVoucherTypeId(UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        }
        if (voucherTypeRepository.existsById(request.getVoucherTypeId())) {
            throw new MmsException("Voucher type already exists: " + request.getVoucherTypeId());
        }
        VoucherTypeMaster vt = VoucherTypeMaster.builder()
                .voucherTypeId(request.getVoucherTypeId())
                .branchIds(request.getBranchIds() != null ? request.getBranchIds() : new java.util.ArrayList<>())
                .voucherTypeName(request.getVoucherTypeName())
                .alias(request.getAlias())
                .voucherCategory(request.getVoucherCategory())
                .abbreviation(request.getAbbreviation())
                .setAlterNumbering(request.getSetAlterNumbering())
                .defaultJurisdiction(request.getDefaultJurisdiction())
                .defaultTitleToPrint(request.getDefaultTitleToPrint())
                .setAlterDeclaration(request.isSetAlterDeclaration())
                .enableDefaultAllocations(request.isEnableDefaultAllocations())
                .whatsappAfterSaving(request.isWhatsappAfterSaving())
                .active(request.isActive())
                .numberingMethod(request.getNumberingMethod())
                .numberingOnDeletion(request.getNumberingOnDeletion())
                .showUnusedNumbers(request.isShowUnusedNumbers())
                .preventDuplicates(request.isPreventDuplicates())
                .allowZeroValued(request.isAllowZeroValued())
                .optionalDefault(request.isOptionalDefault())
                .useEffectiveDates(request.isUseEffectiveDates())
                .effectiveDateLabel(request.getEffectiveDateLabel())
                .allowNarration(request.isAllowNarration())
                .narrationMandatory(request.isNarrationMandatory())
                .narrationPerLine(request.isNarrationPerLine())
                .printAfterSave(request.isPrintAfterSave())
                .requireApproval(request.isRequireApproval())
                .approvalAmountLimit(request.getApprovalAmountLimit())
                .useForJobWork(request.isUseForJobWork())
                .useForJobWorkIn(request.isUseForJobWorkIn())
                .department(request.isDepartment())
                .supplier(request.isSupplier())
                .reportSummaryTitle(request.getReportSummaryTitle())
                .build();
        VoucherTypeMaster saved = voucherTypeRepository.save(vt);

        if (request.getVoucherSeries() != null && !request.getVoucherSeries().isEmpty()) {
            List<VoucherSeriesMaster> createdSeries = new ArrayList<>();
            for (VoucherSeriesRequest seriesReq : request.getVoucherSeries()) {
                seriesReq.setVoucherTypeId(saved.getVoucherTypeId());
                createdSeries.add(voucherSeriesService.create(seriesReq));
            }
            saved.setVoucherSeries(createdSeries);
        }

        return saved;
    }

    @Transactional
    public VoucherTypeMaster update(String id, VoucherTypeRequest request) {
        VoucherTypeMaster vt = voucherTypeRepository.findById(id)
                .orElseThrow(() -> new MmsException("Voucher type not found: " + id));
        vt.setBranchIds(request.getBranchIds() != null ? request.getBranchIds() : new java.util.ArrayList<>());
        vt.setVoucherTypeName(request.getVoucherTypeName());
        vt.setAlias(request.getAlias());
        vt.setVoucherCategory(request.getVoucherCategory());
        vt.setAbbreviation(request.getAbbreviation());
        vt.setSetAlterNumbering(request.getSetAlterNumbering());
        vt.setDefaultJurisdiction(request.getDefaultJurisdiction());
        vt.setDefaultTitleToPrint(request.getDefaultTitleToPrint());
        vt.setSetAlterDeclaration(request.isSetAlterDeclaration());
        vt.setEnableDefaultAllocations(request.isEnableDefaultAllocations());
        vt.setWhatsappAfterSaving(request.isWhatsappAfterSaving());
        vt.setActive(request.isActive());
        vt.setNumberingMethod(request.getNumberingMethod());
        vt.setNumberingOnDeletion(request.getNumberingOnDeletion());
        vt.setShowUnusedNumbers(request.isShowUnusedNumbers());
        vt.setPreventDuplicates(request.isPreventDuplicates());
        vt.setAllowZeroValued(request.isAllowZeroValued());
        vt.setOptionalDefault(request.isOptionalDefault());
        vt.setUseEffectiveDates(request.isUseEffectiveDates());
        vt.setEffectiveDateLabel(request.getEffectiveDateLabel());
        vt.setAllowNarration(request.isAllowNarration());
        vt.setNarrationMandatory(request.isNarrationMandatory());
        vt.setNarrationPerLine(request.isNarrationPerLine());
        vt.setPrintAfterSave(request.isPrintAfterSave());
        vt.setRequireApproval(request.isRequireApproval());
        vt.setApprovalAmountLimit(request.getApprovalAmountLimit());
        vt.setUseForJobWork(request.isUseForJobWork());
        vt.setUseForJobWorkIn(request.isUseForJobWorkIn());
        vt.setDepartment(request.isDepartment());
        vt.setSupplier(request.isSupplier());
        vt.setReportSummaryTitle(request.getReportSummaryTitle());
        VoucherTypeMaster saved = voucherTypeRepository.save(vt);

        if (request.getVoucherSeries() != null && !request.getVoucherSeries().isEmpty()) {
            List<VoucherSeriesMaster> existingSeries = voucherSeriesRepository.findByVoucherTypeId(id);
            for (VoucherSeriesRequest seriesReq : request.getVoucherSeries()) {
                seriesReq.setVoucherTypeId(id);
                if (seriesReq.getSeriesId() == null || seriesReq.getSeriesId().isBlank()) {
                    // No seriesId provided — update an existing series if one exists, otherwise create
                    if (!existingSeries.isEmpty()) {
                        voucherSeriesService.update(existingSeries.get(0).getSeriesId(), seriesReq);
                    } else {
                        voucherSeriesService.create(seriesReq);
                    }
                } else if (voucherSeriesRepository.existsById(seriesReq.getSeriesId())) {
                    voucherSeriesService.update(seriesReq.getSeriesId(), seriesReq);
                } else {
                    voucherSeriesService.create(seriesReq);
                }
            }
        }

        populateSeries(saved);
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<VoucherTypeMaster> list(String voucherCategory, String branchId, int page, int size) {
        boolean hasBranch = branchId != null && !branchId.isBlank();
        boolean hasType = voucherCategory != null && !voucherCategory.isBlank();
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by("voucherTypeId"));
        Page<VoucherTypeMaster> result;
        if (hasBranch && hasType) {
            result = voucherTypeRepository.findByBranchIdAndVoucherCategory(branchId, voucherCategory, pageable);
        } else if (hasBranch) {
            result = voucherTypeRepository.findByBranchId(branchId, pageable);
        } else if (hasType) {
            result = voucherTypeRepository.findByVoucherCategory(voucherCategory, pageable);
        } else {
            result = voucherTypeRepository.findAll(pageable);
        }
        result.forEach(this::populateSeries);
        return result;
    }

    @Transactional(readOnly = true)
    public VoucherTypeMaster getById(String id) {
        VoucherTypeMaster vt = voucherTypeRepository.findById(id)
                .orElseThrow(() -> new MmsException("Voucher type not found: " + id));
        populateSeries(vt);
        return vt;
    }

    private void populateSeries(VoucherTypeMaster vt) {
        List<VoucherSeriesMaster> series = voucherSeriesRepository.findByVoucherTypeId(vt.getVoucherTypeId());
        vt.setVoucherSeries(series.isEmpty() ? null : series);
    }
}
