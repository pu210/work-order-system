package com.eeit219.work_order_system.modules.b.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eeit219.work_order_system.modules.b.entity.WorkOrderAttachment;

public interface WorkOrderAttachmentRepository extends JpaRepository<WorkOrderAttachment, Integer> {

    // D模組新增，contactRecordId排除not null留言圖片，只取得工單建立時上傳的附件
    @Query("SELECT a FROM WorkOrderAttachment a " +
            "JOIN FETCH a.uploadedUser " +
            "WHERE a.workOrder.workOrderId = :workOrderId" +
            " AND a.contactRecordId IS NULL")

    List<WorkOrderAttachment> findByWorkOrder_WorkOrderId(@Param("workOrderId") Integer workOrderId);

    // D模組新增，查詢指定聯繫紀錄所附帶的圖片可在留言區顯示。
    List<WorkOrderAttachment> findByContactRecordIdOrderByCreatedTimeAscAttachmentIdAsc(Integer contactRecordId);
}
