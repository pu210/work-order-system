package com.eeit219.work_order_system.modules.d.dto;

/**
 * 接受流程中各階段提交的回饋類型。
 */
public enum WorkOrderFeedbackType {
    ADMIN_REVIEW,
    HANDLER_COMPLETION,
    USER_ACCEPTANCE,
    ADMIN_ACCEPTANCE
}
