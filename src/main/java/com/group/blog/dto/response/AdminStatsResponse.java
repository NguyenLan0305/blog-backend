package com.group.blog.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminStatsResponse {
    long totalUsers;
    long totalPosts;
    List<BlogResponse> recentPosts;
}