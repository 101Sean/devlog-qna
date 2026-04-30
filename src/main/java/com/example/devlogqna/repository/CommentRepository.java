package com.example.devlogqna.repository;

import com.example.devlogqna.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByQuestionIdOrderByCreatedAtAsc(Long questionId);
    long countByQuestionId(Long questionId);
}
