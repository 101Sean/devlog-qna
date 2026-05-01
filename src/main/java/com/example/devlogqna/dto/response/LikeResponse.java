package com.example.devlogqna.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LikeResponse {

    private Long questionId;
    private Long likeCount;
}
