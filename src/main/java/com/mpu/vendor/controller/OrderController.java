package com.mpu.vendor.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mpu.vendor.dto.OrderCreateRequest;
import com.mpu.vendor.dto.OrderResponse;
import com.mpu.vendor.dto.OrderUpdateRequest;
import com.mpu.vendor.entity.Order;
import com.mpu.vendor.service.OrderService;

@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public Map<String, Object> listOrders() {
        List<Order> dbList = orderService.list();
        List<OrderResponse> resultList = new ArrayList<>();

        for (Order item : dbList) {
            resultList.add(toResponse(item));
        }

        Map<String, Object> map = new HashMap<>();
        map.put("orders", resultList);
        return map;
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable Long orderId) {
        return toResponse(orderService.get(orderId));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody OrderCreateRequest request) {
        Long newId = orderService.create(request);

        Map<String, Object> map = new HashMap<>();
        map.put("id", newId);
        map.put("order_id", newId);
        map.put("message", "Order created");
        return ResponseEntity.status(HttpStatus.CREATED).body(map);
    }

    @PatchMapping("/{orderId}")
    public Map<String, Object> updateOrder(@PathVariable Long orderId,
                                           @RequestBody OrderUpdateRequest request) {
        orderService.update(orderId, request);

        Map<String, Object> map = new HashMap<>();
        map.put("message", "Order updated");
        return map;
    }

    @DeleteMapping("/{orderId}")
    public Map<String, Object> deleteOrder(@PathVariable Long orderId) {
        orderService.delete(orderId);

        Map<String, Object> map = new HashMap<>();
        map.put("message", "Order deleted");
        return map;
    }

    private OrderResponse toResponse(Order item) {
        OrderResponse one = new OrderResponse();
        one.setOrderId(item.getId());
        one.setCustomerName(item.getCustomerName());
        one.setTotalAmount(item.getTotalAmount());
        one.setStatus(item.getStatus());
        one.setCreatedAt(item.getCreatedAt());
        one.setUpdatedAt(item.getUpdatedAt());
        return one;
    }
}
