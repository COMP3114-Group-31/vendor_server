package com.mpu.vendor.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mpu.vendor.mapper.DashboardMapper;

@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardMapper dashboardMapper;

    public DashboardController(DashboardMapper dashboardMapper) {
        this.dashboardMapper = dashboardMapper;
    }

    @GetMapping("/summary")
    public Map<String, Object> summary(@RequestParam(required = false) String search) {
        Map<String, Object> map = new HashMap<>();
        map.put("today_orders", dashboardMapper.countTodayOrders());
        map.put("pending_orders", dashboardMapper.countPendingOrders());
        map.put("month_sales", dashboardMapper.sumCurrentMonthSales());
        map.put("product_count", dashboardMapper.countProducts());
        map.put("recent_orders", dashboardMapper.listRecentOrders(search));
        return map;
    }
}
