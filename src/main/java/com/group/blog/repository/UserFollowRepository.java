package com.group.blog.repository;

import com.group.blog.entity.User;
import com.group.blog.entity.UserFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, UUID> {

    long countByFollowing(User following); // Đếm số người follow user A

    long countByFollower(User follower);   // Đếm số người user A đang follow

    // Tìm record để xóa khi Unfollow
    Optional<UserFollow> findByFollowerAndFollowing(User follower, User following);

    // Kiểm tra trạng thái Follow(A đã follow B chưa?)
    boolean existsByFollowerUsernameAndFollowing(String followerUsername, User following);

    List<UserFollow> findByFollowingOrderByCreatedAtDesc(User following); // Lấy danh sách người follow user A

    List<UserFollow> findByFollowerOrderByCreatedAtDesc(User follower);   // Lấy danh sách người user A follow
}