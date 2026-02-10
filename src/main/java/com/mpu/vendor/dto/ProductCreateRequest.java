package com.mpu.vendor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class ProductCreateRequest {
    private String name;
    @JsonProperty("name_cn")
    private String nameCn;
    private BigDecimal price;
}
