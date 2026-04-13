package com.countrydelight.mms.service.master;

import com.countrydelight.mms.dto.master.BulkUploadResult;
import com.countrydelight.mms.dto.master.BulkUploadResult.RowError;
import com.countrydelight.mms.dto.master.SupplierRequest;
import com.countrydelight.mms.entity.master.SupplierMaster;
import com.countrydelight.mms.exception.MmsException;
import com.countrydelight.mms.repository.master.SupplierMasterRepository;
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
public class SupplierMasterService {

    private final SupplierMasterRepository supplierMasterRepository;

    public Page<SupplierMaster> getAll(String suppId, String suppName, int page, int size) {
        return supplierMasterRepository.findByFilters(
                StringUtils.hasText(suppId) ? suppId : null,
                StringUtils.hasText(suppName) ? suppName : null,
                PageRequest.of(page - 1, size, Sort.by("createdAt").descending())
        );
    }

    public SupplierMaster getById(String suppId) {
        return supplierMasterRepository.findById(suppId)
                .orElseThrow(() -> new MmsException("Supplier not found: " + suppId));
    }

    @Transactional
    public SupplierMaster create(SupplierRequest request) {
        if (supplierMasterRepository.existsById(request.getSuppId())) {
            throw new MmsException("Supplier already exists: " + request.getSuppId());
        }
        SupplierMaster supplier = SupplierMaster.builder()
                .suppId(request.getSuppId())
                .suppName(request.getSuppName())
                .address(request.getAddress())
                .mobNo(request.getMobNo())
                .email(request.getEmail())
                .gstin(request.getGstin())
                .build();
        return supplierMasterRepository.save(supplier);
    }

    @Transactional
    public BulkUploadResult uploadSuppliersFromExcel(MultipartFile file) {
        List<RowError> errors = new ArrayList<>();
        List<SupplierMaster> saveList = new ArrayList<>();
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

                String suppId    = readString(row.getCell(0));
                String suppName  = readString(row.getCell(1));
                String address   = readString(row.getCell(2));
                String mobNo     = readString(row.getCell(3));
                String email     = readString(row.getCell(4));
                String gstin     = readString(row.getCell(5));
                String type      = readString(row.getCell(6));

                String error = null;
                if (!StringUtils.hasText(suppId)) {
                    error = "supp_id is required";
                } else if (!StringUtils.hasText(suppName)) {
                    error = "supp_name is required";
                } else if (!seenIds.add(suppId)) {
                    error = "Duplicate supp_id in file";
                } else if (supplierMasterRepository.existsById(suppId)) {
                    error = "Supplier already exists in database";
                }

                if (error != null) {
                    errors.add(RowError.builder()
                            .rowNumber(rowNum)
                            .entityId(StringUtils.hasText(suppId) ? suppId : null)
                            .reason(error)
                            .build());
                    continue;
                }

                saveList.add(SupplierMaster.builder()
                        .suppId(suppId)
                        .suppName(suppName)
                        .address(StringUtils.hasText(address) ? address : null)
                        .mobNo(StringUtils.hasText(mobNo) ? mobNo : null)
                        .email(StringUtils.hasText(email) ? email : null)
                        .gstin(StringUtils.hasText(gstin) ? gstin : null)
                        .type(StringUtils.hasText(type) ? type : null)
                        .build());
            }
        } catch (IOException e) {
            throw new MmsException("Failed to read Excel file: " + e.getMessage());
        }

        supplierMasterRepository.saveAll(saveList);

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
    public SupplierMaster update(String suppId, SupplierRequest request) {
        SupplierMaster existing = getById(suppId);
        existing.setSuppName(request.getSuppName());
        existing.setAddress(request.getAddress());
        existing.setMobNo(request.getMobNo());
        existing.setEmail(request.getEmail());
        existing.setGstin(request.getGstin());
        return supplierMasterRepository.save(existing);
    }
}
