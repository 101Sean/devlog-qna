package com.example.devlogqna.repository;

import com.example.devlogqna.entity.QuestionTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionTagRepository extends JpaRepository<QuestionTag, Long> {
    List<QuestionTag> findByQuestionId(Long questionId);
    void deleteByQuestionId(Long questionId);
    void deleteByQuestionIdAndTagId(Long questionId, Long tagId);
    boolean existsByTagId(Long tagId);
}
