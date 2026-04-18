package com.group.blog.mapper;

import com.group.blog.dto.request.BlogCreationRequest;
import com.group.blog.dto.request.BlogUpdateRequest;
import com.group.blog.dto.response.BlogResponse;
import com.group.blog.entity.Blog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BlogMapper {

    // 1. Map từ Request sang Entity (Bỏ qua các quan hệ, Service sẽ tự xử lý)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "author", ignore = true) // Tác giả sẽ được lấy từ Token đăng nhập
    Blog toBlog(BlogCreationRequest request);

    // 2. Map từ Entity sang Response (MapStruct tự động map các trường trùng tên)
    BlogResponse toBlogResponse(Blog blog);

    // 3. Update Entity từ Request (Bỏ qua các quan hệ)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "tags", ignore = true)
    void updateBlog(@MappingTarget Blog blog, BlogUpdateRequest request);
}