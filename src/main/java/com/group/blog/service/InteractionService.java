package com.group.blog.service;

import com.group.blog.dto.request.CommentRequest;
import com.group.blog.dto.response.BlogResponse;
import com.group.blog.dto.response.CommentResponse;
import com.group.blog.dto.response.UserResponse;
import com.group.blog.entity.*;
import com.group.blog.exception.AppException;
import com.group.blog.exception.ErrorCode;
import com.group.blog.mapper.BlogMapper;
import com.group.blog.mapper.UserMapper;
import com.group.blog.repository.*;
import com.group.blog.util.SlugUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InteractionService {

    BlogRepository blogRepository;
    UserRepository userRepository;
    BlogLikeRepository blogLikeRepository;
    CommentRepository commentRepository;
    UserMapper userMapper;
    BookmarkRepository bookmarkRepository;
    NotificationService notificationService;
    // 🔥 BỔ SUNG: Tiêm BlogMapper vào để map dữ liệu Bookmark
    BlogMapper blogMapper;

    // ================= LIKE =================
    @Transactional
    public void toggleLike(UUID blogId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Optional<BlogLike> existingLike = blogLikeRepository.findByBlogIdAndUserUsername(blogId, username);

        if (existingLike.isPresent()) {
            // Đã like rồi -> Click lần nữa là UNLIKE
            blogLikeRepository.delete(existingLike.get());
        } else {
            // Chưa like -> LIKE
            Blog blog = blogRepository.findById(blogId)
                    .orElseThrow(() -> new AppException(ErrorCode.BLOG_NOT_FOUND));
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

            BlogLike newLike = BlogLike.builder().blog(blog).user(user).build();
            blogLikeRepository.save(newLike);
            String slug = SlugUtils.generateSlug(blog.getTitle()) + "-" + blog.getId();
            notificationService.createNotification(
                    blog.getAuthor(),
                    "@" + username + " liked your article \"" + blog.getTitle() + "\".",
                    "post.html?id=" + slug,
                    "LIKE"
            );
        }
    }

    // ================= COMMENT =================
    // 1. Lấy danh sách comment của 1 bài viết
    public List<CommentResponse> getCommentsByBlogId(UUID blogId) {
        List<Comment> rootComments = commentRepository.findByBlogIdAndParentIsNullOrderByCreatedAtDesc(blogId);
        return rootComments.stream().map(this::mapToCommentResponse).collect(Collectors.toList());
    }

    // 2. Thêm Comment / Reply
    @Transactional
    public CommentResponse addComment(UUID blogId, CommentRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new AppException(ErrorCode.BLOG_NOT_FOUND));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .blog(blog)
                .user(user)
                .build();

        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            // Ràng buộc: Không cho reply lồng nhau quá sâu (Tùy chọn)
            if(parent.getParent() != null) {
                // Nếu parent đã là 1 reply, ta gắn comment mới vào cùng cấp với parent đó luôn
                comment.setParent(parent.getParent());
            } else {
                comment.setParent(parent);
            }
        }

        Comment savedComment = commentRepository.save(comment);
        String slug = SlugUtils.generateSlug(blog.getTitle()) + "-" + blog.getId();
        String targetUrl = "post.html?id=" + slug + "&openComments=true"; // Kèm parameter để JS tự mở khung comment

        if (request.getParentId() != null) {
            // Đang Reply comment của ai đó -> Thông báo cho chủ comment gốc
            Comment parent = commentRepository.findById(request.getParentId()).orElseThrow();
            notificationService.createNotification(
                    parent.getUser(),
                    "@" + username + " replied to your comment on \"" + blog.getTitle() + "\".",
                    targetUrl,
                    "REPLY"
            );
        } else {
            // Đang Comment trực tiếp vào bài -> Thông báo cho chủ bài viết
            notificationService.createNotification(
                    blog.getAuthor(),
                    "@" + username + " commented on your article \"" + blog.getTitle() + "\".",
                    targetUrl,
                    "COMMENT"
            );
        }
        return mapToCommentResponse(savedComment);
    }

    // 3. Xóa Comment
    @Transactional
    public void deleteComment(UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        String commentAuthor = comment.getUser().getUsername();
        String blogAuthor = comment.getBlog().getAuthor().getUsername();

        // CHỈ CÓ: Tác giả của comment HOẶC Tác giả của bài viết mới được quyền xóa
        if (!currentUsername.equals(commentAuthor) && !currentUsername.equals(blogAuthor)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        commentRepository.delete(comment);
    }

    // Hàm phụ trợ: Đổi từ Entity sang DTO Đệ quy
    private CommentResponse mapToCommentResponse(Comment comment) {
        UserResponse userResponse = userMapper.toUserResponse(comment.getUser());

        List<CommentResponse> replies = null;
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            replies = comment.getReplies().stream().map(this::mapToCommentResponse).collect(Collectors.toList());
        }

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .user(userResponse)
                .replies(replies)
                .build();
    }

    // ================= BOOKMARK =================
    @Transactional
    public void toggleBookmark(UUID blogId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<Bookmark> existingBookmark = bookmarkRepository.findByBlogIdAndUserUsername(blogId, username);

        if (existingBookmark.isPresent()) {
            bookmarkRepository.delete(existingBookmark.get()); // Đã lưu -> Bỏ lưu
        } else {
            Blog blog = blogRepository.findById(blogId).orElseThrow(() -> new AppException(ErrorCode.BLOG_NOT_FOUND));
            User user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));
            Bookmark bookmark = Bookmark.builder().blog(blog).user(user).build();
            bookmarkRepository.save(bookmark); // Chưa lưu -> Lưu
        }
    }

    // Lấy danh sách bài đã lưu của User hiện tại
    public List<BlogResponse> getMyBookmarks() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Bookmark> bookmarks = bookmarkRepository.findByUserUsernameOrderByCreatedAtDesc(username);

        // 🔥 SỬA LẠI: Gọi hàm helper mapToBlogResponse ở dưới
        return bookmarks.stream()
                .map(b -> mapToBlogResponse(b.getBlog()))
                .toList();
    }

    // 🔥 BỔ SUNG: Hàm Helper chuyển Blog thành BlogResponse kèm theo Slug
    private BlogResponse mapToBlogResponse(Blog blog) {
        BlogResponse response = blogMapper.toBlogResponse(blog);
        // Tạo slug chuẩn để Frontend có thể click vào xem chi tiết bài viết
        String generatedSlug = SlugUtils.generateSlug(blog.getTitle()) + "-" + blog.getId().toString();
        response.setSlug(generatedSlug);
        return response;
    }
}