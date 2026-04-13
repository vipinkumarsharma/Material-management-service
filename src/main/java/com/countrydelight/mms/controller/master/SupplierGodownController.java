package com.countrydelight.mms.controller.master;

import com.countrydelight.mms.dto.common.ApiResponse;
import com.countrydelight.mms.dto.master.GodownStockResponse;
import com.countrydelight.mms.service.master.GodownItemStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/master/supplier-godown")
@RequiredArgsConstructor
public class SupplierGodownController {

    private final GodownItemStockService godownItemStockService;

    @GetMapping("/stock")
    public ResponseEntity<ApiResponse<List<GodownStockResponse>>> getStockBySupplier(
            @RequestParam String suppId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<GodownStockResponse> result = godownItemStockService.getStockBySupplier(suppId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result.getNumber() + 1, result.getTotalElements()));
    }
}
