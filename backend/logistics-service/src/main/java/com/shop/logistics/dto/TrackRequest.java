package com.shop.logistics.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class TrackRequest {

    @NotBlank(message = "物流状态不能为空")
    private String status;

    @NotBlank(message = "轨迹描述不能为空")
    private String description;
}

