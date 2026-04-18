package com.group.blog.service;

import com.group.blog.dto.response.NotificationResponse;
import com.group.blog.entity.Notification;
import com.group.blog.entity.User;
import com.group.blog.repository.NotificationRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService {
    NotificationRepository notificationRepository;

    // Hàm gọi nội bộ để tạo thông báo mới
    public void createNotification(User recipient, String message, String targetUrl, String type) {
        // Không tự gửi thông báo cho chính mình (VD: Tự like bài của mình)
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (recipient.getUsername().equals(currentUsername)) return;

        Notification notif = Notification.builder()
                .user(recipient)
                .message(message)
                .targetUrl(targetUrl)
                .type(type)
                .isRead(false)
                .build();
        notificationRepository.save(notif);
    }

    public List<NotificationResponse> getMyNotifications() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return notificationRepository.findByUserUsernameOrderByCreatedAtDesc(username)
                .stream().map(n -> NotificationResponse.builder()
                        .id(n.getId())
                        .message(n.getMessage())
                        .targetUrl(n.getTargetUrl())
                        .type(n.getType())
                        .isRead(n.isRead())
                        .createdAt(n.getCreatedAt())
                        .build()).toList();
    }

    @Transactional
    public void markAsRead(UUID id) {
        Notification n = notificationRepository.findById(id).orElseThrow();
        n.setRead(true);
        notificationRepository.save(n);
    }

    public void deleteNotification(UUID id) {
        notificationRepository.deleteById(id);
    }
}