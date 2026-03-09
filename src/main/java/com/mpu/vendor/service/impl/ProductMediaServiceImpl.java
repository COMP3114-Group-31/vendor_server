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
import com.mpu.vendor.service.ProductMediaService;
import com.mpu.vendor.utils.AliyunOSSOperator;

@Service
public class ProductMediaServiceImpl implements ProductMediaService {

    private final ProductMapper productMapper;
    private final ProductMediaMapper productMediaMapper;
    private final AliyunOSSOperator aliyunOSSOperator;

    public ProductMediaServiceImpl(ProductMapper productMapper,
                                   ProductMediaMapper productMediaMapper,
                                   AliyunOSSOperator aliyunOSSOperator) {
        this.productMapper = productMapper;
        this.productMediaMapper = productMediaMapper;
        this.aliyunOSSOperator = aliyunOSSOperator;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String uploadThumbnail(Long productId, MultipartFile file) {
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new NotFoundException("Product not found");
        }

        checkUploadFile(file, "file");

        try {
            String originalFilename = file.getOriginalFilename();
            String url = aliyunOSSOperator.uploadProductCover(file.getBytes(), originalFilename, productId);
            updateMainThumbnail(productId, url);
            return url;
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            String msg = ex.getMessage();
            if (msg == null || msg.isBlank()) {
                msg = ex.getClass().getSimpleName();
            }
            throw new RuntimeException("Failed to upload cover image: " + msg, ex);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<ProductMedia> uploadMedia(Long productId, MultipartFile[] files, boolean setFirstAsMain) {
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new NotFoundException("Product not found");
        }

        checkUploadFiles(files);

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
                    throw new RuntimeException("Failed to save product media record");
                }
            } catch (BadRequestException ex) {
                throw ex;
            } catch (Exception ex) {
                String msg = ex.getMessage();
                if (msg == null || msg.isBlank()) {
                    msg = ex.getClass().getSimpleName();
                }
                throw new RuntimeException("Failed to upload product media: " + msg, ex);
            }
        }

        return productMediaMapper.listByProductId(productId);
    }

    @Override
    public List<ProductMedia> listMedia(Long productId) {
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new NotFoundException("Product not found");
        }
        return productMediaMapper.listByProductId(productId);
    }

    @Override
    public String getThumbnail(Long productId) {
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new NotFoundException("Product not found");
        }
        return product.getThumbnailUrl();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String setThumbnailFromMedia(Long productId, Long mediaId) {
        ProductMedia media = productMediaMapper.findByIdAndProductId(mediaId, productId);
        if (media == null) {
            throw new NotFoundException("Product media not found");
        }

        throw new BadRequestException(
                "Invalid request data",
                "media_id",
                "Detail images are stored in product_media. Use /products/{productId}/images/cover to update thumbnail_url");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String deleteMedia(Long productId, Long mediaId) {
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new NotFoundException("Product not found");
        }

        ProductMedia media = productMediaMapper.findByIdAndProductId(mediaId, productId);
        if (media == null) {
            throw new NotFoundException("Product media not found");
        }

        int rows = productMediaMapper.deleteByIdAndProductId(mediaId, productId);
        if (rows != 1) {
            throw new RuntimeException("Failed to delete product media");
        }

        return product.getThumbnailUrl();
    }

    private void updateMainThumbnail(Long productId, String url) {
        int rows = productMapper.updateThumbnailUrl(productId, url);
        if (rows != 1) {
            throw new RuntimeException("Failed to update cover image");
        }
    }

    private void checkUploadFiles(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new BadRequestException("Invalid request data", "files", "At least one image file is required");
        }

        for (MultipartFile file : files) {
            checkUploadFile(file, "files");
        }
    }

    private void checkUploadFile(MultipartFile file, String field) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Invalid request data", field, "File cannot be empty");
        }

        String name = file.getOriginalFilename();
        String ext = extractExtension(name, field);
        if (!isAllowedImageExtension(ext)) {
            throw new BadRequestException("Invalid request data", field, "Only jpg/jpeg/png/webp allowed");
        }
    }

    private String extractExtension(String fileName, String field) {
        if (fileName == null) {
            throw new BadRequestException("Invalid request data", field, "File name is missing");
        }

        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            throw new BadRequestException("Invalid request data", field, "File extension is required");
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
