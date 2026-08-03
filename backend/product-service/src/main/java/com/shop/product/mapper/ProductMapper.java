package com.shop.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shop.product.entity.Product;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ProductMapper extends BaseMapper<Product> {

    @Select("<script>" +
            "SELECT DISTINCT p.* FROM product p " +
            "LEFT JOIN product_sku s ON s.product_id = p.id AND s.deleted = 0 " +
            "WHERE p.deleted = 0 AND p.status = 1 " +
            "<if test='name != null and name != \"\"'> AND p.name LIKE CONCAT('%', #{name}, '%')</if>" +
            "<if test='categoryId != null'> AND p.category_id = #{categoryId}</if>" +
            "<if test='region != null and region != \"\"'> AND p.region = #{region}</if>" +
            "<if test='brand != null and brand != \"\"'> AND p.brand = #{brand}</if>" +
            "<if test='minPrice != null'> AND s.price &gt;= #{minPrice}</if>" +
            "<if test='maxPrice != null'> AND s.price &lt;= #{maxPrice}</if>" +
            "<if test='startDate != null'> AND p.publish_date &gt;= #{startDate}</if>" +
            "<if test='endDate != null'> AND p.publish_date &lt;= #{endDate}</if>" +
            " ORDER BY " +
            "<choose>" +
            "<when test='sort == \"priceAsc\"'> s.price ASC, p.id DESC</when>" +
            "<when test='sort == \"priceDesc\"'> s.price DESC, p.id DESC</when>" +
            "<when test='sort == \"sales\"'> p.sales DESC, p.id DESC</when>" +
            "<when test='sort == \"rating\"'> p.rating DESC, p.id DESC</when>" +
            "<otherwise> p.publish_date DESC, p.id DESC</otherwise>" +
            "</choose>" +
            "</script>")
    IPage<Product> searchPage(IPage<Product> page,
                              @Param("name") String name,
                              @Param("categoryId") Long categoryId,
                              @Param("region") String region,
                              @Param("brand") String brand,
                              @Param("minPrice") BigDecimal minPrice,
                              @Param("maxPrice") BigDecimal maxPrice,
                              @Param("startDate") LocalDateTime startDate,
                              @Param("endDate") LocalDateTime endDate,
                              @Param("sort") String sort);
}

