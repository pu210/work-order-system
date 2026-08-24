package com.eeit219.work_order_system.common.exception;

// 通用業務規則違反例外，對應 HTTP 422 Unprocessable Entity。
// 用於「請求格式正確、引用的資源也存在，但伺服器因業務規則或資料完整性問題無法處理」的情境，
// 例如：工單子類別與所屬大類別皆未設定優先級。跨模組可共用，不限 Module B。
public class BusinessRuleViolationException extends RuntimeException {

    public BusinessRuleViolationException(String message) {
        super(message);
    }
}
