package com.group.blog.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name="comments",indexes = {
        @Index(name="idx_comment_blog", columnList="blog_id"),
        @Index(name="idx_comment_parent", columnList="parent_id")
})
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id",nullable = false)
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    Blog blog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    Comment parent;

    // 1 comment có thể có nhiều phản hồi
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    List<Comment> replies;

    @Column(columnDefinition = "TEXT")
    String content;

    LocalDateTime createdAt;

    @PrePersist
    void prePersist(){
        if(createdAt == null){
            createdAt = LocalDateTime.now();
        }
    }
}
