package com.group.blog.controller;

import com.group.blog.dto.request.ApiResponse;
import com.group.blog.dto.request.AuthenticationRequest;
import com.group.blog.dto.request.IntrospectRequest;
import com.group.blog.dto.response.AuthenticationResponse;
import com.group.blog.dto.response.IntrospectResponse;
import com.group.blog.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;
    @PostMapping("/login")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest authenticationRequest, HttpServletResponse response){
        //gọi service tạo token
        var result = authenticationService.authenticate(authenticationRequest);
        //Lấy token ra và bọc vào Cookie
        ResponseCookie springCookie = ResponseCookie.from("accessToken", result.getToken())
                .httpOnly(true)       // Chặn Javascript lấy cookie (Chống XSS)
                .secure(true)         // Yêu cầu HTTPS
                .path("/")            // Cookie có tác dụng ở mọi endpoint
                .maxAge(60 * 60)      // Thời gian sống 1 giờ
                // Frontend và Backend khác domain (vd Frontend Vercel, Backend Render) -> Dùng "None"
                // Nếu chạy chung 1 domain hoặc localhost -> Dùng "Lax" hoặc "Strict"
                .sameSite("None")
                .build();

        // Gắn Cookie vào Header của Response
        response.addHeader(HttpHeaders.SET_COOKIE, springCookie.toString());

        // Xóa token khỏi body trả về để không lộ token ra ngoài JSON
        result.setToken(null);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }
    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request){
        var result=authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/logout")
    ApiResponse<String> logout(HttpServletResponse response) {
        // Tạo một cookie trùng tên nhưng thời gian sống = 0 để trình duyệt tự hủy nó
        ResponseCookie cleanCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0) // Xóa ngay lập tức
                .sameSite("None")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cleanCookie.toString());

        return ApiResponse.<String>builder()
                .result("Đăng xuất thành công")
                .build();
    }
}
