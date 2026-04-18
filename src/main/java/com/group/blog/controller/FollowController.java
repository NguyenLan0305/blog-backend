package com.group.blog.controller;

import com.group.blog.dto.request.ApiResponse;
import com.group.blog.dto.response.UserResponse;
import com.group.blog.service.FollowService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FollowController {

    FollowService followService;

    @PostMapping("/{username}/follow")
    public ApiResponse<String> toggleFollow(@PathVariable String username) {
        followService.toggleFollow(username);
        return ApiResponse.<String>builder()
                .result("Cập nhật trạng thái theo dõi thành công")
                .build();
    }

    @GetMapping("/{username}/followers")
    public ApiResponse<List<UserResponse>> getFollowers(@PathVariable String username) {
        return ApiResponse.<List<UserResponse>>builder()
                .result(followService.getFollowers(username))
                .build();
    }

    @GetMapping("/{username}/following")
    public ApiResponse<List<UserResponse>> getFollowing(@PathVariable String username) {
        return ApiResponse.<List<UserResponse>>builder()
                .result(followService.getFollowing(username))
                .build();
    }
}