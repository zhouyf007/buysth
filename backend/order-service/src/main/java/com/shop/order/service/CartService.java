package com.shop.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.api.dto.ProductDTO;
import com.shop.api.dto.SkuDTO;
import com.shop.api.feign.ProductClient;
import com.shop.common.exception.BizException;
import com.shop.common.result.Result;
import com.shop.order.dto.AddCartRequest;
import com.shop.order.dto.CartItemVO;
import com.shop.order.dto.UpdateCartRequest;
import com.shop.order.entity.CartItem;
import com.shop.order.mapper.CartItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemMapper cartItemMapper;
    private final ProductClient productClient;

    public void add(Long userId, AddCartRequest request) {
        SkuDTO sku = getSku(request.getSkuId());
        CartItem exist = cartItemMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getSkuId, request.getSkuId()));
        if (exist != null) {
            int newQuantity = exist.getQuantity() + request.getQuantity();
            if (sku.getStock() != null && newQuantity > sku.getStock()) {
                throw new BizException("库存不足");
            }
            exist.setQuantity(newQuantity);
            exist.setChecked(1);
            cartItemMapper.updateById(exist);
        } else {
            if (sku.getStock() != null && request.getQuantity() > sku.getStock()) {
                throw new BizException("库存不足");
            }
            CartItem item = new CartItem();
            item.setUserId(userId);
            item.setSkuId(request.getSkuId());
            item.setQuantity(request.getQuantity());
            item.setChecked(1);
            cartItemMapper.insert(item);
        }
    }

    public List<CartItemVO> list(Long userId) {
        List<CartItem> items = cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .orderByDesc(CartItem::getCreateTime));
        return items.stream().map(item -> toVO(item)).collect(Collectors.toList());
    }

    public void update(Long userId, Long cartId, UpdateCartRequest request) {
        CartItem item = cartItemMapper.selectById(cartId);
        if (item == null || !item.getUserId().equals(userId)) {
            throw new BizException(404, "购物车项不存在");
        }
        if (request.getQuantity() != null) {
            if (request.getQuantity() <= 0) {
                cartItemMapper.deleteById(cartId);
                return;
            }
            SkuDTO sku = getSku(item.getSkuId());
            if (sku.getStock() != null && request.getQuantity() > sku.getStock()) {
                throw new BizException("库存不足");
            }
            item.setQuantity(request.getQuantity());
        }
        if (request.getChecked() != null) {
            item.setChecked(request.getChecked());
        }
        cartItemMapper.updateById(item);
    }

    public void delete(Long userId, Long cartId) {
        CartItem item = cartItemMapper.selectById(cartId);
        if (item != null && item.getUserId().equals(userId)) {
            cartItemMapper.deleteById(cartId);
        }
    }

    public List<CartItem> checkedItems(Long userId) {
        return cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getChecked, 1));
    }

    public CartItemVO toVO(CartItem item) {
        SkuDTO sku = getSku(item.getSkuId());
        CartItemVO vo = new CartItemVO();
        vo.setId(item.getId());
        vo.setSkuId(item.getSkuId());
        vo.setProductId(sku.getProductId());
        vo.setSkuSpec(sku.getSpecName() + " " + sku.getSpecValue());
        vo.setImage(sku.getImage() == null ? "" : sku.getImage());
        vo.setPrice(sku.getPrice());
        vo.setStock(sku.getStock());
        vo.setQuantity(item.getQuantity());
        vo.setChecked(item.getChecked());
        vo.setSubtotal(sku.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        try {
            Result<ProductDTO> productResult = productClient.getProduct(sku.getProductId());
            if (productResult.isSuccess() && productResult.getData() != null) {
                vo.setProductName(productResult.getData().getName());
                if (sku.getImage() == null || sku.getImage().isBlank()) {
                    vo.setImage(productResult.getData().getMainImage());
                }
            }
        } catch (Exception ignored) {
            vo.setProductName("商品");
        }
        return vo;
    }

    private SkuDTO getSku(Long skuId) {
        try {
            Result<SkuDTO> result = productClient.getSku(skuId);
            if (!result.isSuccess() || result.getData() == null) {
                throw new BizException(404, "商品不存在");
            }
            return result.getData();
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("商品服务暂时不可用");
        }
    }
}

