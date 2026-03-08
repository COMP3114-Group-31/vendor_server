package com.mpu.vendor.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class OrderResponse {

    @JsonProperty("order_id")
    private Long orderId;

    @JsonProperty("customer_name")
    private String customerName;

    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    private String status;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
