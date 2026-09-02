package com.eeit219.work_order_system.modules.b.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eeit219.work_order_system.modules.b.dto.WorkOrderAttachmentResponse;
import com.eeit219.work_order_system.modules.b.entity.WorkOrderAttachment;

public interface WorkOrderAttachmentRepository extends JpaRepository<WorkOrderAttachment, Integer> {

    // 直接投影成 DTO、不 select fileData：fileData 是 byte[] LOB 欄位，JPA 預設 eager 載入（專案沒裝
    // bytecode enhancement 做不到 lazy），查整個 entity 會把圖片二進位內容整包搬進 JVM，但清單回應本來就不需要它。
    // contactRecordId IS NULL：D 模組加的，排除留言附圖，只回建單當下上傳的附件
    @Query("SELECT new com.eeit219.work_order_system.modules.b.dto.WorkOrderAttachmentResponse(" +
            "a.attachmentId, a.originalFileName, a.contentType, a.fileSize, a.createdTime, " +
            "a.uploadedUser.name, a.uploadedUser.userId) " +
            "FROM WorkOrderAttachment a " +
            "WHERE a.workOrder.workOrderId = :workOrderId" +
            " AND a.contactRecordId IS NULL")
    List<WorkOrderAttachmentResponse> findByWorkOrder_WorkOrderId(@Param("workOrderId") Integer workOrderId);

    // D模組新增，查詢指定聯繫紀錄所附帶的圖片可在留言區顯示。
    List<WorkOrderAttachment> findByContactRecordIdOrderByCreatedTimeAscAttachmentIdAsc(Integer contactRecordId);
}
