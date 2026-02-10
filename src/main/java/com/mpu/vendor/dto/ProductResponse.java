package com.mpu.vendor.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductResponse {
    @JsonProperty("product_id")
    private Integer productId;
    private String name;
    @JsonProperty("name_cn")
    private String nameCn;
    private BigDecimal price;
    private String status;
    @JsonProperty("thumb_url")
    private String thumbUrl;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
