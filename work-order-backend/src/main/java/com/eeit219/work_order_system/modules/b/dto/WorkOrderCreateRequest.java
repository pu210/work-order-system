package com.eeit219.work_order_system.modules.b.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkOrderCreateRequest {

    @NotBlank(message = "標題不可為空")
    @Size(max = 50, message = "標題長度不可超過 50 字")
    private String title;

    @NotNull(message = "細項類別為必填")
    private Integer subCategoryId;

    @NotBlank(message = "位置不可為空")
    @Size(max = 100, message = "位置長度不可超過 100 字")
    private String locationDetail;

    @Size(max = 10, message = "聯絡電話長度不可超過 10 字")
    private String contactPhone;

    @Size(max = 300, message = "描述長度不可超過 300 字")
    private String description;
}
