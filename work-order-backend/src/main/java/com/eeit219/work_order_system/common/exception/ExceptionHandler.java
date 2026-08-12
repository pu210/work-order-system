package com.eeit219.work_order_system.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.eeit219.work_order_system.common.response.ApiResponse;

@RestControllerAdvice
public class ExceptionHandler {
    // 2026_08_09
    // 非法參數 回 400
    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
            ResourceNotFoundException exception) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse.error(
                        HttpStatus.NOT_FOUND.value(),
                        exception.getMessage()));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
            IllegalArgumentException exception) {

        return ResponseEntity.badRequest().body(
                ApiResponse.error(
                        HttpStatus.BAD_REQUEST.value(),
                        exception.getMessage()));
    }

}
