package com.group.blog.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level= AccessLevel.PRIVATE)
public class AuthenticationResponse {
    String token;
    boolean authenticated;// xác định user có đăng nhập thành công hay không
    Date expiryTime;
}
