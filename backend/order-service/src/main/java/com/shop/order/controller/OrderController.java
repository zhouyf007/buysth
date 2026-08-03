package com.shop.order.controller;

import com.shop.common.context.UserContext;
import com.shop.common.result.PageResult;
import com.shop.common.result.Result;
import com.shop.order.dto.AddCartRequest;
import com.shop.order.dto.AddressDTO;
import com.shop.order.dto.CartItemVO;
import com.shop.order.dto.CreateOrderResult;
import com.shop.order.dto.OrderCreateRequest;
import com.shop.order.dto.OrderVO;
import com.shop.order.dto.PromotionPreviewRequest;
import com.shop.order.dto.PromotionPreviewResponse;
import com.shop.order.dto.UpdateCartRequest;
import com.shop.order.service.CartService;
import com.shop.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final CartService cartService;
    private final OrderService orderService;

    @GetMapping("/cart")
    public Result<List<CartItemVO>> cart() {
        return Result.ok(cartService.list(UserContext.getUserId()));
    }

    @PostMapping("/cart")
    public Result<Void> addCart(@Valid @RequestBody AddCartRequest request) {
        cartService.add(UserContext.getUserId(), request);
        return Result.ok();
    }

    @PutMapping("/cart/{cartId}")
    public Result<Void> updateCart(@PathVariable Long cartId, @RequestBody UpdateCartRequest request) {
        cartService.update(UserContext.getUserId(), cartId, request);
        return Result.ok();
    }

    @DeleteMapping("/cart/{cartId}")
    public Result<Void> deleteCart(@PathVariable Long cartId) {
        cartService.delete(UserContext.getUserId(), cartId);
        return Result.ok();
    }

    @PostMapping("/create")
    public Result<CreateOrderResult> create(@Valid @RequestBody OrderCreateRequest request) {
        return Result.ok(orderService.createOrder(UserContext.getUserId(), request));
    }

    @GetMapping("/list")
    public Result<PageResult<OrderVO>> list(@RequestParam(defaultValue = "1") long current,
                                            @RequestParam(defaultValue = "10") long size,
                                            @RequestParam(required = false) String status) {
        return Result.ok(orderService.userPage(UserContext.getUserId(), current, size, status));
    }

    @GetMapping("/deleted")
    public Result<PageResult<OrderVO>> deleted(@RequestParam(defaultValue = "1") long current,
                                               @RequestParam(defaultValue = "10") long size) {
        return Result.ok(orderService.userDeletedPage(UserContext.getUserId(), current, size));
    }

    @GetMapping("/{orderNo}")
    public Result<OrderVO> detail(@PathVariable String orderNo) {
        return Result.ok(orderService.detail(UserContext.getUserId(), orderNo, false));
    }

    @PostMapping("/cancel/{orderNo}")
    public Result<Void> cancel(@PathVariable String orderNo) {
        orderService.cancel(UserContext.getUserId(), orderNo);
        return Result.ok();
    }

    @PostMapping("/confirm/{orderNo}")
    public Result<Void> confirm(@PathVariable String orderNo) {
        orderService.confirmReceipt(UserContext.getUserId(), orderNo);
        return Result.ok();
    }

    @PostMapping("/{orderNo}/address")
    public Result<Void> updateAddress(@PathVariable String orderNo, @Valid @RequestBody AddressDTO address) {
        orderService.updateAddress(UserContext.getUserId(), orderNo, address);
        return Result.ok();
    }

    @DeleteMapping("/{orderNo}")
    public Result<Void> deleteOrder(@PathVariable String orderNo) {
        orderService.deleteUserOrder(UserContext.getUserId(), orderNo);
        return Result.ok();
    }

    @PostMapping("/{orderNo}/restore")
    public Result<Void> restoreOrder(@PathVariable String orderNo) {
        orderService.restoreUserOrder(UserContext.getUserId(), orderNo);
        return Result.ok();
    }

    @PostMapping("/{orderNo}/promotion")
    public Result<Void> applyPromotion(@PathVariable String orderNo, @RequestBody Map<String, String> body) {
        orderService.applyPromotion(UserContext.getUserId(), orderNo, body.get("promotionCode"));
        return Result.ok();
    }

    @PostMapping("/promotion/preview")
    public Result<PromotionPreviewResponse> previewPromotion(@RequestBody PromotionPreviewRequest request) {
        return Result.ok(orderService.previewPromotion(request));
    }
}
