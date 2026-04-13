package com.countrydelight.mms.service.master;

import com.countrydelight.mms.dto.master.BulkUploadResult;
import com.countrydelight.mms.dto.master.ItemBulkUploadResult;
import com.countrydelight.mms.dto.master.ItemBulkUploadResult.RowError;
import com.countrydelight.mms.dto.master.ItemRequest;
import com.countrydelight.mms.entity.master.BranchItemPrice;
import com.countrydelight.mms.entity.master.ItemMaster;
import com.countrydelight.mms.exception.MmsException;
import com.countrydelight.mms.entity.master.SubGroupMasterId;
import com.countrydelight.mms.repository.master.BranchItemPriceRepository;
import com.countrydelight.mms.repository.master.CompanyMasterRepository;
import com.countrydelight.mms.repository.master.GroupMasterRepository;
import com.countrydelight.mms.repository.master.ItemMasterRepository;
import com.countrydelight.mms.repository.master.SubGroupMasterRepository;
import com.countrydelight.mms.repository.master.SupplierMasterRepository;
import com.countrydelight.mms.repository.master.UnitMasterRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemMasterService {

    private final ItemMasterRepository itemMasterRepository;
    private final CompanyMasterRepository companyMasterRepository;
    private final UnitMasterRepository unitMasterRepository;
    private final SupplierMasterRepository supplierMasterRepository;
    private final BranchItemPriceRepository branchItemPriceRepository;
    private final GroupMasterRepository groupMasterRepository;
    private final SubGroupMasterRepository subGroupMasterRepository;

    public Page<ItemMaster> getAll(String itemId, String name, String suppId, String companyId, String branchId,
                                   int page, int size) {
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<ItemMaster> itemsPage = itemMasterRepository.findByFilters(
                StringUtils.hasText(itemId) ? itemId : null,
                StringUtils.hasText(name) ? name : null,
                // Sub-group filtering temporarily disabled
                // StringUtils.hasText(groupId) ? groupId : null,
                // StringUtils.hasText(subGroupId) ? subGroupId : null,
                StringUtils.hasText(suppId) ? suppId : null,
                StringUtils.hasText(companyId) ? companyId : null,
                pageable
        );

        // Override costPrice with branch-specific price when branchId is provided
        if (StringUtils.hasText(branchId)) {
            Map<String, BranchItemPrice> branchPriceMap = branchItemPriceRepository.findByBranchId(branchId)
                    .stream()
                    .collect(Collectors.toMap(BranchItemPrice::getItemId, Function.identity()));

            List<ItemMaster> items = new ArrayList<>(itemsPage.getContent());
            for (ItemMaster item : items) {
                BranchItemPrice branchPrice = branchPriceMap.get(item.getItemId());
                if (branchPrice != null) {
                    if (branchPrice.getCostPrice() != null) {
                        item.setCostPrice(branchPrice.getCostPrice());
                    }
                    if (branchPrice.getMrp() != null) {
                        item.setMrp(branchPrice.getMrp());
                    }
                }
            }
            return new PageImpl<>(items, pageable, itemsPage.getTotalElements());
        }

        return itemsPage;
    }

    public ItemMaster getById(String itemId) {
        return itemMasterRepository.findById(itemId)
                .orElseThrow(() -> new MmsException("Item not found: " + itemId));
    }

    @Transactional
    public ItemMaster create(ItemRequest request) {
        if (itemMasterRepository.existsById(request.getItemId())) {
            throw new MmsException("Item already exists: " + request.getItemId());
        }
        if (StringUtils.hasText(request.getCompanyId()) && !companyMasterRepository.existsById(request.getCompanyId())) {
            throw new MmsException("Company not found: " + request.getCompanyId());
        }
        ItemMaster item = ItemMaster.builder()
                .itemId(request.getItemId())
                .itemDesc(request.getItemDesc())
                .groupId(request.getGroupId())
                .subGroupId(request.getSubGroupId())
                .suppId(request.getSuppId())
                .unitId(request.getUnitId())
                .gstPerc(request.getGstPerc())
                .costPrice(request.getCostPrice())
                .mrp(request.getMrp())
                .hsnCode(request.getHsnCode())
                .cessPerc(request.getCessPerc())
                .companyId(request.getCompanyId())
                .build();
        return itemMasterRepository.save(item);
    }

    @Transactional
    public ItemBulkUploadResult uploadItemsFromExcel(MultipartFile file) {
        List<RowError> errors = new ArrayList<>();
        List<ItemMaster> saveList = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();
        int dataRowCount = 0;

        try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue; // skip header
                }
                if (isBlankRow(row)) {
                    continue;
                }
                dataRowCount++;
                int rowNum = row.getRowNum() + 1; // 1-based for user display

                String itemId = readString(row.getCell(0));
                String itemDesc = readString(row.getCell(1));
                String unitId = readString(row.getCell(2));
                String suppId = readString(row.getCell(3));
                String gstPercStr = readString(row.getCell(4));
                String costPriceStr = readString(row.getCell(5));
                String mrpStr = readString(row.getCell(6));
                String hsnCode = readString(row.getCell(7));
                String cessPercStr = readString(row.getCell(8));
                String companyId = readString(row.getCell(9));
                String groupId = readString(row.getCell(10));
                String subGroupId = readString(row.getCell(11));

                String error = null;
                if (!StringUtils.hasText(itemId)) {
                    error = "item_id is required";
                } else if (!StringUtils.hasText(itemDesc)) {
                    error = "item_desc is required";
                } else if (!StringUtils.hasText(unitId)) {
                    error = "unit_id is required";
                } else if (!seenIds.add(itemId)) {
                    error = "Duplicate item_id in file";
                } else if (itemMasterRepository.existsById(itemId)) {
                    error = "Item already exists in database";
                } else if (!unitMasterRepository.existsById(unitId)) {
                    error = "unit_id not found";
                } else if (StringUtils.hasText(suppId) && !supplierMasterRepository.existsById(suppId)) {
                    error = "supp_id not found";
                } else if (StringUtils.hasText(companyId) && !companyMasterRepository.existsById(companyId)) {
                    error = "company_id not found";
                } else if (StringUtils.hasText(groupId) && !groupMasterRepository.existsById(groupId)) {
                    error = "group_id not found";
                } else if (StringUtils.hasText(subGroupId) && StringUtils.hasText(groupId)
                        && !subGroupMasterRepository.existsById(new SubGroupMasterId(groupId, subGroupId))) {
                    error = "sub_group_id not found for given group_id";
                }

                if (error != null) {
                    errors.add(RowError.builder()
                            .rowNumber(rowNum)
                            .itemId(StringUtils.hasText(itemId) ? itemId : null)
                            .reason(error)
                            .build());
                    continue;
                }

                saveList.add(ItemMaster.builder()
                        .itemId(itemId)
                        .itemDesc(itemDesc)
                        .unitId(unitId)
                        .suppId(StringUtils.hasText(suppId) ? suppId : null)
                        .gstPerc(StringUtils.hasText(gstPercStr) ? new BigDecimal(gstPercStr) : BigDecimal.ZERO)
                        .costPrice(StringUtils.hasText(costPriceStr) ? new BigDecimal(costPriceStr) : BigDecimal.ZERO)
                        .mrp(StringUtils.hasText(mrpStr) ? new BigDecimal(mrpStr) : BigDecimal.ZERO)
                        .hsnCode(StringUtils.hasText(hsnCode) ? hsnCode : null)
                        .cessPerc(StringUtils.hasText(cessPercStr) ? new BigDecimal(cessPercStr) : BigDecimal.ZERO)
                        .companyId(StringUtils.hasText(companyId) ? companyId : null)
                        .groupId(StringUtils.hasText(groupId) ? groupId : null)
                        .subGroupId(StringUtils.hasText(subGroupId) ? subGroupId : null)
                        .build());
            }
        } catch (IOException e) {
            throw new MmsException("Failed to read Excel file: " + e.getMessage());
        }

        itemMasterRepository.saveAll(saveList);

        return ItemBulkUploadResult.builder()
                .totalRows(dataRowCount)
                .savedCount(saveList.size())
                .skippedCount(errors.size())
                .errors(errors)
                .build();
    }

    @Transactional
    public BulkUploadResult uploadItemsFromExcelGeneric(MultipartFile file) {
        ItemBulkUploadResult result = uploadItemsFromExcel(file);
        List<BulkUploadResult.RowError> genericErrors = result.getErrors().stream()
                .map(e -> BulkUploadResult.RowError.builder()
                        .rowNumber(e.getRowNumber())
                        .entityId(e.getItemId())
                        .reason(e.getReason())
                        .build())
                .toList();
        return BulkUploadResult.builder()
                .totalRows(result.getTotalRows())
                .savedCount(result.getSavedCount())
                .skippedCount(result.getSkippedCount())
                .errors(genericErrors)
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
    public ItemMaster update(String itemId, ItemRequest request) {
        ItemMaster existing = getById(itemId);
        if (StringUtils.hasText(request.getCompanyId()) && !companyMasterRepository.existsById(request.getCompanyId())) {
            throw new MmsException("Company not found: " + request.getCompanyId());
        }
        existing.setItemDesc(request.getItemDesc());
        existing.setGroupId(request.getGroupId());
        existing.setSubGroupId(request.getSubGroupId());
        existing.setSuppId(request.getSuppId());
        existing.setUnitId(request.getUnitId());
        existing.setGstPerc(request.getGstPerc());
        existing.setCostPrice(request.getCostPrice());
        existing.setMrp(request.getMrp());
        existing.setHsnCode(request.getHsnCode());
        existing.setCessPerc(request.getCessPerc());
        existing.setCompanyId(request.getCompanyId());
        return itemMasterRepository.save(existing);
    }
}
