package com.countrydelight.mms.controller.master;

import com.countrydelight.mms.dto.common.ApiResponse;
import com.countrydelight.mms.dto.master.VoucherSeriesRequest;
import com.countrydelight.mms.dto.master.VoucherSeriesResponse;
import com.countrydelight.mms.entity.master.VoucherSeriesMaster;
import com.countrydelight.mms.entity.master.VoucherSeriesRestartSchedule;
import com.countrydelight.mms.service.master.VoucherNumberService;
import com.countrydelight.mms.service.master.VoucherSeriesService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/master/voucher-series")
@RequiredArgsConstructor
public class VoucherSeriesController {

    private final VoucherSeriesService voucherSeriesService;
    private final VoucherNumberService voucherNumberService;

    @PostMapping
    public ResponseEntity<ApiResponse<VoucherSeriesResponse>> create(
            @Valid @RequestBody VoucherSeriesRequest request) {
        VoucherSeriesMaster series = voucherSeriesService.create(request);
        return ResponseEntity.ok(ApiResponse.success("Series created", VoucherSeriesResponse.from(series)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VoucherSeriesResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody VoucherSeriesRequest request) {
        VoucherSeriesMaster series = voucherSeriesService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Series updated", VoucherSeriesResponse.from(series)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VoucherSeriesResponse>>> list(
            @RequestParam(required = false) String voucherTypeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        org.springframework.data.domain.Page<VoucherSeriesResponse> result =
                voucherSeriesService.list(voucherTypeId, page, size)
                        .map(VoucherSeriesResponse::from);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result.getNumber() + 1, result.getTotalElements()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VoucherSeriesResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(VoucherSeriesResponse.from(voucherSeriesService.getById(id))));
    }

    @GetMapping("/generate-series")
    public ResponseEntity<ApiResponse<String>> generateNumber(
            @RequestParam String voucherTypeId,
            @RequestParam String branchId) {
        String voucherNumber = voucherNumberService.generateVoucherNumber(voucherTypeId, branchId);
        return ResponseEntity.ok(ApiResponse.success(voucherNumber));
    }

    @GetMapping("/{id}/preview-next")
    public ResponseEntity<ApiResponse<String>> previewNext(
            @PathVariable String id,
            @RequestParam(required = false) String branchId) {
        String next = voucherSeriesService.previewNextNumber(id, branchId);
        return ResponseEntity.ok(ApiResponse.success(next));
    }

    @PostMapping("/{id}/restart-schedule")
    public ResponseEntity<ApiResponse<VoucherSeriesRestartSchedule>> addRestartSchedule(
            @PathVariable String id,
            @RequestBody RestartScheduleRequest body) {
        VoucherSeriesRestartSchedule schedule = voucherSeriesService.addRestartSchedule(
                id, body.getApplicableFromDate(), body.getStartingNumber(),
                body.getPrefixOverride(), body.getSuffixOverride());
        return ResponseEntity.ok(ApiResponse.success("Restart schedule added", schedule));
    }

    @GetMapping("/{id}/restart-schedule")
    public ResponseEntity<ApiResponse<List<VoucherSeriesRestartSchedule>>> getRestartSchedules(
            @PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(voucherSeriesService.getRestartSchedules(id)));
    }

    @Getter
    @Setter
    public static class RestartScheduleRequest {
        private LocalDate applicableFromDate;
        private Integer startingNumber = 1;
        private String prefixOverride;
        private String suffixOverride;
    }
}
