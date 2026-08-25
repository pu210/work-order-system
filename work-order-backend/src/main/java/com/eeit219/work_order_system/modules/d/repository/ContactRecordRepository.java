package com.eeit219.work_order_system.modules.d.repository;

import com.eeit219.work_order_system.modules.d.entity.ContactRecord;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContactRecordRepository extends JpaRepository<ContactRecord, Integer>{
    @EntityGraph(attributePaths = {"author", "workOrder"})

    List<ContactRecord> findByWorkOrder_WorkOrderIdOrderByCreatedTimeAscRecordIdAsc(Integer workOrderId);

    Optional<ContactRecord> findByRecordIdAndWorkOrder_WorkOrderId(Integer recordId, Integer workOrderId);
}
