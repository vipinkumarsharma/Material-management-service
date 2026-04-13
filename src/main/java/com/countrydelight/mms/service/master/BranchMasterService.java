package com.countrydelight.mms.service.master;

import com.countrydelight.mms.dto.master.BranchRequest;
import com.countrydelight.mms.dto.master.BulkUploadResult;
import com.countrydelight.mms.dto.master.BulkUploadResult.RowError;
import com.countrydelight.mms.entity.master.BranchMaster;
import com.countrydelight.mms.exception.MmsException;
import com.countrydelight.mms.repository.master.BranchMasterRepository;
import com.countrydelight.mms.repository.master.CompanyMasterRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BranchMasterService {

    private final BranchMasterRepository branchMasterRepository;
    private final CompanyMasterRepository companyMasterRepository;

    public Page<BranchMaster> getAll(String branchId, String name, String pincode, String companyId, int page, int size) {
        return branchMasterRepository.findByFilters(
                StringUtils.hasText(branchId) ? branchId : null,
                StringUtils.hasText(name) ? name : null,
                StringUtils.hasText(pincode) ? pincode : null,
                StringUtils.hasText(companyId) ? companyId : null,
                PageRequest.of(page - 1, size, Sort.by("createdAt").descending())
        );
    }

    public BranchMaster getById(String branchId) {
        return branchMasterRepository.findById(branchId)
                .orElseThrow(() -> new MmsException("Branch not found: " + branchId));
    }

    @Transactional
    public BranchMaster create(BranchRequest request) {
        if (branchMasterRepository.existsById(request.getBranchId())) {
            throw new MmsException("Branch already exists: " + request.getBranchId());
        }
        if (StringUtils.hasText(request.getCompanyId()) && !companyMasterRepository.existsById(request.getCompanyId())) {
            throw new MmsException("Company not found: " + request.getCompanyId());
        }
        BranchMaster branch = BranchMaster.builder()
                .branchId(request.getBranchId())
                .branchName(request.getBranchName())
                .branchCode(request.getBranchCode())
                .address1(request.getAddress1())
                .gstNo(request.getGstNo())
                .pincode(request.getPincode())
                .companyId(request.getCompanyId())
                .build();
        return branchMasterRepository.save(branch);
    }

    @Transactional
    public BulkUploadResult uploadBranchesFromExcel(MultipartFile file) {
        List<RowError> errors = new ArrayList<>();
        List<BranchMaster> saveList = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();
        int dataRowCount = 0;

        try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue;
                }
                if (isBlankRow(row)) {
                    continue;
                }
                dataRowCount++;
                int rowNum = row.getRowNum() + 1;

                String branchId   = readString(row.getCell(0));
                String branchName = readString(row.getCell(1));
                String address1   = readString(row.getCell(2));
                String gstNo      = readString(row.getCell(3));
                String pincode    = readString(row.getCell(4));
                String branchCode = readString(row.getCell(5));
                String companyId  = readString(row.getCell(6));

                String error = null;
                if (!StringUtils.hasText(branchId)) {
                    error = "branch_id is required";
                } else if (!StringUtils.hasText(branchName)) {
                    error = "branch_name is required";
                } else if (!seenIds.add(branchId)) {
                    error = "Duplicate branch_id in file";
                } else if (branchMasterRepository.existsById(branchId)) {
                    error = "Branch already exists in database";
                } else if (StringUtils.hasText(companyId) && !companyMasterRepository.existsById(companyId)) {
                    error = "company_id not found";
                }

                if (error != null) {
                    errors.add(RowError.builder()
                            .rowNumber(rowNum)
                            .entityId(StringUtils.hasText(branchId) ? branchId : null)
                            .reason(error)
                            .build());
                    continue;
                }

                saveList.add(BranchMaster.builder()
                        .branchId(branchId)
                        .branchName(branchName)
                        .address1(StringUtils.hasText(address1) ? address1 : null)
                        .gstNo(StringUtils.hasText(gstNo) ? gstNo : null)
                        .pincode(StringUtils.hasText(pincode) ? pincode : null)
                        .branchCode(StringUtils.hasText(branchCode) ? branchCode : null)
                        .companyId(StringUtils.hasText(companyId) ? companyId : null)
                        .build());
            }
        } catch (IOException e) {
            throw new MmsException("Failed to read Excel file: " + e.getMessage());
        }

        branchMasterRepository.saveAll(saveList);

        return BulkUploadResult.builder()
                .totalRows(dataRowCount)
                .savedCount(saveList.size())
                .skippedCount(errors.size())
                .errors(errors)
                .build();
    }

    private boolean isBlankRow(Row row) {
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK
                    && StringUtils.hasText(cell.toString())) {
                return false;
            }
        }
        return true;
    }

    private String readString(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return "";
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            double value = cell.getNumericCellValue();
            if (value == Math.floor(value)) {
                return String.valueOf((long) value);
            }
            return BigDecimal.valueOf(value).toPlainString();
        }
        return cell.getStringCellValue().trim();
    }

    @Transactional
    public BranchMaster update(String branchId, BranchRequest request) {
        BranchMaster existing = getById(branchId);
        if (StringUtils.hasText(request.getCompanyId()) && !companyMasterRepository.existsById(request.getCompanyId())) {
            throw new MmsException("Company not found: " + request.getCompanyId());
        }
        existing.setBranchName(request.getBranchName());
        existing.setBranchCode(request.getBranchCode());
        existing.setAddress1(request.getAddress1());
        existing.setGstNo(request.getGstNo());
        existing.setPincode(request.getPincode());
        existing.setCompanyId(request.getCompanyId());
        return branchMasterRepository.save(existing);
    }
}
