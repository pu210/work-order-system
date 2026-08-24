package com.eeit219.work_order_system.common.exception;

// 通用「JWT 已通過驗證，但對應帳號在資料庫查無」例外，對應 HTTP 401 Unauthorized。跨模組可共用，不限 Module B。
public class AuthenticatedUserNotFoundException extends RuntimeException {

    public AuthenticatedUserNotFoundException(String message) {
        super(message);
    }
}
