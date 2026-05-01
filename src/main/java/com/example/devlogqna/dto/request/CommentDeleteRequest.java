package com.example.devlogqna.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "댓글 삭제 요청")
public class CommentDeleteRequest {

    @NotBlank
    @Schema(description = "댓글 작성 시 사용한 비밀번호")
    private String password;
}
