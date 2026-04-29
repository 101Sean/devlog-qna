package com.example.devlogqna.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "questions")
public class Question extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(length = 50)
    private String category;

    @Column(nullable = false)
    private Boolean isSecret = false;

    @Column(nullable = false, length = 100)
    private String authorEmail;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private Boolean notifyEmail = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionStatus status = QuestionStatus.OPEN;

    @Column(nullable = false)
    private Long viewCount = 0L;

    @Column(nullable = false)
    private Boolean adminAnswered = false;

    @Version
    private Long version = 0L;

    // 연관관계
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionTag> questionTags = new ArrayList<>();

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Answer> answers = new ArrayList<>();

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionLike> likes = new ArrayList<>();

    @Builder
    public Question(String title, String content, String category, Boolean isSecret,
                    String authorEmail, String passwordHash, Boolean notifyEmail) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.isSecret = isSecret;
        this.authorEmail = authorEmail;
        this.passwordHash = passwordHash;
        this.notifyEmail = notifyEmail;
    }

    // 비즈니스 메서드
    public void update(String title, String content, String category) {
        this.title = title;
        this.content = content;
        this.category = category;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void markAnswered() {
        this.adminAnswered = true;
        this.status = QuestionStatus.IN_PROGRESS;
    }

    public void changeStatus(QuestionStatus status) {
        this.status = status;
    }
}
