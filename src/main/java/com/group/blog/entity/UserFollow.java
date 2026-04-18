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
@Table(name="user_follows", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"follower_id", "following_id"})
})
public class UserFollow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    // Người đi theo dõi
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    User follower;

    // Người được theo dõi
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    User following;

    LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if(createdAt == null) createdAt = LocalDateTime.now();
    }
}