package com.shop.api.feign;

import com.shop.api.dto.ProductDTO;
import com.shop.api.dto.SalesIncreaseRequest;
import com.shop.api.dto.SkuDTO;
import com.shop.api.dto.StockLockRequest;
import com.shop.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/internal/products/{productId}")
    Result<ProductDTO> getProduct(@PathVariable("productId") Long productId);

    @GetMapping("/internal/skus/{skuId}")
    Result<SkuDTO> getSku(@PathVariable("skuId") Long skuId);

    @PostMapping("/internal/stock/lock")
    Result<Boolean> lockStock(@RequestBody StockLockRequest request);

    @PostMapping("/internal/stock/release")
    Result<Boolean> releaseStock(@RequestBody StockLockRequest request);

    @PostMapping("/internal/sales/increase")
    Result<Boolean> increaseSales(@RequestBody SalesIncreaseRequest request);
}
