package com.shop.order.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class OrderCreateRequest {

    @NotEmpty(message = "订单商品不能为空")
    @Valid
    private List<Item> items;

    @Valid
    private AddressDTO address;

    private String remark;
    private String promotionCode;

    @Data
    public static class Item {
        private Long skuId;
        private Integer quantity;
    }
}

