package com.mpu.vendor.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.mpu.vendor.entity.Product;

@Mapper
public interface ProductMapper {

    @Select({
            "<script>",
            "SELECT id, name, name_cn, description, description_cn, price, cover_image_url, category, status, created_at, updated_at",
            "FROM products",
            "<where>",
            "  <if test=\"search != null and search != ''\">",
            "    (name LIKE CONCAT('%', #{search}, '%')",
            "     OR name_cn LIKE CONCAT('%', #{search}, '%'))",
            "  </if>",
            "  <if test=\"status != null and status != ''\">",
            "    AND status = #{status}",
            "  </if>",
            "</where>",
            "ORDER BY created_at DESC",
            "</script>"
    })
    @Results({
            @Result(column = "name_cn", property = "nameCn"),
            @Result(column = "description_cn", property = "descriptionCn"),
            @Result(column = "cover_image_url", property = "coverImageUrl"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt")
    })
    List<Product> listProducts(@Param("search") String search, @Param("status") String status);

    @Select("SELECT id, name, name_cn, description, description_cn, price, cover_image_url, category, status, created_at, updated_at "
            + "FROM products WHERE id = #{id}")
    @Results({
            @Result(column = "name_cn", property = "nameCn"),
            @Result(column = "description_cn", property = "descriptionCn"),
            @Result(column = "cover_image_url", property = "coverImageUrl"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt")
    })
    Product findById(@Param("id") Long id);

    @Insert("INSERT INTO products (name, name_cn, description, description_cn, price, cover_image_url, category) "
            + "VALUES (#{name}, #{nameCn}, #{description}, #{descriptionCn}, #{price}, #{coverImageUrl}, #{category})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);

    @Update({
            "<script>",
            "UPDATE products",
            "<set>",
            "  <if test='name != null'>name = #{name},</if>",
            "  <if test='nameCn != null'>name_cn = #{nameCn},</if>",
            "  <if test='description != null'>description = #{description},</if>",
            "  <if test='descriptionCn != null'>description_cn = #{descriptionCn},</if>",
            "  <if test='price != null'>price = #{price},</if>",
            "  <if test='coverImageUrl != null'>cover_image_url = #{coverImageUrl},</if>",
            "  <if test='category != null'>category = #{category},</if>",
            "</set>",
            "WHERE id = #{id}",
            "</script>"
    })
    int update(Product product);

    @Update("UPDATE products SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Delete("DELETE FROM products WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Update("UPDATE products SET cover_image_url = #{coverImageUrl} WHERE id = #{id}")
    int updateCoverImageUrl(@Param("id") Long id, @Param("coverImageUrl") String coverImageUrl);
}
