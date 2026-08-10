package com.eeit219.work_order_system.modules.F.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "repair_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RepairCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "repair_categories_id")
    private Integer repairCategoriesId;

    private String name;

    @Column(name = "status")
    private Boolean status = true;

    @Column(name = "created_time")
    private LocalDateTime createdTime = LocalDateTime.now();

    @Column(name = "updated_time")
    private LocalDateTime updatedTime = LocalDateTime.now();

    @Column(name = "default_priority_id")
    private Integer defaultPriorityId;

    @Column(name = "default_priority_name")
    private String defaultPriorityName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_priority_id", referencedColumnName = "priorities_id", insertable = false, updatable = false)
    private Priority defaultPriority;
}