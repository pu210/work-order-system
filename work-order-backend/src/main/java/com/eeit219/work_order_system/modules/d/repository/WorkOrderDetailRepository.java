package com.eeit219.work_order_system.modules.d.repository;

import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WorkOrderDetailRepository extends JpaRepository<WorkOrder, Integer> {

    @EntityGraph(attributePaths = {
            "creator",
            "admin",
            "assignedHandler",
            "subCategory",
            "subCategory.repairCategory",
            "priority"
    })
    @Query("SELECT workOrder FROM WorkOrder workOrder WHERE workOrder.workOrderId = :workOrderId")
    Optional<WorkOrder> findDetailById(@Param("workOrderId") Integer workOrderId);
}
