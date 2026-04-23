package com.group.blog.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level= AccessLevel.PRIVATE)
public class UserUpdateRequest {
    @Email(message = "EMAIL_INVALID")
    String email;

    String bio;
    String avatarUrl;

    // admin mới sửa được role của người dùng
    Set<String> roles;
}
