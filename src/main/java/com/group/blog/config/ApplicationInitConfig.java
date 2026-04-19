package com.group.blog.config;

import com.group.blog.entity.User;
import com.group.blog.entity.UserRole; // 🔥 THÊM IMPORT NÀY
import com.group.blog.enums.Role;
import com.group.blog.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level= AccessLevel.PRIVATE,makeFinal=true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {

                // 1. Tạo đối tượng User trước (khởi tạo danh sách roles rỗng)
                User user = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .roles(new HashSet<>())
                        .build();

                // 2. Tạo đối tượng UserRole và móc nó vào User vừa tạo
                UserRole adminRole = UserRole.builder()
                        .role(Role.ADMIN.name())
                        .user(user) // Rất quan trọng: trỏ ngược lại user để CSDL biết foreign key
                        .build();

                // 3. Thêm role vào danh sách của user
                user.getRoles().add(adminRole);

                // 4. Lưu User xuống Database (Nhờ CascadeType.ALL, UserRole cũng sẽ tự động được lưu)
                userRepository.save(user);

                log.warn("Admin user has been created with default password: admin, please change it");
            }
        };
    }
}