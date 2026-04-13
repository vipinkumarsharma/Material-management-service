package com.countrydelight.mms.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private Integer page;
    private Long count;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Success")
                .data(data)
                .timestamp(LocalDateTime.now(IST))
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now(IST))
                .build();
    }

    public static <T> ApiResponse<T> success(T data, int page, long count) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Success")
                .data(data)
                .page(page)
                .count(count)
                .timestamp(LocalDateTime.now(IST))
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now(IST))
                .build();
    }
}
