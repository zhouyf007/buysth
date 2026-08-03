package com.shop.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockLockRequest {

    @NotBlank
    private String orderNo;

    @NotNull
    private Long skuId;

    @Min(1)
    private Integer quantity;

    private Long userId;
}

