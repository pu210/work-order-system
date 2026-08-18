package com.eeit219.work_order_system.modules.d.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class ContactRecordCreateRequest {

    @NotBlank
    @Size(max = 500)
    private String content;

}
