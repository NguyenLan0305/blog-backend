package com.group.blog.service;

import com.group.blog.dto.request.CategoryRequest;
import com.group.blog.dto.response.CategoryResponse;
import com.group.blog.entity.Category;
import com.group.blog.exception.AppException;
import com.group.blog.exception.ErrorCode;
import com.group.blog.mapper.CategoryMapper;
import com.group.blog.repository.CategoryRepository;
import com.group.blog.util.SlugUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryService {
    CategoryRepository categoryRepository;
    CategoryMapper categoryMapper;

    @Transactional
    public Category create(CategoryRequest request){
        if(categoryRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.CATEGORY_EXITED);
        }
        Category category = categoryMapper.toCategory(request);
        return categoryRepository.save(category);
    }

    @Transactional
    public CategoryResponse update(java.util.UUID id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category does not exist"));

        // Kiểm tra xem tên mới có bị trùng với category khác không
        if (!category.getName().equals(request.getName()) && categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("This Category already exists");
        }

        category.setName(request.getName());
        category = categoryRepository.save(category);

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(SlugUtils.generateSlug(category.getName()) + "-" + category.getId().toString())
                .postCount(0L) // Chỗ này thực tế cần query đếm số lượng, tạm để 0
                .build();
    }

    @Transactional
    public void delete(java.util.UUID id) {
        if (!categoryRepository.existsById(id)) throw new RuntimeException("Category not found");
        categoryRepository.deleteById(id);
    }

    public List<CategoryResponse> getAllCategories() {
        // 1. Gọi hàm tối ưu lấy 1 lần duy nhất từ DB
        List<Object[]> results = categoryRepository.findAllCategoriesWithPostCount();

        // 2. Map dữ liệu Object[] -> DTO
        return results.stream()
                .map(row -> {
                    Category category = (Category) row[0];
                    long count = (long) row[1];

                    return CategoryResponse.builder()
                            .id(category.getId())
                            .name(category.getName())
                            // 🔥 TẠO SLUG "ON-THE-FLY" KÈM ID (Vì 3NF không lưu slug)
                            .slug(SlugUtils.generateSlug(category.getName()) + "-" + category.getId().toString())
                            .postCount(count)
                            .build();
                })
                .toList();
    }
}