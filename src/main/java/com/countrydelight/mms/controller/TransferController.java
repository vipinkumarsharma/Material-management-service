package com.countrydelight.mms.controller;

import com.countrydelight.mms.dto.common.ApiResponse;
import com.countrydelight.mms.dto.transfer.TransferCreateRequest;
import com.countrydelight.mms.entity.transfer.StockTransferHeader;
import com.countrydelight.mms.service.transfer.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    /**
     * Create a new inter-branch transfer.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<StockTransferHeader>> createTransfer(
            @Valid @RequestBody TransferCreateRequest request) {
        StockTransferHeader transfer = transferService.createTransfer(request);
        return ResponseEntity.ok(ApiResponse.success("Transfer created successfully", transfer));
    }

    /**
     * Dispatch transfer from sender branch.
     * Creates Issue at sender and updates ledger with TRANSFER_OUT.
     */
    @PostMapping("/{transferId}/dispatch")
    public ResponseEntity<ApiResponse<StockTransferHeader>> dispatchTransfer(
            @PathVariable Long transferId,
            @RequestParam String sourceLocationId,
            @RequestParam String dispatchedBy) {
        StockTransferHeader transfer = transferService.dispatchTransfer(transferId, sourceLocationId, dispatchedBy);
        return ResponseEntity.ok(ApiResponse.success("Transfer dispatched successfully", transfer));
    }

    /**
     * Receive transfer at destination branch.
     * Creates GRN at receiver and updates ledger with TRANSFER_IN.
     */
    @PostMapping("/{transferId}/receive")
    public ResponseEntity<ApiResponse<StockTransferHeader>> receiveTransfer(
            @PathVariable Long transferId,
            @RequestParam String destLocationId,
            @RequestParam String receivedBy) {
        StockTransferHeader transfer = transferService.receiveTransfer(transferId, destLocationId, receivedBy);
        return ResponseEntity.ok(ApiResponse.success("Transfer received successfully", transfer));
    }

    /**
     * Get transfer by ID.
     */
    @GetMapping("/{transferId}")
    public ResponseEntity<ApiResponse<StockTransferHeader>> getTransfer(@PathVariable Long transferId) {
        StockTransferHeader transfer = transferService.getTransfer(transferId);
        return ResponseEntity.ok(ApiResponse.success(transfer));
    }

    /**
     * Get pending dispatch transfers for a branch.
     */
    @GetMapping("/pending-dispatch/{branchId}")
    public ResponseEntity<ApiResponse<List<StockTransferHeader>>> getPendingDispatch(
            @PathVariable String branchId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<StockTransferHeader> result = transferService.getPendingDispatch(branchId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result.getNumber() + 1, result.getTotalElements()));
    }

    /**
     * Get pending receipt transfers for a branch.
     */
    @GetMapping("/pending-receipt/{branchId}")
    public ResponseEntity<ApiResponse<List<StockTransferHeader>>> getPendingReceipt(
            @PathVariable String branchId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<StockTransferHeader> result = transferService.getPendingReceipt(branchId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result.getNumber() + 1, result.getTotalElements()));
    }
}
