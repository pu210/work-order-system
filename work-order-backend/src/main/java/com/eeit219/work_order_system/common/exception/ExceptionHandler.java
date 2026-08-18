package com.eeit219.work_order_system.common.exception;

import com.eeit219.work_order_system.common.response.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

        // @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
        // public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
        // IllegalArgumentException exception) {

        // return ResponseEntity.badRequest().body(
        // ApiResponse.error(
        // HttpStatus.BAD_REQUEST.value(),
        // exception.getMessage()));
        // }

        private static final Logger log = LoggerFactory.getLogger(ExceptionHandler.class);

        // @org.springframework.web.bind.annotation.ExceptionHandler(
        // ResourceNotFoundException.class)
        // public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
        // ResourceNotFoundException exception) {

        // return ResponseEntity.status(HttpStatus.NOT_FOUND)
        // .body(ApiResponse.error(
        // HttpStatus.NOT_FOUND.value(),
        // exception.getMessage()));
        // }

        // @org.springframework.web.bind.annotation.ExceptionHandler(
        // IllegalArgumentException.class)
        // public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
        // IllegalArgumentException exception) {

        // return ResponseEntity.badRequest()
        // .body(ApiResponse.error(
        // HttpStatus.BAD_REQUEST.value(),
        // exception.getMessage()));
        // }

        @org.springframework.web.bind.annotation.ExceptionHandler(InvalidWorkOrderStateException.class)
        public ResponseEntity<ApiResponse<Void>> handleInvalidState(
                        InvalidWorkOrderStateException exception) {

                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ApiResponse.error(
                                                HttpStatus.CONFLICT.value(),
                                                exception.getMessage()));
        }

        @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Void>> handleValidation(
                        MethodArgumentNotValidException exception) {

                String message = exception.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .findFirst()
                                .map(error -> error.getDefaultMessage())
                                .orElse("請求資料驗證失敗");

                return ResponseEntity.badRequest()
                                .body(ApiResponse.error(
                                                HttpStatus.BAD_REQUEST.value(),
                                                message));
        }

        @org.springframework.web.bind.annotation.ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiResponse<Void>> handleUnreadableJson(
                        HttpMessageNotReadableException exception) {

                return ResponseEntity.badRequest()
                                .body(ApiResponse.error(
                                                HttpStatus.BAD_REQUEST.value(),
                                                "JSON 格式或欄位格式錯誤"));
        }

        // @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
        // public ResponseEntity<ApiResponse<Void>> handleUnexpectedError(
        // Exception exception) {

        // log.error("未預期的伺服器錯誤", exception);

        //         return ResponseEntity.status(
        //                         HttpStatus.INTERNAL_SERVER_ERROR)
        //                         .body(ApiResponse.error(
        //                                         HttpStatus.INTERNAL_SERVER_ERROR.value(),
        //                                         "伺服器發生未預期錯誤"));
        // }

        // 403 沒有權限 Forbidden
        @org.springframework.web.bind.annotation.ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
                        AccessDeniedException exception) {

                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(ApiResponse.error(
                                                HttpStatus.FORBIDDEN.value(),
                                                exception.getMessage()));
        }
        // 409 樂觀鎖發生衝突
        @org.springframework.web.bind.annotation.ExceptionHandler(OptimisticLockingFailureException.class)
        public ResponseEntity<ApiResponse<Void>> handleOptimisticLock(
                        OptimisticLockingFailureException exception) {

                log.warn("工單發生樂觀鎖衝突", exception);

                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ApiResponse.error(
                                                HttpStatus.CONFLICT.value(),
                                                "工單已被其他人修改，請重新載入最新資料"));
        }
        // return ResponseEntity.status(
        // HttpStatus.INTERNAL_SERVER_ERROR)
        // .body(ApiResponse.error(
        // HttpStatus.INTERNAL_SERVER_ERROR.value(),
        // "伺服器發生未預期錯誤"));
        // }
//                return ResponseEntity.status(
//                                HttpStatus.INTERNAL_SERVER_ERROR)
//                                .body(ApiResponse.error(
//                                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
//                                                "伺服器發生未預期錯誤"));
//        }

        // D Module 無權查看403
        @org.springframework.web.bind.annotation.ExceptionHandler(
                AccessDeniedException.class)
        public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException e){

                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(),
                                e.getMessage()));

        }

        @org.springframework.web.bind.annotation.ExceptionHandler(EntityNotFoundException.class)
        public ResponseEntity<ApiResponse<Void>> EntityNotFound(EntityNotFoundException e) {

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(
                                HttpStatus.NOT_FOUND.value(),
                                e.getMessage()
                        ));


        }
}
