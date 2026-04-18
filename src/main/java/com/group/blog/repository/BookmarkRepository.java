package com.group.blog.repository;

import com.group.blog.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, UUID> {
    boolean existsByBlogIdAndUserUsername(UUID blogId, String username);

    Optional<Bookmark> findByBlogIdAndUserUsername(UUID blogId, String username);

    // Lấy danh sách các bài viết đã lưu của một User, sắp xếp mới nhất lên đầu
    List<Bookmark> findByUserUsernameOrderByCreatedAtDesc(String username);
}