package com.example.devlogqna.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "comments")
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false, length = 100)
    private String authorEmail;

    @Column(nullable = false, length = 50)
    private String authorName;

    @Column(nullable = false)
    private String passwordHash;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Version
    private Long version = 0L;

    @Builder
    public Comment(Question question, String authorEmail, String authorName,
                   String passwordHash, String content) {
        this.question = question;
        this.authorEmail = authorEmail;
        this.authorName = authorName;
        this.passwordHash = passwordHash;
        this.content = content;
    }

    public void update(String content) {
        this.content = content;
    }
}
