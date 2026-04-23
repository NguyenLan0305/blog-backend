package com.group.blog.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level= AccessLevel.PRIVATE)
@Table(name="users")
public class User {
 @Id
 @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;
    @Column(nullable = false,unique = true,length = 100)
    String username;
    @Column(nullable = false,length = 255)
    String password;
    @Column(unique = true,length = 255)
    String email;
    String bio;

    String avatarUrl;
    LocalDateTime createdAt;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL,orphanRemoval = true,fetch = FetchType.EAGER)
    @Builder.Default
    Set<UserRole> roles = new HashSet<>();

    @OneToMany(mappedBy = "author",cascade = CascadeType.ALL)
            @Builder.Default
    List<Blog> blogs=new ArrayList<>();

    @PrePersist
    void prePersist(){
       if(createdAt == null)
        createdAt = LocalDateTime.now();
    }
}
