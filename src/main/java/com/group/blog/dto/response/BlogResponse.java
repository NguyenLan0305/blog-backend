package com.group.blog.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BlogResponse {

    UUID id;
    String title;
    String slug;
    String banner;
    String description;
    String content;

    boolean draft;
    String draftContent;
    String draftBanner;

    long totalLikes;
    long totalComments;
    long totalReads;

    LocalDateTime publishedAt;
    LocalDateTime updatedAt;
    LocalDateTime createdAt;

    UserResponse author;

    CategoryResponse category;

    Set<TagResponse> tags;

    //user hiện tại đã like bài này chưa
    boolean isLikedByCurrentUser;
    //user hiện tại đã lưu bài này chưa
    boolean isBookmarkedByCurrentUser;
}