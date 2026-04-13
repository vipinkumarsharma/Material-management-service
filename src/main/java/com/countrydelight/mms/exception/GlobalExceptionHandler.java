package com.countrydelight.mms.exception;

import com.countrydelight.mms.dto.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MmsException.class)
    public ResponseEntity<ApiResponse<Void>> handleMmsException(MmsException ex) {
        log.error("MMS Exception: {}", ex.getMessage(), ex);
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleInsufficientStock(InsufficientStockException ex) {
        log.error("Insufficient stock: {}", ex.getMessage());
        Map<String, Object> details = new HashMap<>();
        details.put("itemId", ex.getItemId());
        details.put("locationId", ex.getLocationId());
        details.put("requestedQty", ex.getRequestedQty());
        details.put("availableQty", ex.getAvailableQty());

        ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
                .success(false)
                .message(ex.getMessage())
                .data(details)
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ApprovalRequiredException.class)
    public ResponseEntity<ApiResponse<Object>> handleApprovalRequired(ApprovalRequiredException ex) {
        log.warn("Approval required: {}", ex.getMessage());
        Map<String, Object> details = new HashMap<>();
        details.put("txnType", ex.getTxnType());
        details.put("priceVariances", ex.getPriceVariances());

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .data(details)
                .build();
        return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .success(false)
                .message("Validation failed")
                .data(errors)
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity
                .internalServerError()
                .body(ApiResponse.error("An unexpected error occurred"));
    }
}
