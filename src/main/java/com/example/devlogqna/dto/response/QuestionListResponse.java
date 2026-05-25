package com.example.devlogqna.dto.response;

import com.example.devlogqna.entity.QuestionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class QuestionListResponse {

    private Long id;
    private String title;
    private String category;
    private QuestionStatus status;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private String authorEmail;
    private Boolean isSecret;
    private boolean adminAnswered;
    private List<String> tags;
    private LocalDateTime createdAt;
}
