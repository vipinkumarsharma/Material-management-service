package com.countrydelight.mms.controller;

import com.countrydelight.mms.dto.common.ApiResponse;
import com.countrydelight.mms.dto.stock.LocationObRequest;
import com.countrydelight.mms.dto.stock.LocationObResponse;
import com.countrydelight.mms.dto.stock.OpeningBalanceRequest;
import com.countrydelight.mms.dto.stock.OpeningBalanceSummary;
import com.countrydelight.mms.service.stock.OpeningBalanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/stock/opening-balance")
@RequiredArgsConstructor
public class OpeningBalanceController {

    private final OpeningBalanceService openingBalanceService;

    @PostMapping
    public ResponseEntity<ApiResponse<OpeningBalanceSummary>> uploadOpeningBalance(
            @Valid @RequestBody OpeningBalanceRequest request) {
        OpeningBalanceSummary summary = openingBalanceService.uploadOpeningBalance(request);
        return ResponseEntity.ok(ApiResponse.success("Opening balance uploaded successfully", summary));
    }

    @PostMapping("/location")
    public ResponseEntity<ApiResponse<OpeningBalanceSummary>> uploadLocationOpeningBalance(
            @Valid @RequestBody LocationObRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                openingBalanceService.uploadLocationOpeningBalance(request)));
    }

    @GetMapping("/location")
    public ResponseEntity<ApiResponse<LocationObResponse>> getLocationOpeningBalance(
            @RequestParam String branchId,
            @RequestParam String locationId) {
        return ResponseEntity.ok(ApiResponse.success(
                openingBalanceService.getLocationOpeningBalance(branchId, locationId)));
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<OpeningBalanceSummary>> uploadOpeningBalanceCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam("cutoffDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate cutoffDate) {
        OpeningBalanceSummary summary = openingBalanceService.uploadOpeningBalanceCsv(file, cutoffDate);
        return ResponseEntity.ok(ApiResponse.success("Opening balance uploaded from CSV successfully", summary));
    }
}
