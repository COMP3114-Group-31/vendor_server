package com.mpu.vendor.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.mpu.vendor.entity.Order;

@Mapper
public interface OrderMapper {

    @Select("SELECT id, customer_name, total_amount, status, created_at, updated_at FROM orders ORDER BY created_at DESC")
    @Results({
            @Result(column = "customer_name", property = "customerName"),
            @Result(column = "total_amount", property = "totalAmount"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt")
    })
    List<Order> listOrders();

    @Select("SELECT id, customer_name, total_amount, status, created_at, updated_at FROM orders WHERE id = #{id}")
    @Results({
            @Result(column = "customer_name", property = "customerName"),
            @Result(column = "total_amount", property = "totalAmount"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt")
    })
    Order findById(@Param("id") Long id);

    @Insert("INSERT INTO orders (customer_name, total_amount, status) VALUES (#{customerName}, #{totalAmount}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    @Update({
            "<script>",
            "UPDATE orders",
            "<set>",
            "  <if test='customerName != null'>customer_name = #{customerName},</if>",
            "  <if test='totalAmount != null'>total_amount = #{totalAmount},</if>",
            "  <if test='status != null'>status = #{status},</if>",
            "</set>",
            "WHERE id = #{id}",
            "</script>"
    })
    int update(Order order);

    @Delete("DELETE FROM orders WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
