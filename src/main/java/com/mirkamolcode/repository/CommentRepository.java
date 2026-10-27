package com.mirkamolcode.repository;

import com.mirkamolcode.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    Page<Comment> findAllByUserUsername(String username, Pageable pageable);
    Optional<Comment> findByIdAndUserUsername(UUID id, String username);
}
