package com.shop.api.feign;

import com.shop.api.dto.PromotionDTO;
import com.shop.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "seckill-service")
public interface SeckillClient {

    @GetMapping("/internal/promotions/{code}")
    Result<PromotionDTO> getPromotion(@PathVariable("code") String code);
}

