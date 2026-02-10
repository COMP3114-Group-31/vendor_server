package com.mpu.vendor.service.impl;

import com.mpu.vendor.dto.ProductCreateRequest;
import com.mpu.vendor.dto.ProductStatusRequest;
import com.mpu.vendor.dto.ProductUpdateRequest;
import com.mpu.vendor.entity.Product;
import com.mpu.vendor.exception.BadRequestException;
import com.mpu.vendor.exception.NotFoundException;
import com.mpu.vendor.mapper.ProductMapper;
import com.mpu.vendor.service.ProductService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    @Override
    public List<Product> list(String search, String status) {
        String cleanSearch = null;
        String cleanStatus = null;

        if (search != null) {
            cleanSearch = search.trim();
            if (cleanSearch.isEmpty()) {
                cleanSearch = null;
            }
        }

        if (status != null) {
            cleanStatus = status.trim().toLowerCase();
            if (cleanStatus.isEmpty()) {
                cleanStatus = null;
            }
            if (cleanStatus != null && !cleanStatus.equals("active") && !cleanStatus.equals("inactive")) {
                throw new BadRequestException("Invalid request data", "status",
                        "Status must be either 'active' or 'inactive'");
            }
        }

        return productMapper.listProducts(cleanSearch, cleanStatus);
    }

    @Override
    public Product get(Integer id) {
        Product product = productMapper.findById(id);
        if (product == null) {
            throw new NotFoundException("Product not found");
        }
        return product;
    }

    @Override
    public Integer create(ProductCreateRequest request) {
        if (request == null) {
            throw new BadRequestException("Invalid request data");
        }
        if (!StringUtils.hasText(request.getName())) {
            throw new BadRequestException("Invalid request data", "name", "Name is required");
        }
        if (!StringUtils.hasText(request.getNameCn())) {
            throw new BadRequestException("Invalid request data", "name_cn", "Name_cn is required");
        }
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Invalid request data", "price", "Price must be a positive number");
        }

        Product product = new Product();
        product.setName(request.getName().trim());
        product.setNameCn(request.getNameCn().trim());
        product.setPrice(request.getPrice());
        product.setStatus("active");
        product.setThumbnailUrl(null);
        LocalDateTime now = LocalDateTime.now();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        int rows = productMapper.insert(product);
        if (rows != 1) {
            throw new RuntimeException("Failed to create product");
        }
        return product.getId();
    }

    @Override
    public void update(Integer id, ProductUpdateRequest request) {
        if (request == null) {
            throw new BadRequestException("Invalid request data");
        }
        Product existing = productMapper.findById(id);
        if (existing == null) {
            throw new NotFoundException("Product not found");
        }
        if (request.getName() != null && request.getName().trim().isEmpty()) {
            throw new BadRequestException("Invalid request data", "name", "Name is required");
        }
        if (request.getNameCn() != null && request.getNameCn().trim().isEmpty()) {
            throw new BadRequestException("Invalid request data", "name_cn", "Name_cn is required");
        }
        if (request.getPrice() != null && request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Invalid request data", "price", "Price must be a positive number");
        }

        Product toUpdate = new Product();
        toUpdate.setId(id);

        if (request.getName() != null) {
            toUpdate.setName(request.getName().trim());
        }
        if (request.getNameCn() != null) {
            toUpdate.setNameCn(request.getNameCn().trim());
        }

        toUpdate.setPrice(request.getPrice());
        toUpdate.setUpdatedAt(LocalDateTime.now());

        int rows = productMapper.update(toUpdate);
        if (rows != 1) {
            throw new RuntimeException("Failed to update product");
        }
    }

    @Override
    public void updateStatus(Integer id, ProductStatusRequest request) {
        if (request == null || !StringUtils.hasText(request.getStatus())) {
            throw new BadRequestException("Invalid request data", "status",
                    "Status must be either 'active' or 'inactive'");
        }
        String status = request.getStatus().trim().toLowerCase();
        if (!status.equals("active") && !status.equals("inactive")) {
            throw new BadRequestException("Invalid request data", "status",
                    "Status must be either 'active' or 'inactive'");
        }
        Product existing = productMapper.findById(id);
        if (existing == null) {
            throw new NotFoundException("Product not found");
        }
        int rows = productMapper.updateStatus(id, status, LocalDateTime.now());
        if (rows != 1) {
            throw new RuntimeException("Failed to update product status");
        }
    }

    @Override
    public void delete(Integer id) {
        Product existing = productMapper.findById(id);
        if (existing == null) {
            throw new NotFoundException("Product not found");
        }
        int rows = productMapper.deleteById(id);
        if (rows != 1) {
            throw new RuntimeException("Failed to delete product");
        }
    }
}
