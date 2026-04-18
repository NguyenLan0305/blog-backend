package com.group.blog.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level= AccessLevel.PRIVATE)
@Table(name="categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;
    @Column(nullable = false, unique = true,length = 100)
    String name;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    @Builder.Default
    List<Blog> blogs = new ArrayList<>();
}
