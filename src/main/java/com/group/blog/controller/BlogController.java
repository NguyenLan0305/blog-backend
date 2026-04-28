package com.group.blog.controller;

import com.group.blog.dto.request.ApiResponse;
import com.group.blog.dto.request.BlogCreationRequest;
import com.group.blog.dto.request.BlogUpdateRequest;
import com.group.blog.dto.response.BlogResponse;
import com.group.blog.dto.response.BlogSuggestionResponse;
import com.group.blog.service.BlogService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;
@RestController
@RequestMapping("/blogs") // Định tuyến gốc cho tất cả API trong này
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BlogController {

    BlogService blogService;

    // 1. Tạo bài viết mới
    @PostMapping
    public ApiResponse<BlogResponse> createBlog(@RequestBody @Valid BlogCreationRequest request) {
        return ApiResponse.<BlogResponse>builder()
                .result(blogService.createBlog(request))
                .build();
    }

    // 2. Lấy danh sách toàn bộ bài viết
    @GetMapping
    public ApiResponse<List<BlogResponse>> getAllBlogs() {
        return ApiResponse.<List<BlogResponse>>builder()
                .result(blogService.getAllBlogs())
                .build();
    }

    // 3. Lấy chi tiết 1 bài viết theo ID
    @GetMapping("/{id}")
    public ApiResponse<BlogResponse> getBlogById(
            @PathVariable UUID id, HttpServletRequest request) { // Thêm request vào đây

        // Logic lấy IP (hỗ trợ cả khi chạy qua Load Balancer/Nginx)
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        // Gọi Service và truyền thêm ipAddress
        return ApiResponse.<BlogResponse>builder()
                .result(blogService.getBlogById(id, ipAddress))
                .build();
    }

    // 4. Cập nhật bài viết
    @PutMapping("/{id}")
    public ApiResponse<BlogResponse> updateBlog(
            @PathVariable UUID id,
            @RequestBody @Valid BlogUpdateRequest request) {
        return ApiResponse.<BlogResponse>builder()
                .result(blogService.updateBlog(id, request))
                .build();
    }

    // 5. Xóa bài viết
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteBlog(@PathVariable UUID id) {
        blogService.deleteBlog(id);
        return ApiResponse.<String>builder()
                .result("Bài viết đã được xóa thành công")
                .build();
    }

    @GetMapping("/category/{categoryId}")
    public ApiResponse<List<BlogResponse>> getBlogsByCategory(@PathVariable UUID categoryId) {
        return ApiResponse.<List<BlogResponse>>builder()
                .result(blogService.getBlogsByCategory(categoryId))
                .build();
    }

    @GetMapping("/tag/{tagId}")
    public ApiResponse<List<BlogResponse>> getBlogsByTag(@PathVariable UUID tagId) {
        return ApiResponse.<List<BlogResponse>>builder()
                .result(blogService.getBlogsByTag(tagId))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<List<BlogResponse>> searchBlogs(@RequestParam("keyword") String keyword) {
        return ApiResponse.<List<BlogResponse>>builder()
                .result(blogService.searchBlogs(keyword))
                .build();
    }

    @GetMapping("/search/suggestions")
    public ApiResponse<List<BlogSuggestionResponse>> getSuggestions(@RequestParam("keyword") String keyword) {
        return ApiResponse.<List<BlogSuggestionResponse>>builder()
                .result(blogService.getSearchSuggestions(keyword))
                .build();
    }


    @GetMapping("/filter")
    public ApiResponse<List<BlogResponse>> filterBlogs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID categoryId) {
        return ApiResponse.<List<BlogResponse>>builder()
                .result(blogService.filterBlogs(keyword, categoryId))
                .build();
    }

    // 6. Lấy danh sách bài viết của TÔI (người đang đăng nhập)
    @GetMapping("/my-blogs")
    public ApiResponse<List<BlogResponse>> getMyBlogs() {
        return ApiResponse.<List<BlogResponse>>builder()
                .result(blogService.getMyBlogs())
                .build();
    }

    // 7. Lấy bài viết công khai của một tác giả
    @GetMapping("/user/{username}")
    public ApiResponse<List<BlogResponse>> getBlogsByUsername(@PathVariable String username) {
        return ApiResponse.<List<BlogResponse>>builder()
                .result(blogService.getPublishedBlogsByUsername(username))
                .build();
    }
}