package com.example.devlogqna.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "question_likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"question_id", "user_email"}))
public class QuestionLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false, length = 100)
    private String userEmail;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder
    public QuestionLike(Question question, String userEmail) {
        this.question = question;
        this.userEmail = userEmail;
    }
}
