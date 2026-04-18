package com.group.blog.repository;

import com.group.blog.entity.BlogLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlogLikeRepository extends JpaRepository<BlogLike, UUID> {
    long countByBlogId(UUID blogId);

    boolean existsByBlogIdAndUserUsername(UUID blogId, String username);

    // Tìm like để xóa nếu user click lần 2 (Unlike)
    Optional<BlogLike> findByBlogIdAndUserUsername(UUID blogId, String username);
}