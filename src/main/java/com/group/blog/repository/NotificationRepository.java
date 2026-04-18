package com.group.blog.repository;

import com.group.blog.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    // lấy các thông báo của 1 user theo username
    List<Notification> findByUserUsernameOrderByCreatedAtDesc(String username);
}