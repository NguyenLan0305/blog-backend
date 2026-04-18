package com.group.blog.controller;

import com.group.blog.dto.request.ApiResponse;
import com.group.blog.dto.request.CategoryRequest;
import com.group.blog.dto.response.CategoryResponse;
import com.group.blog.entity.Category;
import com.group.blog.repository.CategoryRepository;
import com.group.blog.service.CategoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryController {
    CategoryService categoryService;

    @GetMapping
    public ApiResponse<List<CategoryResponse>> getAll() {
        return ApiResponse.<List<CategoryResponse>>builder()
                .result(categoryService.getAllCategories())
                .build();
    }

    @PostMapping
    public ApiResponse<Category> create(@RequestBody CategoryRequest request) {
        return ApiResponse.<Category>builder()
                .result(categoryService.create(request))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> update(@PathVariable java.util.UUID id, @RequestBody CategoryRequest request) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable java.util.UUID id) {
        categoryService.delete(id);
        return ApiResponse.<Void>builder().build();
    }
}
