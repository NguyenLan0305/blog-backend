package com.group.blog.service;

import com.group.blog.dto.request.BlogCreationRequest;
import com.group.blog.dto.request.BlogUpdateRequest;
import com.group.blog.dto.response.BlogResponse;
import com.group.blog.dto.response.BlogSuggestionResponse;
import com.group.blog.entity.*;
import com.group.blog.exception.AppException;
import com.group.blog.exception.ErrorCode;
import com.group.blog.mapper.BlogMapper;
import com.group.blog.repository.*;
import com.group.blog.util.SlugUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BlogService {

    BlogRepository blogRepository;
    UserRepository userRepository;
    CategoryRepository categoryRepository;
    TagRepository tagRepository;
    BlogMapper blogMapper;
    BlogViewRepository blogViewRepository;
    BlogLikeRepository blogLikeRepository;
    CommentRepository commentRepository;
    BookmarkRepository bookmarkRepository;

    // 1. Tạo bài viết mới
    @Transactional
    public BlogResponse createBlog(BlogCreationRequest request) {
        var context = SecurityContextHolder.getContext();
        String currentUsername = context.getAuthentication().getName();
        User author = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

        // Kiểm tra nếu categoryId != null thì mới tìm trong DB
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        }

        Blog blog = blogMapper.toBlog(request);
        blog.setAuthor(author);
        blog.setCategory(category); // Nếu lưu nháp chưa chọn, category sẽ là null (hợp lệ)

        if (request.getTags() != null && !request.getTags().isEmpty()) {
            Set<Tag> finalTags = new HashSet<>();
            for (String tagName : request.getTags()) {
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setName(tagName);
                            return tagRepository.save(newTag);
                        });
                finalTags.add(tag);
            }
            blog.setTags(finalTags);
        }
        return enrichBlogResponse(blogRepository.save(blog));
    }

    // 2. Cập nhật bài viết (Xử lý tách biệt Luồng Nháp và Luồng Xuất bản)
    @Transactional
    public BlogResponse updateBlog(UUID id, BlogUpdateRequest request) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BLOG_NOT_FOUND));

        var context = SecurityContextHolder.getContext();
        String currentUsername = context.getAuthentication().getName();
        if (!blog.getAuthor().getUsername().equals(currentUsername)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Cập nhật Danh mục chung
        if (request.getCategoryId() != null && (blog.getCategory() == null || !request.getCategoryId().equals(blog.getCategory().getId()))) {
            Category newCategory = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            blog.setCategory(newCategory);
        }

        // Cập nhật Tiêu đề và Mô tả chung
        if (request.getTitle() != null) blog.setTitle(request.getTitle());
        if (request.getDescription() != null) blog.setDescription(request.getDescription());

        // kiểm tra hành động lưu nháp hay đăng lên
        boolean isRequestingSaveDraft = Boolean.TRUE.equals(request.getDraft());

        if (isRequestingSaveDraft) {
            // lưu nháp
            if (blog.isDraft()) {
                // Trường hợp: Bài viết này TỪ TRƯỚC ĐẾN NAY chưa bao giờ xuất bản.
                // Lưu thẳng vào cột chính (content) và giữ nguyên trạng thái draft = true.
                if (request.getContent() != null) blog.setContent(request.getContent());
                if (request.getBanner() != null) blog.setBanner(request.getBanner());

            } else {
                // Trường hợp: Bài viết ĐÃ XUẤT BẢN, giờ user đang sửa lại.
                // user sửa nhưng chưa muốn đăng lại thì lưu bản đang sửa dở trong quản lý bài viết của user đó,
                // còn bài đó nội dung đã đăng người dùng khác bấm vào xem vẫn như cũ
                // Lưu nội dung mới vào draftContent. Trạng thái blog VẪN LÀ draft = false (đang public).
                if (request.getContent() != null) blog.setDraftContent(request.getContent());
                if (request.getBanner() != null) blog.setDraftBanner(request.getBanner());
            }

        } else {
            // user bấm đăng
            // Đổ toàn bộ nội dung mới (từ frontend truyền lên) vào thẳng cột content(cập nhất bài đã đăng và đăng bài nháp)
            if (request.getContent() != null) blog.setContent(request.getContent());
            if (request.getBanner() != null) blog.setBanner(request.getBanner());

            // Xóa rỗng cột Nháp vì bản Nháp và bản Public đã đồng bộ
            blog.setDraftContent(null);
            blog.setDraftBanner(null);
            blog.setDraft(false);

            if (blog.getPublishedAt() == null) {
                blog.setPublishedAt(LocalDateTime.now());
            }
        }

        // Xử lý Tags chung
        if (request.getTags() != null) {
            Set<Tag> finalTags = new HashSet<>();
            for (String tagName : request.getTags()) {
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setName(tagName);
                            return tagRepository.save(newTag);
                        });
                finalTags.add(tag);
            }
            blog.setTags(finalTags);
        }

        return enrichBlogResponse(blogRepository.save(blog));
    }

    // 3. Lấy chi tiết 1 bài viết
    @Transactional
    public BlogResponse getBlogById(UUID id, String ipAddress) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BLOG_NOT_FOUND));

        BlogView view = new BlogView();
        view.setBlog(blog);
        view.setIpAddress(ipAddress); // Ghi nhận IP

        boolean isAuthorViewing = false; // Cờ kiểm tra tác giả

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            String currentUsername = authentication.getName();

            // Nếu người đang xem chính là tác giả bài viết
            if (blog.getAuthor().getUsername().equals(currentUsername)) {
                isAuthorViewing = true;
            } else {
                // Nếu là người khác đọc, tìm User và gán vào dòng View
                userRepository.findByUsername(currentUsername).ifPresent(view::setUser);
            }
        }

        // CHỈ LƯU VÀO DATABASE NẾU:
        // 1. Không phải tác giả đang xem
        // 2. IP này chưa xem bài viết này trong vòng 1 GIỜ QUA (Chống F5 spam)
        if (!isAuthorViewing) {
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            boolean alreadyViewed = blogViewRepository.existsByBlogIdAndIpAddressAndViewedAtAfter(
                    blog.getId(),
                    ipAddress,
                    oneHourAgo
            );

            if (!alreadyViewed) {
                blogViewRepository.save(view); // Thoả mãn hết điều kiện mới lưu!
            }
        }

        return enrichBlogResponse(blog);
    }

    // 4. Lấy danh sách toàn bộ bài viết
    public List<BlogResponse> getAllBlogs() {
        List<Object[]> results = blogRepository.findAllBlogsWithCounts();

        return results.stream().map(row -> {
            Blog blog = (Blog) row[0];
            long viewCount = (long) row[1];
            long likeCount = (long) row[2];
            long commentCount = (long) row[3];

            BlogResponse response = blogMapper.toBlogResponse(blog);

            //TẠO SLUG THEO FORMAT: "title-slug" + "-" + "UUID"
            String generatedSlug = SlugUtils.generateSlug(blog.getTitle()) + "-" + blog.getId().toString();
            response.setSlug(generatedSlug);

            response.setTotalReads((int) viewCount);
            response.setTotalLikes((int) likeCount);
            response.setTotalComments((int) commentCount);

            return response;
        }).toList();
    }

    // 5. Xóa bài viết
    @Transactional
    public void deleteBlog(UUID id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BLOG_NOT_FOUND));

        var context = SecurityContextHolder.getContext();
        String currentUsername = context.getAuthentication().getName();

        boolean isAuthor = blog.getAuthor().getUsername().equals(currentUsername);
        boolean isAdmin = context.getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAuthor && !isAdmin) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        blogRepository.deleteById(id);
    }

    // Lấy danh sách bài viết của TÔI (người đang đăng nhập)
    public List<BlogResponse> getMyBlogs() {
        //Lấy username từ Token
        var context = SecurityContextHolder.getContext();
        String currentUsername = context.getAuthentication().getName();
        List<Object[]> results = blogRepository.findByAuthorUsernameWithCounts(currentUsername);
        return results.stream().map(this::mapRowToBlogResponse).toList();
    }


    // Trả về list Blog khi biết Category ID
    public List<BlogResponse> getBlogsByCategory(UUID categoryId) {
        List<Object[]> results = blogRepository.findBlogsByCategoryIdWithCounts(categoryId);
        return results.stream().map(this::mapRowToBlogResponse).toList();
    }
    // Trả về list Blog khi biết Tag ID
    public List<BlogResponse> getBlogsByTag(UUID tagId) {
        List<Object[]> results = blogRepository.findBlogsByTagIdWithCounts(tagId);
        return results.stream().map(this::mapRowToBlogResponse).toList();
    }

    public List<BlogResponse> searchBlogs(String keyword) {
        List<Object[]> results = blogRepository.searchBlogsByKeywordWithCounts(keyword);
        return results.stream().map(this::mapRowToBlogResponse).toList();
    }


    public List<BlogSuggestionResponse> getSearchSuggestions(String keyword) {
        return blogRepository.findTop5ByTitleContainingIgnoreCaseAndDraftIsFalse(keyword)
                .stream().map(blog -> BlogSuggestionResponse.builder()
                        .id(blog.getId())
                        .title(blog.getTitle())
                        .slug(SlugUtils.generateSlug(blog.getTitle()) + "-" + blog.getId())
                        .authorName(blog.getAuthor() != null ? blog.getAuthor().getUsername() : "Anonymous")
                        .categoryName(blog.getCategory() != null ? blog.getCategory().getName() : "Khác")
                        .build())
                .toList();
    }


    // Hàm xử lý lọc đa luồng
    public List<BlogResponse> filterBlogs(String keyword, UUID categoryId) {
        List<Object[]> results;

        boolean hasKeyword = (keyword != null && !keyword.trim().isEmpty());
        boolean hasCategory = (categoryId != null);

        if (hasKeyword && hasCategory) {
            results = blogRepository.findByKeywordAndCategoryIdWithCounts(keyword, categoryId);
        } else if (hasKeyword) {
            results = blogRepository.searchBlogsByKeywordWithCounts(keyword);
        } else if (hasCategory) {
            results = blogRepository.findBlogsByCategoryIdWithCounts(categoryId);
        } else {
            results = blogRepository.findAllBlogsWithCounts();
        }

        return results.stream().map(this::mapRowToBlogResponse).toList();
    }

    // Lấy danh sách bài viết đã xuất bản của một User bất kỳ
    public List<BlogResponse> getPublishedBlogsByUsername(String username) {
        List<Object[]> results = blogRepository.findPublishedByAuthorUsernameWithCounts(username);
        return results.stream().map(this::mapRowToBlogResponse).toList();
    }

    public long countTotalBlogs() {
        return blogRepository.count();
    }

    public List<BlogResponse> getRecentBlogs(int limit) {
        return blogRepository.findTop5ByDraftFalseOrderByCreatedAtDesc().stream()
                .map(blogMapper::toBlogResponse)
                .toList();
    }

    // Tạo hàm Helper
    private BlogResponse mapRowToBlogResponse(Object[] row) {
        Blog blog = (Blog) row[0];
        long viewCount = (long) row[1];
        long likeCount = (long) row[2];
        long commentCount = (long) row[3];

        BlogResponse response = blogMapper.toBlogResponse(blog);
        response.setSlug(SlugUtils.generateSlug(blog.getTitle()) + "-" + blog.getId());
        response.setTotalReads((int) viewCount);
        response.setTotalLikes((int) likeCount);
        response.setTotalComments((int) commentCount);
        return response;
    }


    // HELPER METHOD
    private BlogResponse enrichBlogResponse(Blog blog) {
        BlogResponse response = blogMapper.toBlogResponse(blog);

        // TẠO SLUG THEO FORMAT: "title-slug" + "-" + "UUID"
        String generatedSlug = SlugUtils.generateSlug(blog.getTitle()) + "-" + blog.getId().toString();
        response.setSlug(generatedSlug);

        response.setTotalReads((int) blogViewRepository.countByBlogId(blog.getId()));
        response.setTotalLikes((int) blogLikeRepository.countByBlogId(blog.getId()));
        response.setTotalComments((int) commentRepository.countByBlogId(blog.getId()));

        //KIỂM TRA TRẠNG THÁI LIKE CỦA USER HIỆN TẠI
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            String currentUsername = authentication.getName();
            // 1. Kiểm tra trạng thái Like
            boolean isLiked = blogLikeRepository.existsByBlogIdAndUserUsername(blog.getId(), currentUsername);
            response.setLikedByCurrentUser(isLiked);

            // 2. Kiểm tra trạng thái Bookmark
            boolean isBookmarked = bookmarkRepository.existsByBlogIdAndUserUsername(blog.getId(), currentUsername);
            response.setBookmarkedByCurrentUser(isBookmarked);
        } else {
            // Khách chưa đăng nhập
            response.setLikedByCurrentUser(false);
            response.setBookmarkedByCurrentUser(false);
        }

        return response;
    }


}