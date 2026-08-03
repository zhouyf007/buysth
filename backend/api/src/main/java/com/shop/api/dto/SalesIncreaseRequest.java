package com.shop.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class SalesIncreaseRequest {

    private String orderNo;
    private List<Item> items;

    @Data
    public static class Item {
        private Long productId;
        private Integer quantity;
    }
}

