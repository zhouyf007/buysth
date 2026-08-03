package com.shop.order.dto;

import lombok.Data;

import java.util.List;

@Data
public class PromotionPreviewRequest {

    private String promotionCode;
    private List<Item> items;

    @Data
    public static class Item {
        private Long skuId;
        private Integer quantity;
    }
}

