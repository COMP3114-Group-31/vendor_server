package com.mpu.vendor.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Product {
    private Integer id;
    private String name;
    private String nameCn;
    private BigDecimal price;
    private String thumbnailUrl;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
