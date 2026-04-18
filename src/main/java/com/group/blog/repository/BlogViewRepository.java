package com.group.blog.repository;


import com.group.blog.entity.BlogLike;
import com.group.blog.entity.BlogView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
    public interface BlogViewRepository extends JpaRepository<BlogView, UUID> {
        long countByBlogId(UUID blogId);
    }

