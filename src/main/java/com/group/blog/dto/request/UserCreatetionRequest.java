package com.group.blog.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level= AccessLevel.PRIVATE)
    public class UserCreatetionRequest {
        @NotBlank(message = "USERNAME_REQUIRED")
        @Size(min = 3, message = "USERNAME_INVALID")
        String username;

        @NotBlank(message = "PASSWORD_REQUIRED")
        @Size(min = 8, message = "INVALID_PASSWORD")
        String password;
    }

