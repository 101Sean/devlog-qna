package com.example.devlogqna.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class QuestionPageResponse {
    private List<QuestionListResponse> content;
    private int page;
    private int totalPages;
    private long totalElements;
    private boolean last;
}
