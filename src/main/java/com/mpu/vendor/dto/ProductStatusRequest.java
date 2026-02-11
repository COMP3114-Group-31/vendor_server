package com.mpu.vendor.dto;

import lombok.Data;

@Data
public class ProductStatusRequest {

    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
