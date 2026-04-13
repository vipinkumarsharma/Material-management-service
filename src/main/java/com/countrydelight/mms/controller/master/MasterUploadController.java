package com.countrydelight.mms.controller.master;

import com.countrydelight.mms.dto.common.ApiResponse;
import com.countrydelight.mms.dto.master.BulkUploadResult;
import com.countrydelight.mms.exception.MmsException;
import com.countrydelight.mms.service.master.BranchMasterService;
import com.countrydelight.mms.service.master.GroupMasterService;
import com.countrydelight.mms.service.master.ItemMasterService;
import com.countrydelight.mms.service.master.SupplierMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;

import java.util.Locale;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/master")
@RequiredArgsConstructor
public class MasterUploadController {

    private final ItemMasterService itemMasterService;
    private final BranchMasterService branchMasterService;
    private final SupplierMasterService supplierMasterService;
    private final GroupMasterService groupMasterService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BulkUploadResult>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type) {

        BulkUploadResult result = switch (type.toUpperCase(Locale.ROOT)) {
            case "ITEM"     -> itemMasterService.uploadItemsFromExcelGeneric(file);
            case "BRANCH"   -> branchMasterService.uploadBranchesFromExcel(file);
            case "SUPPLIER" -> supplierMasterService.uploadSuppliersFromExcel(file);
            case "GROUP"    -> groupMasterService.uploadGroupsFromExcel(file);
            default         -> throw new MmsException(
                    "Unknown upload type: " + type + ". Allowed values: ITEM, BRANCH, SUPPLIER, GROUP");
        };

        String message = result.getSavedCount() + " record(s) saved, "
                + result.getSkippedCount() + " skipped";
        return ResponseEntity.ok(ApiResponse.success(message, result));
    }
}
