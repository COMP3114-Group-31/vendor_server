package com.mpu.vendor.service;

import com.mpu.vendor.dto.ProductCreateRequest;
import com.mpu.vendor.dto.ProductStatusRequest;
import com.mpu.vendor.dto.ProductUpdateRequest;
import com.mpu.vendor.entity.Product;
import java.util.List;

public interface ProductService {
    List<Product> list(String search, String status);

    Product get(Integer id);

    Integer create(ProductCreateRequest request);

    void update(Integer id, ProductUpdateRequest request);

    void updateStatus(Integer id, ProductStatusRequest request);

    void delete(Integer id);
}
