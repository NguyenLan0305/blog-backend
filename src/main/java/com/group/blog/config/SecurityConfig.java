package com.group.blog.config;

import com.group.blog.enums.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Các API dùng phương thức POST được phép truy cập tự do (Đăng ký, Đăng nhập, Xác thực Token)
    private final String[] PUBLIC_POST_ENDPOINTS = {
            "/users",
            "/auth/login",
            "/auth/introspect"
    };

    // Các API và View dùng phương thức GET được phép truy cập tự do
    private final String[] PUBLIC_GET_ENDPOINTS = {
            "/assets/**", "/css/**", "/js/**",         // Tài nguyên tĩnh (frontend)
            "/fragments/**",                           // Các file html tĩnh (sidebar, navbar...)

            // --- CÁC ROUTE GIAO DIỆN (VIEW) CÔNG KHAI ---
            "/", "/home", "/login", "/register", "/forgot-password",

            // --- CÁC API DỮ LIỆU CÔNG KHAI ---
            "/categories", "/tags",                    // Danh mục, Thẻ
            "/blogs",                                  // Lấy danh sách blog
            "/blogs/{id:[0-9a-fA-F\\-]{36}}",          // Xem chi tiết blog
            "/blogs/category/**",                      // Lọc theo danh mục
            "/blogs/tag/**",                           // Lọc theo tag
            "/blogs/search/**",                        // Tìm kiếm & gợi ý
            "/blogs/filter",                           // Lọc đa luồng
            "/blogs/user/**",                          // Xem bài viết của 1 tác giả
            "/users/profile/**",                       // Xem thông tin profile của 1 tác giả
            "/blogs/*/comments",                       // Đọc danh sách comment của bài viết
            "/users/*/followers",                      // Xem Followers
            "/users/*/following"                       // Xem Following
    };

    // Định nghĩa riêng các Endpoint dành cho khu vực ADMIN
    private final String[] ADMIN_ENDPOINTS = {
            "/api/admin/**", // Các REST API phục vụ cho trang Dashboard
            "/admin/**"      // Các route View trả về file HTML Admin
    };

    @Value("${jwt.signerKey}")
    private String signerKey;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .cors(org.springframework.security.config.Customizer.withDefaults())
                .authorizeHttpRequests(request -> request
                        // 1. CÁC API VÀ VIEW PUBLIC (AI CŨNG VÀO ĐƯỢC)
                        .requestMatchers(HttpMethod.POST, PUBLIC_POST_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_ENDPOINTS).permitAll()

                        // 2. CÁC API ĐẶC THÙ YÊU CẦU QUYỀN ADMIN
                        .requestMatchers(ADMIN_ENDPOINTS).hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/users").hasRole(Role.ADMIN.name())

                        // Chặn quyền Thêm/Sửa/Xóa Categories & Tags chỉ dành cho Admin
                        .requestMatchers(HttpMethod.POST, "/categories", "/tags").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, "/categories/**", "/tags/**").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/categories/**", "/tags/**").hasRole(Role.ADMIN.name())

                        // 3. TẤT CẢ CÁC API CÒN LẠI ĐỀU BẮT BUỘC PHẢI ĐĂNG NHẬP
                        .anyRequest().authenticated()
                );

        // Cấu hình giải mã JWT Token
        httpSecurity.oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwtConfigurer -> jwtConfigurer.decoder(jwtDecoder())
                        .jwtAuthenticationConverter(jwtAuthenticationConverter()))
        );

        // Tắt CSRF (Vì dùng Token JWT)
        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        // Chế độ Stateless
        httpSecurity.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        return httpSecurity.build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    JwtDecoder jwtDecoder() {
        SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HmacSHA512");
        return NimbusJwtDecoder
                .withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
