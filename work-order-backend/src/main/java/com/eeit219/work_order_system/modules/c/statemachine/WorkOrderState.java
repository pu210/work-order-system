package com.eeit219.work_order_system.modules.c.statemachine;
public enum WorkOrderState {
    PENDING_REVIEW,             // 待審核
    CANCELLED,                  // 已取消
    PENDING_EVALUATION,         // 待評估
    IN_PROGRESS,                // 進行中
    PENDING_USER_ACCEPTANCE,    // 待使用者驗收
    PENDING_ADMIN_ACCEPTANCE,   // 待管理員驗收
    COMPLETED                   // 已完成
}