package com.mpu.vendor.mapper;

import com.mpu.vendor.entity.Product;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductMapper {

    @Select({
        "<script>",
        "SELECT id, name, name_cn, price, thumbnail_url, status, created_at, updated_at",
        "FROM products",
        "<where>",
        "  <if test=\"search != null and search != ''\">",
        "    (name LIKE CONCAT('%', #{search}, '%')",
        "     OR name_cn LIKE CONCAT('%', #{search}, '%'))",
        "  </if>",
        "  <if test=\"status != null and status != ''\">",
        "    <if test=\"search != null and search != ''\">AND</if>",
        "    status = #{status}",
        "  </if>",
        "</where>",
        "ORDER BY created_at DESC",
        "</script>"
    })
    @Results({
        @Result(column = "name_cn", property = "nameCn"),
        @Result(column = "thumbnail_url", property = "thumbnailUrl"),
        @Result(column = "created_at", property = "createdAt"),
        @Result(column = "updated_at", property = "updatedAt")
    })
    List<Product> listProducts(@Param("search") String search, @Param("status") String status);

    @Select("SELECT id, name, name_cn, price, thumbnail_url, status, created_at, updated_at FROM products WHERE id = #{id}")
    @Results({
        @Result(column = "name_cn", property = "nameCn"),
        @Result(column = "thumbnail_url", property = "thumbnailUrl"),
        @Result(column = "created_at", property = "createdAt"),
        @Result(column = "updated_at", property = "updatedAt")
    })
    Product findById(@Param("id") Integer id);

    @Insert("INSERT INTO products (name, name_cn, price, thumbnail_url, status, created_at, updated_at) " +
            "VALUES (#{name}, #{nameCn}, #{price}, #{thumbnailUrl}, #{status}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);

    @Update({
        "<script>",
        "UPDATE products",
        "<set>",
        "  <if test='name != null'>name = #{name},</if>",
        "  <if test='nameCn != null'>name_cn = #{nameCn},</if>",
        "  <if test='price != null'>price = #{price},</if>",
        "  updated_at = #{updatedAt}",
        "</set>",
        "WHERE id = #{id}",
        "</script>"
    })
    int update(Product product);

    @Update("UPDATE products SET status = #{status}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateStatus(@Param("id") Integer id, @Param("status") String status, @Param("updatedAt") java.time.LocalDateTime updatedAt);

    @Delete("DELETE FROM products WHERE id = #{id}")
    int deleteById(@Param("id") Integer id);
}
