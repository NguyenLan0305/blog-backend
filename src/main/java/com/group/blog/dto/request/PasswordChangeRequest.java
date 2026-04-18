package com.group.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PasswordChangeRequest {

    @NotBlank(message = "OLD_PASSWORD_REQUIRED")
    String oldPassword;

    @Size(min = 8, message = "INVALID_PASSWORD")
    String newPassword;

    // Optional: Có thể thêm trường này để validate 2 mật khẩu khớp nhau ngay từ DTO
    @NotBlank(message = "CONFIRM_PASSWORD_REQUIRED")
    String confirmPassword;
}