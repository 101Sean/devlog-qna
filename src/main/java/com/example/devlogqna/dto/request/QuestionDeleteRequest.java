package com.example.devlogqna.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "질문 삭제 요청")
public class QuestionDeleteRequest {

    @NotBlank
    @Email
    private String authorEmail;

    @NotBlank
    private String password;
}
