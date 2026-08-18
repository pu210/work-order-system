package com.eeit219.work_order_system.common.exception;

// 通用資源衝突例外，對應 HTTP 409。
// 用於「請求本身合法、資源也存在，但目前狀態與請求的操作互斥」的情境，
// 例如：非上傳者嘗試刪除他人上傳的附件。跨模組可共用，不限 Module B。
public class ResourceConflictException extends RuntimeException {

    public ResourceConflictException(String message) {
        super(message);
    }
}
