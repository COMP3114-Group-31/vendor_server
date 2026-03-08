package com.mpu.vendor.service.impl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.mpu.vendor.dto.OrderCreateRequest;
import com.mpu.vendor.dto.OrderUpdateRequest;
import com.mpu.vendor.entity.Order;
import com.mpu.vendor.exception.BadRequestException;
import com.mpu.vendor.exception.NotFoundException;
import com.mpu.vendor.mapper.OrderMapper;
import com.mpu.vendor.service.OrderService;

@Service
public class OrderServiceImpl implements OrderService {

    private static final List<String> ALLOWED_STATUS = Arrays.asList("待处理", "处理中", "已发货", "已交付");

    private final OrderMapper orderMapper;

    public OrderServiceImpl(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    @Override
    public List<Order> list() {
        return orderMapper.listOrders();
    }

    @Override
    public Order get(Long id) {
        Order dbData = orderMapper.findById(id);
        if (dbData == null) {
            throw new NotFoundException("Order not found");
        }
        return dbData;
    }

    @Override
    public Long create(OrderCreateRequest request) {
        if (request == null) {
            throw new BadRequestException("Invalid request data");
        }

        validate(request.getCustomerName(), request.getTotalAmount(), request.getStatus(), false);

        Order saveData = new Order();
        saveData.setCustomerName(request.getCustomerName().trim());
        saveData.setTotalAmount(request.getTotalAmount());
        saveData.setStatus(normalizeStatus(request.getStatus()));

        int rows = orderMapper.insert(saveData);
        if (rows != 1) {
            throw new RuntimeException("Failed to create order");
        }
        return saveData.getId();
    }

    @Override
    public void update(Long id, OrderUpdateRequest request) {
        if (request == null) {
            throw new BadRequestException("Invalid request data");
        }

        Order dbData = orderMapper.findById(id);
        if (dbData == null) {
            throw new NotFoundException("Order not found");
        }

        validate(request.getCustomerName(), request.getTotalAmount(), request.getStatus(), true);

        Order updateData = new Order();
        updateData.setId(id);
        if (request.getCustomerName() != null) {
            updateData.setCustomerName(request.getCustomerName().trim());
        }
        if (request.getTotalAmount() != null) {
            updateData.setTotalAmount(request.getTotalAmount());
        }
        if (request.getStatus() != null) {
            updateData.setStatus(normalizeStatus(request.getStatus()));
        }

        int rows = orderMapper.update(updateData);
        if (rows != 1) {
            throw new RuntimeException("Failed to update order");
        }
    }

    @Override
    public void delete(Long id) {
        Order dbData = orderMapper.findById(id);
        if (dbData == null) {
            throw new NotFoundException("Order not found");
        }

        int rows = orderMapper.deleteById(id);
        if (rows != 1) {
            throw new RuntimeException("Failed to delete order");
        }
    }

    private void validate(String customerName, BigDecimal totalAmount, String status, boolean allowNull) {
        if (!allowNull || customerName != null) {
            if (customerName == null || customerName.trim().isEmpty()) {
                throw new BadRequestException("Invalid request data", "customer_name", "Customer name is required");
            }
        }

        if (!allowNull || totalAmount != null) {
            if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Invalid request data", "total_amount", "Total amount must be >= 0");
            }
        }

        if (!allowNull || status != null) {
            if (status == null || status.trim().isEmpty()) {
                throw new BadRequestException("Invalid request data", "status", "Status is required");
            }
            if (!ALLOWED_STATUS.contains(status.trim())) {
                throw new BadRequestException("Invalid request data", "status", "Status must be one of: 待处理, 处理中, 已发货, 已交付");
            }
        }
    }

    private String normalizeStatus(String status) {
        return status == null ? null : status.trim();
    }
}
