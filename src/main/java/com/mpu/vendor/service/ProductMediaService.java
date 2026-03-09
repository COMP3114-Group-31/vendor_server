package com.mpu.vendor.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.mpu.vendor.entity.ProductMedia;

public interface ProductMediaService {

    String uploadThumbnail(Long productId, MultipartFile file);

    List<ProductMedia> uploadMedia(Long productId, MultipartFile[] files, boolean setFirstAsMain);

    List<ProductMedia> listMedia(Long productId);

    String getThumbnail(Long productId);

    String setThumbnailFromMedia(Long productId, Long mediaId);

    String deleteMedia(Long productId, Long mediaId);
}
