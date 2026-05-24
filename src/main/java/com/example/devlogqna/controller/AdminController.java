package com.example.devlogqna.controller;

import org.springframework.ui.Model;
import com.example.devlogqna.dto.request.AnswerRequest;
import com.example.devlogqna.dto.request.StatusRequest;
import com.example.devlogqna.dto.request.TagRequest;
import com.example.devlogqna.dto.response.AnswerResponse;
import com.example.devlogqna.dto.response.QuestionResponse;
import com.example.devlogqna.dto.response.TagResponse;
import com.example.devlogqna.entity.QuestionStatus;
import com.example.devlogqna.service.AnswerService;
import com.example.devlogqna.service.CommentService;
import com.example.devlogqna.service.QuestionService;
import com.example.devlogqna.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "어드민 전용 API")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final QuestionService questionService;
    private final AnswerService answerService;
    private final CommentService commentService;
    private final TagService tagService;

    @GetMapping("/questions")
    @Operation(summary = "모든 질문 조회", description = "비밀글 포함, 상태·카테고리 필터 가능")
    public ResponseEntity<Page<QuestionResponse>> getAllQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) QuestionStatus status,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(questionService.getAllQuestions(page, status, category));
    }

    @PatchMapping("/questions/{id}/status")
    @Operation(summary = "질문 상태 변경")
    public ResponseEntity<QuestionResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusRequest request) {
        return ResponseEntity.ok(questionService.updateStatus(id, request.getStatus()));
    }

    @DeleteMapping("/questions/{id}")
    @Operation(summary = "질문 삭제")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        questionService.adminDeleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/questions/{id}/answer")
    @Operation(summary = "답변 등록")
    public ResponseEntity<AnswerResponse> createAnswer(
            @PathVariable Long id,
            @Valid @RequestBody AnswerRequest request,
            Principal principal) {
        return ResponseEntity.ok(answerService.createAnswer(id, request, principal.getName()));
    }

    @PutMapping("/answers/{id}")
    @Operation(summary = "답변 수정")
    public ResponseEntity<AnswerResponse> updateAnswer(
            @PathVariable Long id,
            @Valid @RequestBody AnswerRequest request) {
        return ResponseEntity.ok(answerService.updateAnswer(id, request));
    }

    @DeleteMapping("/answers/{id}")
    @Operation(summary = "답변 삭제")
    public ResponseEntity<Void> deleteAnswer(@PathVariable Long id) {
        answerService.deleteAnswer(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/comments/{id}")
    @Operation(summary = "댓글 삭제 (어드민)")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.adminDeleteComment(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/tags")
    @Operation(summary = "태그 생성")
    public ResponseEntity<TagResponse> createTag(@Valid @RequestBody TagRequest request) {
        return ResponseEntity.ok(tagService.createTag(request));
    }

    @DeleteMapping("/tags/{id}")
    @Operation(summary = "태그 삭제")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}
