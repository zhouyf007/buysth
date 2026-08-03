package com.shop.order.dto;

import lombok.Data;

@Data
public class UpdateCartRequest {

    private Integer quantity;
    private Integer checked;
}

