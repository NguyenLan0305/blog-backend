package com.group.blog.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentResponse {
    UUID id;
    String content;
    LocalDateTime createdAt;

    // Trả về thông tin người bình luận (avatar, username)
    UserResponse user;

    // Danh sách các câu trả lời (Reply) thuộc về comment này
    List<CommentResponse> replies;
}