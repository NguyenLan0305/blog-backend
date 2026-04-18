package com.group.blog.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

    @Data
    @Builder
    public class BlogSuggestionResponse {
        UUID id;
        String title;
        String slug;
        String authorName;
        String categoryName;
    }

