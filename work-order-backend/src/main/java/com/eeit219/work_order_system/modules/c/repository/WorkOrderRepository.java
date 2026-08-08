package com.eeit219.work_order_system.modules.c.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.eeit219.work_order_system.modules.c.domain.WorkOrderBean;

public interface WorkOrderRepository extends JpaRepository<WorkOrderBean, Integer> {

}
