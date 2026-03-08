package com.mpu.vendor.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class OrderCreateRequest {

    @JsonProperty("customer_name")
    private String customerName;

    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    private String status;
}
