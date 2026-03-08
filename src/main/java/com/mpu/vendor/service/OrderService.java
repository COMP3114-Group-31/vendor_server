package com.mpu.vendor.service;

import java.util.List;

import com.mpu.vendor.dto.OrderCreateRequest;
import com.mpu.vendor.dto.OrderUpdateRequest;
import com.mpu.vendor.entity.Order;

public interface OrderService {

    List<Order> list();

    Order get(Long id);

    Long create(OrderCreateRequest request);

    void update(Long id, OrderUpdateRequest request);

    void delete(Long id);
}
