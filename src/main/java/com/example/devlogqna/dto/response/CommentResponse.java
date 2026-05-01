package com.example.devlogqna.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CommentResponse {

    private Long id;
    private String authorName;
    private String content;
    private LocalDateTime createdAt;
}
