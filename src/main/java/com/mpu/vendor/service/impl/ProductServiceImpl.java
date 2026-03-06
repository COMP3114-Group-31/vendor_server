package com.mpu.vendor.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.mpu.vendor.dto.ProductCreateRequest;
import com.mpu.vendor.dto.ProductStatusRequest;
import com.mpu.vendor.dto.ProductUpdateRequest;
import com.mpu.vendor.entity.Product;
import com.mpu.vendor.exception.BadRequestException;
import com.mpu.vendor.exception.NotFoundException;
import com.mpu.vendor.mapper.ProductMapper;
import com.mpu.vendor.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    @Override
    public List<Product> list(String search, String status) {
        String searchValue = null;
        if (search != null) {
            String tmp = search.trim();
            if (!tmp.isEmpty()) {
                searchValue = tmp;
            }
        }

        String statusValue = null;
        if (status != null) {
            String tmp = status.trim().toLowerCase();
            if (!tmp.isEmpty()) {
                if (!tmp.equals("active") && !tmp.equals("inactive")) {
                    throw new BadRequestException("Invalid request data", "status",
                            "Status must be either 'active' or 'inactive'");
                }
                statusValue = tmp;
            }
        }

        return productMapper.listProducts(searchValue, statusValue);
    }

    @Override
    public Product get(Long id) {
        Product dbData = productMapper.findById(id);
        if (dbData == null) {
            throw new NotFoundException("Product not found");
        }
        return dbData;
    }

    @Override
    public Long create(ProductCreateRequest request) {
        if (request == null) {
            throw new BadRequestException("Invalid request data");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException("Invalid request data", "name", "Name is required");
        }
        if (request.getNameCn() == null || request.getNameCn().trim().isEmpty()) {
            throw new BadRequestException("Invalid request data", "name_cn", "Name_cn is required");
        }
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new BadRequestException("Invalid request data", "description", "Description is required");
        }
        if (request.getDescriptionCn() == null || request.getDescriptionCn().trim().isEmpty()) {
            throw new BadRequestException("Invalid request data", "description_cn", "Description_cn is required");
        }
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Invalid request data", "price", "Price must be a positive number");
        }
        if (request.getCategory() != null && !request.getCategory().trim().isEmpty()) {
            String c = request.getCategory().trim().toLowerCase();
            if (!isValidCategory(c)) {
                throw new BadRequestException("Invalid request data", "category",
                        "Category must be one of: phone, tablet, laptop, accessory, other");
            }
        }

        Product saveData = new Product();
        saveData.setName(request.getName().trim());
        saveData.setNameCn(request.getNameCn().trim());
        saveData.setDescription(request.getDescription().trim());
        saveData.setDescriptionCn(request.getDescriptionCn().trim());
        saveData.setPrice(request.getPrice());

        if (request.getThumbnailUrl() != null) {
            saveData.setThumbnailUrl(request.getThumbnailUrl().trim());
        }
        if (request.getCategory() != null && !request.getCategory().trim().isEmpty()) {
            saveData.setCategory(request.getCategory().trim().toLowerCase());
        }

        int rows = productMapper.insert(saveData);
        if (rows != 1) {
            throw new RuntimeException("Failed to create product");
        }

        return saveData.getId();
    }

    @Override
    public void update(Long id, ProductUpdateRequest request) {
        if (request == null) {
            throw new BadRequestException("Invalid request data");
        }

        Product dbData = productMapper.findById(id);
        if (dbData == null) {
            throw new NotFoundException("Product not found");
        }

        if (request.getName() != null && request.getName().trim().isEmpty()) {
            throw new BadRequestException("Invalid request data", "name", "Name is required");
        }
        if (request.getNameCn() != null && request.getNameCn().trim().isEmpty()) {
            throw new BadRequestException("Invalid request data", "name_cn", "Name_cn is required");
        }
        if (request.getDescription() != null && request.getDescription().trim().isEmpty()) {
            throw new BadRequestException("Invalid request data", "description", "Description is required");
        }
        if (request.getDescriptionCn() != null && request.getDescriptionCn().trim().isEmpty()) {
            throw new BadRequestException("Invalid request data", "description_cn", "Description_cn is required");
        }
        if (request.getPrice() != null && request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Invalid request data", "price", "Price must be a positive number");
        }
        if (request.getCategory() != null && !request.getCategory().trim().isEmpty()) {
            String c = request.getCategory().trim().toLowerCase();
            if (!isValidCategory(c)) {
                throw new BadRequestException("Invalid request data", "category",
                        "Category must be one of: phone, tablet, laptop, accessory, other");
            }
        }

        Product updateData = new Product();
        updateData.setId(id);

        if (request.getName() != null) {
            updateData.setName(request.getName().trim());
        }
        if (request.getNameCn() != null) {
            updateData.setNameCn(request.getNameCn().trim());
        }
        if (request.getDescription() != null) {
            updateData.setDescription(request.getDescription().trim());
        }
        if (request.getDescriptionCn() != null) {
            updateData.setDescriptionCn(request.getDescriptionCn().trim());
        }
        if (request.getPrice() != null) {
            updateData.setPrice(request.getPrice());
        }
        if (request.getThumbnailUrl() != null) {
            updateData.setThumbnailUrl(request.getThumbnailUrl().trim());
        }
        if (request.getCategory() != null) {
            updateData.setCategory(request.getCategory().trim().toLowerCase());
        }

        int rows = productMapper.update(updateData);
        if (rows != 1) {
            throw new RuntimeException("Failed to update product");
        }
    }

    @Override
    public void updateStatus(Long id, ProductStatusRequest request) {
        if (request == null || request.getStatus() == null || request.getStatus().trim().isEmpty()) {
            throw new BadRequestException("Invalid request data", "status",
                    "Status must be either 'active' or 'inactive'");
        }

        String statusValue = request.getStatus().trim().toLowerCase();
        if (!statusValue.equals("active") && !statusValue.equals("inactive")) {
            throw new BadRequestException("Invalid request data", "status",
                    "Status must be either 'active' or 'inactive'");
        }

        Product dbData = productMapper.findById(id);
        if (dbData == null) {
            throw new NotFoundException("Product not found");
        }

        int rows = productMapper.updateStatus(id, statusValue);
        if (rows != 1) {
            throw new RuntimeException("Failed to update product status");
        }
    }

    @Override
    public void delete(Long id) {
        Product dbData = productMapper.findById(id);
        if (dbData == null) {
            throw new NotFoundException("Product not found");
        }

        int rows = productMapper.deleteById(id);
        if (rows != 1) {
            throw new RuntimeException("Failed to delete product");
        }
    }

    private boolean isValidCategory(String c) {
        List<String> arr = new ArrayList<>();
        arr.add("phone");
        arr.add("tablet");
        arr.add("laptop");
        arr.add("accessory");
        arr.add("other");
        return arr.contains(c);
    }
}
