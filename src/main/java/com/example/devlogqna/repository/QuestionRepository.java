package com.example.devlogqna.repository;

import com.example.devlogqna.entity.Question;
import com.example.devlogqna.entity.QuestionStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    // 공개 질문 목록 (태그 필터, 페이징)
    @Query("SELECT DISTINCT q FROM Question q " +
            "LEFT JOIN q.questionTags qt " +
            "LEFT JOIN qt.tag t " +
            "WHERE (:tag IS NULL OR t.name = :tag) " +
            "ORDER BY q.createdAt DESC")
    Page<Question> findPublicQuestions(@Param("tag") String tag, Pageable pageable);

    // 어드민 전체 질문 목록 (상태, 카테고리 필터)
    @Query("SELECT q FROM Question q " +
            "WHERE (:status IS NULL OR q.status = :status) " +
            "AND (:category IS NULL OR q.category = :category) " +
            "ORDER BY q.createdAt DESC")
    Page<Question> findAllWithFilters(@Param("status") QuestionStatus status,
                                      @Param("category") String category,
                                      Pageable pageable);

    // 작성자 검증용
    @Query("SELECT q FROM Question q WHERE q.id = :id AND q.authorEmail = :email")
    Optional<Question> findByIdAndAuthorEmail(@Param("id") Long id, @Param("email") String email);

    // 미답변 질문 개수
    @Query("SELECT COUNT(q) FROM Question q WHERE q.adminAnswered = false")
    long countUnanswered();

    // 조회수 업데이트
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Question q SET q.viewCount = q.viewCount + :delta WHERE q.id = :id")
    void incrementViewCount(@Param("id") Long id, @Param("delta") Long delta);

    // 답변 등록 시 상태 변경
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Question q SET q.adminAnswered = true, q.status = 'IN_PROGRESS' WHERE q.id = :id")
    void markAnswered(@Param("id") Long id);
}
