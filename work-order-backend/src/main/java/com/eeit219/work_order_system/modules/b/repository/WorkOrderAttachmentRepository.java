package com.eeit219.work_order_system.modules.b.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eeit219.work_order_system.modules.b.dto.WorkOrderAttachmentResponse;
import com.eeit219.work_order_system.modules.b.entity.WorkOrderAttachment;

public interface WorkOrderAttachmentRepository extends JpaRepository<WorkOrderAttachment, Integer> {

    // 直接投影成 DTO、不 select fileData：這個 LOB 欄位沒設 lazy fetch（專案沒裝 bytecode enhancement，
    // JPA 對 byte[] 預設就是 eager），原本「SELECT a FROM ...」查列表會把整張圖片的二進位內容都撈進 JVM，
    // 但回應的 WorkOrderAttachmentResponse 根本不含 fileData，等於每次列附件都白白搬一次大檔案。
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
