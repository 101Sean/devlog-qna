package com.example.devlogqna.repository;

import com.example.devlogqna.entity.QuestionLike;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface QuestionLikeRepository extends JpaRepository<QuestionLike, Long> {
    Optional<QuestionLike> findByQuestionIdAndUserEmail(Long questionId, String userEmail);
    long countByQuestionId(Long questionId);

    @Modifying
    @Query("DELETE FROM QuestionLike l WHERE l.question.id = :questionId AND l.userEmail = :userEmail")
    void deleteByQuestionIdAndUserEmail(@Param("questionId") Long questionId,
                                        @Param("userEmail") String userEmail);
}
