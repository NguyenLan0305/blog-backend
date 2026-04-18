package com.group.blog.controller;
import com.group.blog.dto.request.ApiResponse;
import com.group.blog.dto.request.CommentRequest;
import com.group.blog.dto.response.BlogResponse;
import com.group.blog.dto.response.CommentResponse;
import com.group.blog.service.InteractionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/blogs") // Hoặc bạn có thể tách ra thành /comments riêng
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InteractionController {

    InteractionService interactionService;

    // --- LIKE ---
    @PostMapping("/{blogId}/like")
    public ApiResponse<String> toggleLike(@PathVariable UUID blogId) {
        interactionService.toggleLike(blogId);
        return ApiResponse.<String>builder()
                .result("Toggled like successfully")
                .build();
    }

    // --- COMMENT ---
    @GetMapping("/{blogId}/comments")
    public ApiResponse<List<CommentResponse>> getComments(@PathVariable UUID blogId) {
        return ApiResponse.<List<CommentResponse>>builder()
                .result(interactionService.getCommentsByBlogId(blogId))
                .build();
    }

    @PostMapping("/{blogId}/comments")
    public ApiResponse<CommentResponse> addComment(@PathVariable UUID blogId, @RequestBody CommentRequest request) {
        return ApiResponse.<CommentResponse>builder()
                .result(interactionService.addComment(blogId, request))
                .build();
    }

    @DeleteMapping("/comments/{commentId}")
    public ApiResponse<String> deleteComment(@PathVariable UUID commentId) {
        interactionService.deleteComment(commentId);
        return ApiResponse.<String>builder()
                .result("Comment deleted successfully")
                .build();
    }
    @PostMapping("/{blogId}/bookmark")
    public ApiResponse<String> toggleBookmark(@PathVariable UUID blogId) {
        interactionService.toggleBookmark(blogId);
        return ApiResponse.<String>builder().result("Toggled bookmark successfully").build();
    }

    @GetMapping("/bookmarks") // API lấy danh sách bài đã lưu
    public ApiResponse<List<BlogResponse>> getMyBookmarks() {
        return ApiResponse.<List<BlogResponse>>builder().result(interactionService.getMyBookmarks()).build();
    }
}