package com.eeit219.work_order_system.modules.b.entity;

import java.time.LocalDateTime;
import java.util.Optional;

import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;
import com.eeit219.work_order_system.modules.f.entity.Priority;
import com.eeit219.work_order_system.modules.f.entity.RepairTargets;
import com.eeit219.work_order_system.modules.f.entity.SubCategory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "work_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "work_order_id")
    private Integer workOrderId;

    @Column(name = "work_order_no", nullable = false, length = 20)
    private String workOrderNo;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_category_id", nullable = false)
    private SubCategory subCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "priority_id", nullable = false)
    private Priority priority;

    @Column(name = "location_detail", nullable = false, length = 100)
    private String locationDetail;

    @Column(name = "contact_phone", length = 10)
    private String contactPhone;

    @Column(name = "description", length = 300)
    private String description;

    @Column(name = "due_time")
    private LocalDateTime dueTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private WorkOrderState status = WorkOrderState.PENDING_REVIEW;

    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_user_id", nullable = false)
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private User admin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_handler")
    private User assignedHandler;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id")
    private RepairTargets repairTargets;

    @Column(name = "is_overdue", nullable = false)
    private Boolean isOverdue = false;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version = 0;

}
