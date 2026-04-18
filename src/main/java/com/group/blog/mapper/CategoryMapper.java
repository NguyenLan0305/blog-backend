package com.group.blog.mapper;

import com.group.blog.dto.request.CategoryRequest;
import com.group.blog.dto.response.CategoryResponse;
import com.group.blog.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    // 1. Map từ Request sang Entity (Bỏ qua các quan hệ, Service sẽ tự xử lý)
    @Mapping(target = "blogs", ignore = true)
    Category toCategory(CategoryRequest request);

    CategoryResponse toCategoryResponse(Category category);
}

