package com.example.devlogqna.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "어드민 로그인 요청")
public class LoginRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
