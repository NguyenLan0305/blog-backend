package com.group.blog.repository;

import com.group.blog.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    boolean existsByName(String name);

    // Lấy tất cả Category và đếm mỗi chủ đề có bao nhiêu bài viết
    @Query("""
     SELECT c,COUNT(b.id)
     FROM Category c
     LEFT JOIN c.blogs b
     GROUP BY c
    """)
    List<Object[]> findAllCategoriesWithPostCount();
}