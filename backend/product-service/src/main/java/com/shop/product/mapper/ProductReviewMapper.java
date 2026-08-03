package com.shop.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.product.entity.ProductReview;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

public interface ProductReviewMapper extends BaseMapper<ProductReview> {

    @Select("SELECT COALESCE(AVG(rating), 5) FROM product_review WHERE product_id = #{productId} AND status = 1 AND deleted = 0")
    BigDecimal averageRating(@Param("productId") Long productId);
}

