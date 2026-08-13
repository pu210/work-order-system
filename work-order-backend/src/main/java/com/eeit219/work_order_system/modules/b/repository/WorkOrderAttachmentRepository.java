package com.eeit219.work_order_system.modules.b.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eeit219.work_order_system.modules.b.entity.WorkOrderAttachment;

public interface WorkOrderAttachmentRepository extends JpaRepository<WorkOrderAttachment, Integer> {

    @Query("SELECT a FROM WorkOrderAttachment a " +
            "JOIN FETCH a.uploadedUser " +
            "WHERE a.workOrder.workOrderId = :workOrderId")
    List<WorkOrderAttachment> findByWorkOrder_WorkOrderId(@Param("workOrderId") Integer workOrderId);
}
