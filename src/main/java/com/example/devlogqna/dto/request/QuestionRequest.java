package com.example.devlogqna.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "질문 등록 요청")
public class QuestionRequest {

    @NotBlank
    @Size(max = 200)
    @Schema(description = "제목", example = "Homebridge 연결 오류")
    private String title;

    @NotBlank
    @Schema(description = "내용", example = "Raspberry Pi에서 연결이 안 됩니다.")
    private String content;

    @Size(max = 50)
    @Schema(description = "카테고리", example = "homebridge")
    private String category;

    @Schema(description = "비밀글 여부", example = "false")
    private Boolean isSecret;

    @NotBlank @Email
    @Schema(description = "작성자 이메일", example = "user@example.com")
    private String authorEmail;

    @NotBlank
    @Schema(description = "작성자 비밀번호 (비밀글 열람/수정/삭제 시 사용)", example = "myPassword123")
    private String password;

    @Schema(description = "답변 알림 수신 동의", example = "true")
    private Boolean notifyEmail;

    @Schema(description = "태그 이름 목록", example = "[\"설치\", \"오류\"]")
    private List<String> tags;
}
