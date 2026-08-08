package com.eeit219.work_order_system.modules.c.domain;

import java.time.LocalDateTime;

import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;

@Data
@Entity
@Table(name = "work_orders")
public class WorkOrderBean {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "work_order_id")
    private Integer workOrderId;
    @Column(name = "work_order_no")
    private String workOrderNo;
    @Column(name = "title")
    private String title;
    @Column(name = "sub_category_id")
    private Integer subCategoryId;
    @Column(name = "priority_id")
    private Integer priorityId;
    @Column(name = "location_detail")
    private String locationDetail;
    @Column(name = "contact_phone")
    private String contactPhone;
    @Column(name = "description")
    private String description;
    @Column(name = "due_time")
    private LocalDateTime dueTime;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WorkOrderState status;
    @Column(name = "created_time")
    private LocalDateTime createdTime;
    @Column(name = "creator_user_id")
    private Integer creatorUserId;
    @Column(name = "assigned_handler")
    private Integer assignedHandler;
    @Column(name = "is_overdue")
    private Boolean isOverdue;
    @Version
    @Column(name = "version")
    private Integer version;
}