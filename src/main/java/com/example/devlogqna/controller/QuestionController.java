package com.example.devlogqna.controller;

import com.example.devlogqna.dto.request.QuestionDeleteRequest;
import com.example.devlogqna.dto.request.QuestionRequest;
import com.example.devlogqna.dto.request.QuestionUpdateRequest;
import com.example.devlogqna.dto.request.UnlockRequest;
import com.example.devlogqna.dto.response.QuestionListResponse;
import com.example.devlogqna.dto.response.QuestionResponse;
import com.example.devlogqna.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@Tag(name = "Questions", description = "질문 관련 공개 API")
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping
    @Operation(summary = "공개 질문 목록", description = "페이징 및 태그 필터를 제공합니다.")
    public ResponseEntity<Page<QuestionListResponse>> getQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String tag) {
        return ResponseEntity.ok(questionService.getPublicQuestions(page, tag));
    }

    @GetMapping("/{id}")
    @Operation(summary = "질문 상세", description = "비밀 질문은 열람 권한이 필요합니다.")
    public ResponseEntity<QuestionResponse> getQuestion(@PathVariable Long id) {
        return ResponseEntity.ok(questionService.getQuestion(id, null));
    }

    @PostMapping
    @Operation(summary = "질문 등록")
    public ResponseEntity<QuestionResponse> createQuestion(@Valid @RequestBody QuestionRequest request) {
        return ResponseEntity.ok(questionService.createQuestion(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "질문 수정")
    public ResponseEntity<QuestionResponse> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuestionUpdateRequest request) {
        return ResponseEntity.ok(questionService.updateQuestion(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "질문 삭제")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuestionDeleteRequest request) {
        questionService.deleteQuestion(id, request.getAuthorEmail(), request.getPassword());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/unlock")
    @Operation(summary = "비밀 질문 열람", description = "작성자 이메일과 비밀번호를 검증하여 상세 내용을 반환합니다.")
    public ResponseEntity<QuestionResponse> unlockSecretQuestion(
            @PathVariable Long id,
            @Valid @RequestBody UnlockRequest request) {
        return ResponseEntity.ok(questionService.unlockSecretQuestion(id, request));
    }
}
