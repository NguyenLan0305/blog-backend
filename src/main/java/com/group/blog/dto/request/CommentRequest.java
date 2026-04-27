package com.group.blog.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentRequest {
    String content;
    // Nếu là comment gốc thì truyền null, nếu là Reply thì truyền ID của comment cha vào đây
    UUID parentId;
}