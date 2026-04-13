package com.countrydelight.mms.controller.master;

import com.countrydelight.mms.dto.common.ApiResponse;
import com.countrydelight.mms.dto.master.BranchItemPriceRequest;
import com.countrydelight.mms.dto.master.ItemBulkUploadResult;
import com.countrydelight.mms.dto.master.ItemRequest;
import com.countrydelight.mms.entity.master.BranchItemPrice;
import com.countrydelight.mms.entity.master.ItemMaster;
import com.countrydelight.mms.service.master.BranchItemPriceService;
import com.countrydelight.mms.service.master.ItemMasterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/master/items")
@RequiredArgsConstructor
@Tag(name = "Item Master", description = "Item CRUD and branch-level pricing APIs")
public class ItemMasterController {

    private final ItemMasterService itemMasterService;
    private final BranchItemPriceService branchItemPriceService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ItemMaster>>> getAll(
            @RequestParam(required = false) String itemId,
            @RequestParam(required = false) String name,
            // Sub-group filtering temporarily disabled
            // @RequestParam(required = false) String groupId,
            // @RequestParam(required = false) String subGroupId,
            @RequestParam(required = false) String suppId,
            @RequestParam(required = false) String companyId,
            @RequestParam(required = false) String branchId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ItemMaster> result = itemMasterService.getAll(itemId, name, suppId, companyId, branchId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result.getNumber() + 1, result.getTotalElements()));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ApiResponse<ItemMaster>> getById(@PathVariable String itemId) {
        ItemMaster item = itemMasterService.getById(itemId);
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ItemMaster>> create(@Valid @RequestBody ItemRequest request) {
        ItemMaster created = itemMasterService.create(request);
        return ResponseEntity.ok(ApiResponse.success("Item created successfully", created));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ItemBulkUploadResult>> uploadItems(
            @RequestParam("file") MultipartFile file) {
        ItemBulkUploadResult result = itemMasterService.uploadItemsFromExcel(file);
        String message = result.getSavedCount() + " item(s) saved, "
                + result.getSkippedCount() + " skipped";
        return ResponseEntity.ok(ApiResponse.success(message, result));
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<ApiResponse<ItemMaster>> update(
            @PathVariable String itemId,
            @Valid @RequestBody ItemRequest request) {
        ItemMaster updated = itemMasterService.update(itemId, request);
        return ResponseEntity.ok(ApiResponse.success("Item updated successfully", updated));
    }

    // ---- Branch-Level Pricing ----

    @GetMapping("/{itemId}/branch-prices")
    @Operation(summary = "List branch prices for item",
            description = "Returns branch-level price overrides for an item. Optionally filter by branchId.")
    public ResponseEntity<ApiResponse<List<BranchItemPrice>>> getBranchPrices(
            @PathVariable String itemId,
            @RequestParam(required = false) String branchId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<BranchItemPrice> result = branchItemPriceService.getBranchPricesForItem(itemId, branchId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result.getNumber() + 1, result.getTotalElements()));
    }

    @PutMapping("/{itemId}/branch-prices/{branchId}")
    @Operation(summary = "Set branch price for item",
            description = "Creates or updates the branch-level cost_price and mrp override for an item.")
    public ResponseEntity<ApiResponse<BranchItemPrice>> setBranchPrice(
            @PathVariable String itemId,
            @PathVariable String branchId,
            @Valid @RequestBody BranchItemPriceRequest request) {
        BranchItemPrice price = branchItemPriceService.setBranchPrice(branchId, itemId, request);
        return ResponseEntity.ok(ApiResponse.success("Branch price set successfully", price));
    }

    @DeleteMapping("/{itemId}/branch-prices/{branchId}")
    @Operation(summary = "Remove branch price override",
            description = "Removes the branch-level price override. Item will fall back to item_master defaults.")
    public ResponseEntity<ApiResponse<Void>> removeBranchPrice(
            @PathVariable String itemId,
            @PathVariable String branchId) {
        branchItemPriceService.removeBranchPrice(branchId, itemId);
        return ResponseEntity.ok(ApiResponse.success("Branch price removed successfully", null));
    }
}
