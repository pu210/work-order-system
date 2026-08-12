package com.eeit219.work_order_system.common.response;

public record ApiResponse<T>(
        boolean success,
        int status,
        String message,
        T data) {

    public static <T> ApiResponse<T> success(
            int status,
            String message,
            T data) {

        return new ApiResponse<>(
                true,
                status,
                message,
                data);
    }

    public static <T> ApiResponse<T> error(
            int status,
            String message) {

        return new ApiResponse<>(
                false,
                status,
                message,
                null);
    }
}