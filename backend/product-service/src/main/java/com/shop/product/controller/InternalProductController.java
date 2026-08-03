package com.shop.product.controller;

import com.shop.api.dto.ProductDTO;
import com.shop.api.dto.SalesIncreaseRequest;
import com.shop.api.dto.SkuDTO;
import com.shop.api.dto.StockLockRequest;
import com.shop.common.result.Result;
import com.shop.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalProductController {

    private final ProductService productService;

    @GetMapping("/products/{productId}")
    public Result<ProductDTO> product(@PathVariable Long productId) {
        return Result.ok(productService.internalProduct(productId));
    }

    @GetMapping("/skus/{skuId}")
    public Result<SkuDTO> sku(@PathVariable Long skuId) {
        return Result.ok(productService.internalSku(skuId));
    }

    @PostMapping("/stock/lock")
    public Result<Boolean> lock(@RequestBody StockLockRequest request) {
        return Result.ok(productService.lockStock(request));
    }

    @PostMapping("/stock/release")
    public Result<Boolean> release(@RequestBody StockLockRequest request) {
        return Result.ok(productService.releaseStock(request));
    }

    @PostMapping("/sales/increase")
    public Result<Boolean> increaseSales(@RequestBody SalesIncreaseRequest request) {
        return Result.ok(productService.increaseSales(request));
    }
}
