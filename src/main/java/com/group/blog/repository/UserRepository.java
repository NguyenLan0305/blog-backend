package com.group.blog.repository;

import com.group.blog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
   public interface UserRepository extends JpaRepository<User, UUID> {

   boolean existsByUsername(String username);

   Optional<User> findById(UUID id);

   Optional<User> findByUsername(String username);

   boolean  existsByEmail(String email);

   Optional<User> findByEmail(String email);

   void deleteById(UUID id);
}
