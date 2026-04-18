package com.group.blog.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name="blogs")
public class Blog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(nullable = false)
    String title;

    String banner;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(columnDefinition = "LONGTEXT")
    String content;

    boolean draft;

    // Cột chứa nội dung đang sửa dở (nháp)
    @Column(columnDefinition = "LONGTEXT")
    String draftContent;

    // Cột chứa ảnh bìa đang sửa dở
    @Column(columnDefinition = "LONGTEXT")
    String draftBanner;

    LocalDateTime publishedAt;
    LocalDateTime updatedAt;
    LocalDateTime createdAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    Category category;

    @ManyToMany
    @JoinTable(
            name = "blog_tags",
            joinColumns = @JoinColumn(name = "blog_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"),
            uniqueConstraints = {
                    @UniqueConstraint(columnNames = {"blog_id","tag_id"})
            }
    )
    @Builder.Default
    Set<Tag> tags=new HashSet<>();

    @PrePersist
    void prePersist(){
        createdAt = LocalDateTime.now();

        if(!draft && publishedAt == null){
            publishedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    void preUpdate(){
        updatedAt = LocalDateTime.now();
    }
}