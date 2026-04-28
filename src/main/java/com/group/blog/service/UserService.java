package com.group.blog.service;

import com.group.blog.dto.request.PasswordChangeRequest;
import com.group.blog.dto.request.UserCreatetionRequest;
import com.group.blog.dto.request.UserUpdateRequest;
import com.group.blog.dto.response.UserResponse;
import com.group.blog.entity.User;
import com.group.blog.entity.UserRole; // THÊM IMPORT NÀY
import com.group.blog.enums.Role;
import com.group.blog.exception.AppException;
import com.group.blog.exception.ErrorCode;
import com.group.blog.mapper.UserMapper;
import com.group.blog.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    FollowService followService;

    @Transactional
    public UserResponse createUser(UserCreatetionRequest request) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new AppException(ErrorCode.USER_EXITED);

        User u = userMapper.toUser(request);
        u.setPassword(passwordEncoder.encode(request.getPassword()));

        UserRole defaultRole = UserRole.builder()
                .role(Role.USER.name())
                .user(u)
                .build();
        u.getRoles().add(defaultRole);

        User savedUser = userRepository.save(u);
        return userMapper.toUserResponse(savedUser);
    }

    @Transactional
    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));
        userMapper.updateUser(u, request);
        // nếu có cập nhật quyền
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            u.getRoles().clear(); // Xóa sạch quyền cũ

            // Duyệt qua danh sách chữ (VD: "ADMIN", "USER") truyền từ request
            request.getRoles().forEach(roleName -> {
                UserRole newRole = UserRole.builder()
                        .role(roleName)
                        .user(u)
                        .build();
                u.getRoles().add(newRole);
            });
        }

        // Lưu vào Database
        return followService.enrichUserResponse(userRepository.save(u));
    }

    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) throw new AppException(ErrorCode.USER_NOT_EXITED);
        userRepository.deleteById(id);
    }

    public List<UserResponse> getUsers() {
        return userRepository.findAll().stream().map(followService::enrichUserResponse).toList();
    }

    public UserResponse getUser(UUID id) {
        return followService.enrichUserResponse(userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED)));
    }

    public void changePassword(PasswordChangeRequest request) {
        var context = SecurityContextHolder.getContext();
        String currentUsername = context.getAuthentication().getName();

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        boolean isMatch = passwordEncoder.matches(request.getOldPassword(), user.getPassword());
        if (!isMatch) {
            throw new AppException(ErrorCode.PASSWORD_INCORRECT);
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public UserResponse getMyProfile() {
        var context = SecurityContextHolder.getContext();
        String currentUsername = context.getAuthentication().getName();

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        return followService.enrichUserResponse(user);
    }

    @Transactional
    public UserResponse updateMyProfile(UserUpdateRequest request) {
        var context = SecurityContextHolder.getContext();
        String currentUsername = context.getAuthentication().getName();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());

        return userMapper.toUserResponse(userRepository.save(user));
    }

    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        return followService.enrichUserResponse(user);
    }

    public long countTotalUsers() {
        return userRepository.count();
    }

}