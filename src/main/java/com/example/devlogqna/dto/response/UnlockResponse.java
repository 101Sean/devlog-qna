package com.example.devlogqna.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UnlockResponse {

    private boolean success;
    private Long questionId;  // 성공 시
    private String message;   // 실패 시 사유
}
