package com.shop.product.controller;

import com.shop.common.result.PageResult;
import com.shop.common.result.Result;
import com.shop.common.context.UserContext;
import com.shop.product.dto.ReviewRequest;
import com.shop.product.dto.ProductVO;
import com.shop.product.entity.Category;
import com.shop.product.entity.ProductReview;
import com.shop.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/categories")
    public Result<List<Category>> categories() {
        return Result.ok(productService.listCategories());
    }

    @GetMapping("/list")
    public Result<PageResult<ProductVO>> list(@RequestParam(defaultValue = "1") long current,
                                              @RequestParam(defaultValue = "12") long size,
                                              @RequestParam(required = false) String name,
                                              @RequestParam(required = false) Long categoryId,
                                              @RequestParam(required = false) String region,
                                              @RequestParam(required = false) String brand,
                                              @RequestParam(required = false) BigDecimal minPrice,
                                              @RequestParam(required = false) BigDecimal maxPrice,
                                              @RequestParam(required = false)
                                              @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
                                              @RequestParam(required = false)
                                              @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate,
                                              @RequestParam(required = false) String sort) {
        return Result.ok(productService.search(current, size, name, categoryId, region, brand,
                minPrice, maxPrice, startDate, endDate, sort));
    }

    @GetMapping("/hot")
    public Result<List<ProductVO>> hot() {
        return Result.ok(productService.hot());
    }

    @GetMapping("/{id}")
    public Result<ProductVO> detail(@PathVariable Long id) {
        return Result.ok(productService.detail(id));
    }

    @GetMapping("/{id}/reviews")
    public Result<PageResult<ProductReview>> reviews(@PathVariable Long id,
                                                     @RequestParam(defaultValue = "1") long current,
                                                     @RequestParam(defaultValue = "5") long size) {
        return Result.ok(productService.pageReviews(id, current, size));
    }

    @PostMapping("/{id}/reviews")
    public Result<Void> addReview(@PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
        productService.addReview(id, UserContext.getUserId(), request.getRating(), request.getContent());
        return Result.ok();
    }
}
