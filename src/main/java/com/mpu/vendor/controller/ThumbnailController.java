package com.mpu.vendor.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mpu.vendor.entity.ProductMedia;
import com.mpu.vendor.service.ThumbnailService;

@RestController
@RequestMapping({"/products/{productId}/images", "/products/{productId}/thumbnails"})
@CrossOrigin(origins = "*")
public class ThumbnailController {

    private final ThumbnailService thumbnailService;

    public ThumbnailController(ThumbnailService thumbnailService) {
        this.thumbnailService = thumbnailService;
    }

    @PostMapping("/cover")
    public ResponseEntity<Map<String, Object>> uploadCover(@PathVariable Long productId,
                                                           @RequestParam("file") MultipartFile file) {
        String coverImageUrl = thumbnailService.uploadCover(productId, file);

        Map<String, Object> map = new HashMap<>();
        map.put("message", "Cover image uploaded");
        map.put("product_id", productId);
        map.put("cover_image_url", coverImageUrl);
        map.put("main_thumbnail_url", coverImageUrl);
        return ResponseEntity.status(HttpStatus.CREATED).body(map);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> upload(@PathVariable Long productId,
                                                       @RequestParam("files") MultipartFile[] files,
                                                       @RequestParam(name = "set_first_as_main", defaultValue = "true") boolean setFirstAsMain) {
        List<ProductMedia> list = thumbnailService.uploadThumbnails(productId, files, setFirstAsMain);

        Map<String, Object> map = new HashMap<>();
        map.put("message", "Images uploaded");
        map.put("product_id", productId);
        String coverImageUrl = thumbnailService.getMainThumbnail(productId);
        map.put("cover_image_url", coverImageUrl);
        map.put("main_thumbnail_url", coverImageUrl);
        map.put("items", toItems(list));
        map.put("detail_images", toItems(list));

        return ResponseEntity.status(HttpStatus.CREATED).body(map);
    }

    @GetMapping
    public Map<String, Object> list(@PathVariable Long productId) {
        List<ProductMedia> list = thumbnailService.listThumbnails(productId);

        Map<String, Object> map = new HashMap<>();
        map.put("product_id", productId);
        String coverImageUrl = thumbnailService.getMainThumbnail(productId);
        map.put("cover_image_url", coverImageUrl);
        map.put("main_thumbnail_url", coverImageUrl);
        map.put("items", toItems(list));
        map.put("detail_images", toItems(list));
        return map;
    }

    @PatchMapping("/{mediaId}/main")
    public Map<String, Object> setMain(@PathVariable Long productId,
                                       @PathVariable Long mediaId) {
        String url = thumbnailService.setMainThumbnail(productId, mediaId);

        Map<String, Object> map = new HashMap<>();
        map.put("message", "Cover image updated");
        map.put("product_id", productId);
        map.put("cover_image_url", url);
        map.put("main_thumbnail_url", url);
        return map;
    }

    @DeleteMapping("/{mediaId}")
    public Map<String, Object> delete(@PathVariable Long productId,
                                      @PathVariable Long mediaId) {
        String mainUrl = thumbnailService.deleteThumbnail(productId, mediaId);

        Map<String, Object> map = new HashMap<>();
        map.put("message", "Image deleted");
        map.put("product_id", productId);
        map.put("cover_image_url", mainUrl);
        map.put("main_thumbnail_url", mainUrl);
        return map;
    }

    private List<Map<String, Object>> toItems(List<ProductMedia> list) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (ProductMedia item : list) {
            Map<String, Object> one = new HashMap<>();
            one.put("media_id", item.getMediaId());
            one.put("url", item.getUrl());
            one.put("media_type", item.getMediaType());
            one.put("created_at", item.getCreatedAt());
            items.add(one);
        }
        return items;
    }
}
