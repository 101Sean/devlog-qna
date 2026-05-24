package com.example.devlogqna.repository;

import com.example.devlogqna.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);

    @Query("SELECT DISTINCT t FROM Tag t " +
            "JOIN QuestionTag qt ON qt.tag.id = t.id " +
            "JOIN Question q ON qt.question.id = q.id " +
            "WHERE q.isSecret = false")
    List<Tag> findTagsUsedInPublicQuestions();

    boolean existsByName(String name);
}