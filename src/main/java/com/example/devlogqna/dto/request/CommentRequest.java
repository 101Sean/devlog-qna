package com.example.devlogqna.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "댓글 등록 요청")
public class CommentRequest {

    @NotBlank
    @Email
    private String authorEmail;

    @NotBlank @Size(max = 50)
    private String authorName;

    @NotBlank
    private String password;

    @NotBlank
    private String content;
}
