package com.mpu.vendor.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ProductMedia {

    private Long mediaId;

    private Long productId;

    private String mediaType;

    private String url;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
