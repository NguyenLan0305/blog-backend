package com.group.blog.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level= AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    String id;
    String username;
    String email;
    String bio;
    String avatarUrl;
    Set<String> roles;
    LocalDateTime createdAt;
    long totalFollowers;
    long totalFollowing;
    boolean isFollowedByCurrentUser; // kiểm tra xem user hiện tại theo dõi user nào đó không
}
