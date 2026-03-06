package com.mpu.vendor.service;

import java.util.List;

import com.mpu.vendor.dto.ProductCreateRequest;
import com.mpu.vendor.dto.ProductStatusRequest;
import com.mpu.vendor.dto.ProductUpdateRequest;
import com.mpu.vendor.entity.Product;

public interface ProductService {

    List<Product> list(String search, String status);

    Product get(Long id);

    Long create(ProductCreateRequest request);

    void update(Long id, ProductUpdateRequest request);

    void updateStatus(Long id, ProductStatusRequest request);

    void delete(Long id);
}
