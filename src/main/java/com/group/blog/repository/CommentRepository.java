package com.group.blog.repository;

import com.group.blog.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    // đếm 1 bài viết có bao nhiêu bình luận
    long countByBlogId(UUID blogId);

    // Chỉ lấy các comment GỐC (parent IS NULL)
    List<Comment> findByBlogIdAndParentIsNullOrderByCreatedAtDesc(UUID blogId);
}