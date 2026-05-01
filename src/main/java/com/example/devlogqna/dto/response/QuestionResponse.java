package com.example.devlogqna.dto.response;

import com.example.devlogqna.entity.QuestionStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "질문 상세 응답")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionResponse {

    private Long id;
    private String title;
    private String content;          // 비밀 글이고 열람 권한 없으면 null
    private String category;
    private Boolean isSecret;
    private String authorEmail;      // 본인 확인용 (본인 질문일 때만 표시)
    private QuestionStatus status;
    private Long viewCount;
    private Boolean adminAnswered;
    private List<String> tags;       // 태그 이름 목록
    private Long likeCount;          // 좋아요 개수
    private List<AnswerResponse> answers;   // 어드민 답변들
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
