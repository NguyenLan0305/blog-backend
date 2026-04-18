package com.group.blog.service;

import com.group.blog.dto.response.AdminStatsResponse;
import com.group.blog.dto.response.BlogResponse;
import com.group.blog.repository.BlogRepository;
import com.group.blog.repository.UserRepository;
import com.group.blog.mapper.BlogMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminService {

    UserRepository userRepository;
    BlogRepository blogRepository;
    BlogMapper blogMapper; // Dùng mapper bạn đang có để chuyển Entity sang DTO

    public AdminStatsResponse getDashboardStats() {
        // Đếm tổng user
        long totalUsers = userRepository.count();

        // Đếm tổng bài viết đã xuất bản (không tính bản nháp)
        long totalPosts = blogRepository.countByDraftFalse();

        // Lấy 5 bài viết mới nhất
        List<BlogResponse> recentPosts = blogRepository.findTop5ByDraftFalseOrderByCreatedAtDesc()
                .stream()
                .map(blogMapper::toBlogResponse) // Sử dụng Mapper có sẵn của bạn
                .collect(Collectors.toList());

        return AdminStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalPosts(totalPosts)
                .recentPosts(recentPosts)
                .build();
    }
}