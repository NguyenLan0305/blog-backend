package com.group.blog.controller;

import com.group.blog.dto.request.ApiResponse;
import com.group.blog.dto.response.NotificationResponse;
import com.group.blog.service.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {
    NotificationService notificationService;

    @GetMapping
    public ApiResponse<List<NotificationResponse>> getMyNotifications() {
        return ApiResponse.<List<NotificationResponse>>builder().result(notificationService.getMyNotifications()).build();
    }

    @PutMapping("/{id}/read")
    public ApiResponse<String> markAsRead(@PathVariable UUID id) {
        notificationService.markAsRead(id);
        return ApiResponse.<String>builder().result("Đã đánh dấu đọc").build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteNotification(@PathVariable UUID id) {
        notificationService.deleteNotification(id);
        return ApiResponse.<String>builder().result("Đã xóa thông báo").build();
    }
}