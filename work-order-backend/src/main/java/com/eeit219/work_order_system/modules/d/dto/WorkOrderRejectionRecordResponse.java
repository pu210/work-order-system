package com.eeit219.work_order_system.modules.d.dto;

import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 工單退回紀錄的前端回傳資料。
 *
 * 僅提供畫面顯示需要的欄位，避免直接回傳 RepairTicketHistory Entity
 * 而暴露不必要的資料庫關聯。
 */
@Getter
@Builder
@AllArgsConstructor
public class WorkOrderRejectionRecordResponse {

    // 退回歷程的唯一識別碼。
    private Integer historyId;

    // 用來區分工程師退回、管理員拒絕或管理員退回重做。
    private WorkOrderRejectionType rejectionType;

    // 執行退回操作的人員 ID。
    private Integer rejectedByUserId;

    // 執行退回操作的人員姓名。
    private String rejectedByName;

    // 使用者填寫的退回原因。
    private String reason;

    // 退回操作的發生時間。
    private LocalDateTime rejectedTime;

    // 完成退回操作後，工單進入的狀態。
    private WorkOrderState resultingStatus;
}
