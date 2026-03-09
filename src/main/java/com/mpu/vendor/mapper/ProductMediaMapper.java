package com.mpu.vendor.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.mpu.vendor.entity.ProductMedia;

@Mapper
public interface ProductMediaMapper {

    @Insert("INSERT INTO product_media (product_id, media_type, url) VALUES (#{productId}, #{mediaType}, #{url})")
    @Options(useGeneratedKeys = true, keyProperty = "mediaId")
    int insert(ProductMedia media);

    @Select("SELECT media_id, product_id, media_type, url, created_at, updated_at FROM product_media WHERE product_id = #{productId} ORDER BY created_at ASC, media_id ASC")
    List<ProductMedia> listByProductId(@Param("productId") Long productId);

    @Select("SELECT media_id, product_id, media_type, url, created_at, updated_at FROM product_media WHERE media_id = #{mediaId} AND product_id = #{productId}")
    ProductMedia findByIdAndProductId(@Param("mediaId") Long mediaId, @Param("productId") Long productId);

    @Select("SELECT media_id, product_id, media_type, url, created_at, updated_at FROM product_media WHERE product_id = #{productId} ORDER BY created_at ASC, media_id ASC LIMIT 1")
    ProductMedia findFirstByProductId(@Param("productId") Long productId);

    @Delete("DELETE FROM product_media WHERE media_id = #{mediaId} AND product_id = #{productId}")
    int deleteByIdAndProductId(@Param("mediaId") Long mediaId, @Param("productId") Long productId);

    @Delete("DELETE FROM product_media WHERE product_id = #{productId}")
    int deleteByProductId(@Param("productId") Long productId);
}
