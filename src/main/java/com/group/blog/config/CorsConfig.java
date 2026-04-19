package com.group.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        // 🔥 ĐÃ SỬA: Xóa dấu * và điền đích danh các link được phép
                        .allowedOrigins(
                                "http://localhost:5500",      // Để bạn code trên máy tính
                                "http://127.0.0.1:5500",      // Để bạn code trên máy tính
                                "http://localhost:3000",
                                "https://inkwell-blog-lime.vercel.app" // 🔥 Link Vercel chính thức của bạn
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}