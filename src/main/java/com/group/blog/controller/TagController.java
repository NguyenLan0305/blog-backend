package com.group.blog.controller;

import com.group.blog.dto.request.ApiResponse;
import com.group.blog.dto.response.TagResponse;
import com.group.blog.entity.Tag;
import com.group.blog.repository.TagRepository;
import com.group.blog.service.TagService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TagController {
    TagService tagService;

    @GetMapping
    public ApiResponse<List<TagResponse>> getAll() {
        return ApiResponse.<List<TagResponse>>builder()
                .result(tagService.getAllTags())
                .build();
    }

    @PostMapping
    public ApiResponse<TagResponse> createTag(@RequestBody com.group.blog.dto.request.CategoryRequest request) {
        return ApiResponse.<TagResponse>builder()
                .result(tagService.create(request))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<TagResponse> update(@PathVariable java.util.UUID id, @RequestBody com.group.blog.dto.request.CategoryRequest request) {
        return ApiResponse.<TagResponse>builder().result(tagService.update(id, request)).build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable java.util.UUID id) {
        tagService.delete(id);
        return ApiResponse.<Void>builder().build();
    }
}
