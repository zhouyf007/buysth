package com.shop.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.order.dto.MonthlyOrderStat;
import com.shop.order.dto.StatusStat;
import com.shop.order.entity.Orders;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface OrdersMapper extends BaseMapper<Orders> {

    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m') AS month, COUNT(*) AS orderCount, " +
            "COALESCE(SUM(pay_amount), 0) AS totalAmount FROM orders " +
            "WHERE deleted = 0 AND status IN ('PAID', 'SHIPPED', 'COMPLETED') " +
            "AND create_time >= DATE_SUB(NOW(), INTERVAL 5 MONTH) " +
            "GROUP BY month ORDER BY month")
    List<MonthlyOrderStat> monthlyStats();

    @Select("SELECT status, COUNT(*) AS count FROM orders WHERE deleted = 0 GROUP BY status")
    List<StatusStat> statusStats();
}
