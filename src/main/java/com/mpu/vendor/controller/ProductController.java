package com.mpu.vendor.controller;

import com.mpu.vendor.dto.ProductCreateRequest;
import com.mpu.vendor.dto.ProductResponse;
import com.mpu.vendor.dto.ProductStatusRequest;
import com.mpu.vendor.dto.ProductUpdateRequest;
import com.mpu.vendor.entity.Product;
import com.mpu.vendor.service.ProductService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Map<String, Object> listProducts(@RequestParam(required = false) String search,
                                            @RequestParam(required = false) String status) {
        List<Product> productList = productService.list(search, status);
        List<ProductResponse> products = new ArrayList<>();
        for (Product product : productList) {
            products.add(toListResponse(product));
        }
        Map<String, Object> response = new HashMap<>();
        response.put("products", products);
        return response;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createProduct(@RequestBody ProductCreateRequest request) {
        Integer id = productService.create(request);
        Map<String, Object> response = new HashMap<>();
        response.put("product_id", id);
        response.put("message", "Product created");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{productId}")
    public ProductResponse getProduct(@PathVariable Integer productId) {
        Product product = productService.get(productId);
        return toDetailResponse(product);
    }

    @PatchMapping("/{productId}")
    public Map<String, Object> updateProduct(@PathVariable Integer productId,
                                             @RequestBody ProductUpdateRequest request) {
        productService.update(productId, request);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Product updated");
        return response;
    }

    @PatchMapping("/{productId}/status")
    public Map<String, Object> updateStatus(@PathVariable Integer productId,
                                            @RequestBody ProductStatusRequest request) {
        productService.updateStatus(productId, request);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Product status updated");
        return response;
    }

    @DeleteMapping("/{productId}")
    public Map<String, Object> deleteProduct(@PathVariable Integer productId) {
        productService.delete(productId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Product deleted");
        return response;
    }

    private ProductResponse toListResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setProductId(product.getId());
        response.setName(product.getName());
        response.setNameCn(product.getNameCn());
        response.setPrice(product.getPrice());
        response.setStatus(product.getStatus());
        response.setThumbUrl(product.getThumbnailUrl());
        return response;
    }

    private ProductResponse toDetailResponse(Product product) {
        ProductResponse response = toListResponse(product);
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        return response;
    }
}
