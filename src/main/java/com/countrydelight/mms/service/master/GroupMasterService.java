package com.countrydelight.mms.service.master;

import com.countrydelight.mms.dto.master.BulkUploadResult;
import com.countrydelight.mms.dto.master.BulkUploadResult.RowError;
import com.countrydelight.mms.dto.master.GroupRequest;
import com.countrydelight.mms.dto.master.SubGroupRequest;
import com.countrydelight.mms.entity.master.GroupMaster;
import com.countrydelight.mms.entity.master.SubGroupMaster;
import com.countrydelight.mms.entity.master.SubGroupMasterId;
import com.countrydelight.mms.exception.MmsException;
import com.countrydelight.mms.repository.master.GroupMasterRepository;
import com.countrydelight.mms.repository.master.SubGroupMasterRepository;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GroupMasterService {

    private final GroupMasterRepository groupMasterRepository;
    private final SubGroupMasterRepository subGroupMasterRepository;

    // ---- Group ----

    public Page<GroupMaster> getAllGroups(String groupId, String groupDesc, int page, int size) {
        return groupMasterRepository.findByFilters(
                StringUtils.hasText(groupId) ? groupId : null,
                StringUtils.hasText(groupDesc) ? groupDesc : null,
                PageRequest.of(page - 1, size, Sort.by("createdAt").descending())
        );
    }

    public GroupMaster getGroupById(String groupId) {
        return groupMasterRepository.findById(groupId)
                .orElseThrow(() -> new MmsException("Group not found: " + groupId));
    }

    @Transactional
    public GroupMaster createGroup(GroupRequest request) {
        if (groupMasterRepository.existsById(request.getGroupId())) {
            throw new MmsException("Group already exists: " + request.getGroupId());
        }
        GroupMaster group = GroupMaster.builder()
                .groupId(request.getGroupId())
                .groupDesc(request.getGroupDesc())
                .build();
        return groupMasterRepository.save(group);
    }

    @Transactional
    public GroupMaster updateGroup(String groupId, GroupRequest request) {
        GroupMaster existing = getGroupById(groupId);
        existing.setGroupDesc(request.getGroupDesc());
        return groupMasterRepository.save(existing);
    }

    // ---- SubGroup ----

    public Page<SubGroupMaster> getSubGroupsByGroup(String groupId, int page, int size) {
        return subGroupMasterRepository.findByGroupId(groupId,
                PageRequest.of(page - 1, size, Sort.by("createdAt").descending()));
    }

    public SubGroupMaster getSubGroupById(String groupId, String subGroupId) {
        SubGroupMasterId id = new SubGroupMasterId(groupId, subGroupId);
        return subGroupMasterRepository.findById(id)
                .orElseThrow(() -> new MmsException("SubGroup not found: " + groupId + "/" + subGroupId));
    }

    @Transactional
    public SubGroupMaster createSubGroup(String groupId, SubGroupRequest request) {
        // Validate group exists
        getGroupById(groupId);
        SubGroupMasterId id = new SubGroupMasterId(groupId, request.getSubGroupId());
        if (subGroupMasterRepository.existsById(id)) {
            throw new MmsException("SubGroup already exists: " + groupId + "/" + request.getSubGroupId());
        }
        SubGroupMaster subGroup = SubGroupMaster.builder()
                .groupId(groupId)
                .subGroupId(request.getSubGroupId())
                .subGroupDesc(request.getSubGroupDesc())
                .build();
        return subGroupMasterRepository.save(subGroup);
    }

    @Transactional
    public SubGroupMaster updateSubGroup(String groupId, String subGroupId, SubGroupRequest request) {
        SubGroupMaster existing = getSubGroupById(groupId, subGroupId);
        existing.setSubGroupDesc(request.getSubGroupDesc());
        return subGroupMasterRepository.save(existing);
    }

    // ---- Bulk Upload ----

    @Transactional
    public BulkUploadResult uploadGroupsFromExcel(MultipartFile file) {
        List<RowError> errors = new ArrayList<>();
        // Use LinkedHashMap to preserve insertion order (first occurrence wins for duplicates)
        Map<String, GroupMaster> groupsToSave = new LinkedHashMap<>();
        List<SubGroupMaster> subGroupsToSave = new ArrayList<>();
        // Track sub-group composite keys seen in file to detect duplicates
        Set<String> seenSubGroupKeys = new HashSet<>();
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
                int rowNum = row.getRowNum() + 1;

                String groupId      = readString(row.getCell(0));
                String groupDesc    = readString(row.getCell(1));
                String subGroupId   = readString(row.getCell(2));
                String subGroupDesc = readString(row.getCell(3));

                // Validate required group fields
                String error = null;
                if (!StringUtils.hasText(groupId)) {
                    error = "group_id is required";
                } else if (!StringUtils.hasText(groupDesc)) {
                    error = "group_desc is required";
                } else if (StringUtils.hasText(subGroupId) && !StringUtils.hasText(subGroupDesc)) {
                    error = "sub_group_desc is required when sub_group_id is provided";
                } else if (StringUtils.hasText(subGroupId)) {
                    String compositeKey = groupId + "||" + subGroupId;
                    if (seenSubGroupKeys.contains(compositeKey)) {
                        error = "Duplicate sub_group_id '" + subGroupId + "' for group_id '" + groupId + "' in file";
                    } else if (subGroupMasterRepository.existsById(new SubGroupMasterId(groupId, subGroupId))) {
                        error = "SubGroup already exists in database: " + groupId + "/" + subGroupId;
                    }
                }

                if (error != null) {
                    errors.add(RowError.builder()
                            .rowNumber(rowNum)
                            .entityId(StringUtils.hasText(groupId) ? groupId : null)
                            .reason(error)
                            .build());
                    continue;
                }

                // Collect group (first occurrence wins; skip if already in DB)
                if (!groupsToSave.containsKey(groupId) && !groupMasterRepository.existsById(groupId)) {
                    groupsToSave.put(groupId, GroupMaster.builder()
                            .groupId(groupId)
                            .groupDesc(groupDesc)
                            .build());
                }

                // Collect sub-group if provided
                if (StringUtils.hasText(subGroupId)) {
                    String compositeKey = groupId + "||" + subGroupId;
                    seenSubGroupKeys.add(compositeKey);
                    subGroupsToSave.add(SubGroupMaster.builder()
                            .groupId(groupId)
                            .subGroupId(subGroupId)
                            .subGroupDesc(subGroupDesc)
                            .build());
                }
            }
        } catch (IOException e) {
            throw new MmsException("Failed to read Excel file: " + e.getMessage());
        }

        // Save groups first (sub-groups have FK to group)
        groupMasterRepository.saveAll(groupsToSave.values());
        subGroupMasterRepository.saveAll(subGroupsToSave);

        int savedCount = groupsToSave.size() + subGroupsToSave.size();
        return BulkUploadResult.builder()
                .totalRows(dataRowCount)
                .savedCount(savedCount)
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
}
