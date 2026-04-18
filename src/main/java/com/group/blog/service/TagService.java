package com.group.blog.service;

import com.group.blog.dto.response.TagResponse;
import com.group.blog.entity.Tag;
import com.group.blog.repository.TagRepository;
import com.group.blog.util.SlugUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TagService {

    TagRepository tagRepository;

    public List<TagResponse> getAllTags() {
        List<Object[]> results = tagRepository.findAllTagsWithPostCount();

        return results.stream().map(row -> {
            Tag tag = (Tag) row[0];
            long postCount = (long) row[1];

            return TagResponse.builder()
                    .id(tag.getId())
                    .name(tag.getName())
                    // Không lưu DB, tự tạo Slug on-the-fly (chuẩn 3NF)
                    .slug(SlugUtils.generateSlug(tag.getName()) + "-" + tag.getId())
                    .postCount(postCount)
                    .build();
        }).toList();
    }

    @org.springframework.transaction.annotation.Transactional
    public TagResponse create(com.group.blog.dto.request.CategoryRequest request) { // Dùng tạm CategoryRequest vì nó cũng chỉ có mỗi field 'name'
        if(tagRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tag is existed!"); // Hoặc dùng AppException của bạn
        }
        Tag tag = new Tag();
        tag.setName(request.getName());
        tag = tagRepository.save(tag);

        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .slug(SlugUtils.generateSlug(tag.getName()) + "-" + tag.getId())
                .postCount(0L)
                .build();

    }

    @Transactional
    public TagResponse update(java.util.UUID id, com.group.blog.dto.request.CategoryRequest request) {
        Tag tag = tagRepository.findById(id).orElseThrow(() -> new RuntimeException("Tag not found"));
        tag.setName(request.getName());
        tag = tagRepository.save(tag);
        return TagResponse.builder().id(tag.getId()).name(tag.getName()).build();
    }

    @Transactional
    public void delete(java.util.UUID id) {
        tagRepository.deleteById(id);
    }
}
