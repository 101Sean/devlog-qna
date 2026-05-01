package com.example.devlogqna.dto.request;

import com.example.devlogqna.entity.QuestionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "질문 상태 변경 요청")
public class StatusRequest {

    @NotNull
    private QuestionStatus status;
}
