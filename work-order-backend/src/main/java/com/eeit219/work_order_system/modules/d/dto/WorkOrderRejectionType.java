package com.eeit219.work_order_system.modules.d.dto;

/**
 * D 模組用來區分工單退回情境。
 *
 * 不直接把 REJECT 當成單一類型，是因為不同退回流程
 * 對應的可見角色與畫面提示並不相同。
 */
public enum WorkOrderRejectionType {

    // 負責工程師拒絕處理，工單回到待審核狀態。
    HANDLER_RETURNED,

    // 管理員初審拒絕，工單進入取消狀態。
    ADMIN_REJECTED,

    // 管理員驗收未通過，工單退回負責工程師重做。
    ADMIN_RETURNED_FOR_REWORK
}
