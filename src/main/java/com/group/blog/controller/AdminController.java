package com.group.blog.controller;

import com.group.blog.dto.request.ApiResponse;
import com.group.blog.dto.response.AdminStatsResponse;
import com.group.blog.service.AdminService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminController {

    AdminService adminService;

    @GetMapping("/stats")
    public ApiResponse<AdminStatsResponse> getDashboardStats() {
        return ApiResponse.<AdminStatsResponse>builder()
                .result(adminService.getDashboardStats())
                .build();
    }
}