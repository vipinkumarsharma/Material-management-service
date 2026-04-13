package com.countrydelight.mms.controller;

import com.countrydelight.mms.dto.common.ApiResponse;
import com.countrydelight.mms.dto.stock.StockCorrectionRequest;
import com.countrydelight.mms.dto.stock.StockCorrectionResponse;
import com.countrydelight.mms.service.stock.StockCorrectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stock/corrections")
@RequiredArgsConstructor
public class StockCorrectionController {

    private final StockCorrectionService correctionService;

    @PostMapping
    public ResponseEntity<ApiResponse<StockCorrectionResponse>> correctEntry(
            @Valid @RequestBody StockCorrectionRequest request) {
        StockCorrectionResponse response = correctionService.correctEntry(request);
        return ResponseEntity.ok(ApiResponse.success("Stock correction applied successfully", response));
    }
}
