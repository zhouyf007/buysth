package com.shop.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shop.order.dto.MonthlyOrderStat;
import com.shop.order.dto.StatusStat;
import com.shop.order.entity.Orders;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface OrdersMapper extends BaseMapper<Orders> {

    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m') AS month, COUNT(*) AS orderCount, " +
            "COALESCE(SUM(pay_amount), 0) AS totalAmount FROM orders " +
            "WHERE deleted = 0 AND admin_deleted = 0 AND status IN ('PAID', 'SHIPPED', 'COMPLETED') " +
            "AND create_time >= DATE_SUB(NOW(), INTERVAL 5 MONTH) " +
            "GROUP BY month ORDER BY month")
    List<MonthlyOrderStat> monthlyStats();

    @Select("SELECT status, COUNT(*) AS count FROM orders WHERE deleted = 0 AND admin_deleted = 0 GROUP BY status")
    List<StatusStat> statusStats();

    @Update("UPDATE orders SET user_deleted = 1 WHERE order_no = #{orderNo} AND user_id = #{userId}")
    int softDeleteByUser(@Param("orderNo") String orderNo, @Param("userId") Long userId);

    @Update("UPDATE orders SET user_deleted = 0 WHERE order_no = #{orderNo} AND user_id = #{userId}")
    int restoreByUser(@Param("orderNo") String orderNo, @Param("userId") Long userId);

    @Select("SELECT * FROM orders WHERE user_id = #{userId} AND deleted = 0 AND user_deleted = 1 ORDER BY create_time DESC")
    IPage<Orders> selectDeletedPage(IPage<Orders> page, @Param("userId") Long userId);

    @Update("<script>UPDATE orders SET admin_deleted = 1 WHERE order_no IN " +
            "<foreach collection='orderNos' item='no' open='(' separator=',' close=')'>#{no}</foreach></script>")
    int softDeleteBatch(@Param("orderNos") java.util.List<String> orderNos);
}
