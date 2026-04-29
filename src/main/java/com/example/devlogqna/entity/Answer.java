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
@Table(name = "answers")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answered_by", nullable = false)
    private User answeredBy;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Version
    private Long version = 0L;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder
    public Answer(Question question, User answeredBy, String content) {
        this.question = question;
        this.answeredBy = answeredBy;
        this.content = content;
    }

    public void update(String content) {
        this.content = content;
    }
}
