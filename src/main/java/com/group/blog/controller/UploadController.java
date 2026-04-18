package com.group.blog.controller;

import com.group.blog.dto.request.ApiResponse;
import com.group.blog.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {
    private final CloudinaryService cloudinaryService;

    @PostMapping("/image")
    public ApiResponse<String> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        String imageUrl = cloudinaryService.uploadImage(file);
        return ApiResponse.<String>builder()
                .result(imageUrl)
                .build();
    }
}