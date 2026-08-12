package com.eeit219.work_order_system.modules.f.entity;

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
@Table(name = "sub_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sub_categories_id")
    private Integer subCategoriesId;

    @Column(name = "category_id")
    private Integer categoryId;

    private String name;

    @Column(name = "override_priority_id")
    private Integer overridePriorityId;

    @Column(name = "override_priority_name")
    private String overridePriorityName;

    @Column(name = "status")
    private Boolean status = true;

    @Column(name = "created_time")
    private LocalDateTime createdTime = LocalDateTime.now();

    @Column(name = "updated_time")
    private LocalDateTime updatedTime = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "repair_categories_id", insertable = false, updatable = false)
    private RepairCategory repairCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "override_priority_id", referencedColumnName = "priorities_id", insertable = false, updatable = false)
    private Priority overridePriority;
}