package com.mpu.vendor.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DashboardMapper {

    @Select("SELECT COUNT(*) FROM orders WHERE DATE(created_at) = CURRENT_DATE")
    int countTodayOrders();

    @Select("SELECT COUNT(*) FROM orders WHERE status = '待处理'")
    int countPendingOrders();

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE YEAR(created_at) = YEAR(CURRENT_DATE) AND MONTH(created_at) = MONTH(CURRENT_DATE)")
    BigDecimal sumCurrentMonthSales();

    @Select("SELECT COUNT(*) FROM products")
    int countProducts();

    @Select({
            "<script>",
            "SELECT id AS order_id, customer_name, total_amount, status, created_at",
            "FROM orders",
            "<if test='search != null and search != \"\"'>",
            "WHERE CAST(id AS CHAR) LIKE CONCAT('%', #{search}, '%')",
            "   OR customer_name LIKE CONCAT('%', #{search}, '%')",
            "</if>",
            "ORDER BY created_at DESC, id DESC",
            "LIMIT 5",
            "</script>"
    })
    List<Map<String, Object>> listRecentOrders(@Param("search") String search);
}
