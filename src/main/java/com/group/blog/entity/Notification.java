package com.group.blog.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name="notifications", indexes = {
        @Index(name="idx_notif_user", columnList="user_id")
})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    // Người NHẬN thông báo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(nullable = false)
    String message;

    String targetUrl; // Link bấm vào để xem chi tiết (VD: "/blog/java-la-gi")

    @Builder.Default
    boolean isRead = false; // Trạng thái đã đọc hay chưa

    LocalDateTime createdAt;

    @Column(nullable = false)
    String type; // Chứa các giá trị: "LIKE", "COMMENT", "REPLY", "FOLLOW"

    @PrePersist
    void prePersist(){
        if(createdAt == null){
            createdAt = LocalDateTime.now();
        }
    }
}