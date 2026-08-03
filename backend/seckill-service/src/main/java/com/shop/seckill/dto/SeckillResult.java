package com.shop.seckill.dto;

import lombok.Data;

@Data
public class SeckillResult {

    private boolean success;
    private String message;
    private String orderNo;
}

