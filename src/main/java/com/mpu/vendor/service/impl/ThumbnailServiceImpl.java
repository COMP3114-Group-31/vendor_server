package com.mpu.vendor.service.impl;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.mpu.vendor.entity.Product;
import com.mpu.vendor.entity.ProductMedia;
import com.mpu.vendor.exception.BadRequestException;
import com.mpu.vendor.exception.NotFoundException;
import com.mpu.vendor.mapper.ProductMapper;
import com.mpu.vendor.mapper.ProductMediaMapper;
import com.mpu.vendor.service.ThumbnailService;
import com.mpu.vendor.utils.AliyunOSSOperator;

@Service
public class ThumbnailServiceImpl implements ThumbnailService {

    private final ProductMapper productMapper;
    private final ProductMediaMapper productMediaMapper;
    private final AliyunOSSOperator aliyunOSSOperator;

    public ThumbnailServiceImpl(ProductMapper productMapper,
                                ProductMediaMapper productMediaMapper,
                                AliyunOSSOperator aliyunOSSOperator) {
        this.productMapper = productMapper;
        this.productMediaMapper = productMediaMapper;
        this.aliyunOSSOperator = aliyunOSSOperator;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<ProductMedia> uploadThumbnails(Long productId, MultipartFile[] files, boolean setFirstAsMain) {
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new NotFoundException("Product not found");
        }

        checkUploadFiles(files);

        String firstUploadedUrl = null;

        for (MultipartFile file : files) {
            try {
                String originalFilename = file.getOriginalFilename();
                String url = aliyunOSSOperator.uploadProductThumbnail(file.getBytes(), originalFilename, productId);

                ProductMedia media = new ProductMedia();
                media.setProductId(productId);
                media.setMediaType("image");
                media.setUrl(url);

                int insertRows = productMediaMapper.insert(media);
                if (insertRows != 1) {
                    throw new RuntimeException("Failed to save thumbnail record");
                }

                if (firstUploadedUrl == null) {
                    firstUploadedUrl = url;
                }
            } catch (BadRequestException ex) {
                throw ex;
            } catch (Exception ex) {
                String msg = ex.getMessage();
                if (msg == null || msg.isBlank()) {
                    msg = ex.getClass().getSimpleName();
                }
                throw new RuntimeException("Failed to upload thumbnail: " + msg, ex);
            }
        }

        if (setFirstAsMain && firstUploadedUrl != null) {
            updateMainThumbnail(productId, firstUploadedUrl);
        }

        return productMediaMapper.listByProductId(productId);
    }

    @Override
    public List<ProductMedia> listThumbnails(Long productId) {
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new NotFoundException("Product not found");
        }
        return productMediaMapper.listByProductId(productId);
    }

    @Override
    public String getMainThumbnail(Long productId) {
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new NotFoundException("Product not found");
        }
        return product.getThumbnailUrl();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String setMainThumbnail(Long productId, Long mediaId) {
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new NotFoundException("Product not found");
        }

        ProductMedia media = productMediaMapper.findByIdAndProductId(mediaId, productId);
        if (media == null) {
            throw new NotFoundException("Thumbnail not found");
        }

        updateMainThumbnail(productId, media.getUrl());
        return media.getUrl();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String deleteThumbnail(Long productId, Long mediaId) {
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new NotFoundException("Product not found");
        }

        ProductMedia media = productMediaMapper.findByIdAndProductId(mediaId, productId);
        if (media == null) {
            throw new NotFoundException("Thumbnail not found");
        }

        int rows = productMediaMapper.deleteByIdAndProductId(mediaId, productId);
        if (rows != 1) {
            throw new RuntimeException("Failed to delete thumbnail");
        }

        String currentMainUrl = product.getThumbnailUrl();
        if (currentMainUrl != null && currentMainUrl.equals(media.getUrl())) {
            ProductMedia first = productMediaMapper.findFirstByProductId(productId);
            String newMainUrl = first == null ? null : first.getUrl();
            updateMainThumbnail(productId, newMainUrl);
            return newMainUrl;
        }

        return currentMainUrl;
    }

    private void updateMainThumbnail(Long productId, String url) {
        int rows = productMapper.updateThumbnailUrl(productId, url);
        if (rows != 1) {
            throw new RuntimeException("Failed to update main thumbnail");
        }
    }

    private void checkUploadFiles(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new BadRequestException("Invalid request data", "files", "At least one image file is required");
        }

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                throw new BadRequestException("Invalid request data", "files", "File cannot be empty");
            }

            String name = file.getOriginalFilename();
            String ext = extractExtension(name);
            if (!isAllowedImageExtension(ext)) {
                throw new BadRequestException("Invalid request data", "files", "Only jpg/jpeg/png/webp allowed");
            }
        }
    }

    private String extractExtension(String fileName) {
        if (fileName == null) {
            throw new BadRequestException("Invalid request data", "files", "File name is missing");
        }

        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            throw new BadRequestException("Invalid request data", "files", "File extension is required");
        }

        return fileName.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private boolean isAllowedImageExtension(String ext) {
        if ("jpg".equals(ext)) {
            return true;
        }
        if ("jpeg".equals(ext)) {
            return true;
        }
        if ("png".equals(ext)) {
            return true;
        }
        return "webp".equals(ext);
    }
}
