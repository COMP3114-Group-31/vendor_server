package com.mpu.vendor.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.mpu.vendor.entity.ProductMedia;

public interface ThumbnailService {

    String uploadCover(Long productId, MultipartFile file);

    List<ProductMedia> uploadThumbnails(Long productId, MultipartFile[] files, boolean setFirstAsMain);

    List<ProductMedia> listThumbnails(Long productId);

    String getMainThumbnail(Long productId);

    String setMainThumbnail(Long productId, Long mediaId);

    String deleteThumbnail(Long productId, Long mediaId);
}
