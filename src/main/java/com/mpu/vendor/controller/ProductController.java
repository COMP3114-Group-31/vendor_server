package com.mpu.vendor.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mpu.vendor.dto.ProductCreateRequest;
import com.mpu.vendor.dto.ProductResponse;
import com.mpu.vendor.dto.ProductStatusRequest;
import com.mpu.vendor.dto.ProductUpdateRequest;
import com.mpu.vendor.entity.Product;
import com.mpu.vendor.service.ProductService;

@RestController
@RequestMapping("/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Map<String, Object> listProducts(@RequestParam(required = false) String search,
                                            @RequestParam(required = false) String status) {
        List<Product> dbList = productService.list(search, status);
        List<ProductResponse> resultList = new ArrayList<>();

        for (Product item : dbList) {
            ProductResponse one = new ProductResponse();
            one.setProductId(item.getId());
            one.setName(item.getName());
            one.setNameCn(item.getNameCn());
            one.setDescription(item.getDescription());
            one.setDescriptionCn(item.getDescriptionCn());
            one.setPrice(item.getPrice());
            one.setThumbnailUrl(item.getThumbnailUrl());
            one.setCategory(item.getCategory());
            one.setStatus(item.getStatus());
            one.setCreatedAt(item.getCreatedAt());
            one.setUpdatedAt(item.getUpdatedAt());
            resultList.add(one);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("products", resultList);
        return map;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createProduct(@RequestBody ProductCreateRequest request) {
        Long newId = productService.create(request);

        Map<String, Object> map = new HashMap<>();
        map.put("id", newId);
        map.put("product_id", newId);
        map.put("message", "Product created");

        return ResponseEntity.status(HttpStatus.CREATED).body(map);
    }

    @GetMapping("/{productId}")
    public ProductResponse getProduct(@PathVariable Long productId) {
        Product item = productService.get(productId);

        ProductResponse one = new ProductResponse();
        one.setProductId(item.getId());
        one.setName(item.getName());
        one.setNameCn(item.getNameCn());
        one.setDescription(item.getDescription());
        one.setDescriptionCn(item.getDescriptionCn());
        one.setPrice(item.getPrice());
        one.setThumbnailUrl(item.getThumbnailUrl());
        one.setCategory(item.getCategory());
        one.setStatus(item.getStatus());
        one.setCreatedAt(item.getCreatedAt());
        one.setUpdatedAt(item.getUpdatedAt());
        return one;
    }

    @PatchMapping("/{productId}")
    public Map<String, Object> updateProduct(@PathVariable Long productId,
                                             @RequestBody ProductUpdateRequest request) {
        productService.update(productId, request);

        Map<String, Object> map = new HashMap<>();
        map.put("message", "Product updated");
        return map;
    }

    @PatchMapping("/{productId}/status")
    public Map<String, Object> updateStatus(@PathVariable Long productId,
                                            @RequestBody ProductStatusRequest request) {
        productService.updateStatus(productId, request);

        Map<String, Object> map = new HashMap<>();
        map.put("message", "Product status updated");
        return map;
    }

    @DeleteMapping("/{productId}")
    public Map<String, Object> deleteProduct(@PathVariable Long productId) {
        productService.delete(productId);

        Map<String, Object> map = new HashMap<>();
        map.put("message", "Product deleted");
        return map;
    }

}
