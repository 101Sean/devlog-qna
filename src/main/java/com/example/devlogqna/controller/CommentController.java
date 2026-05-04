package com.example.devlogqna.controller;

import com.example.devlogqna.dto.request.CommentDeleteRequest;
import com.example.devlogqna.dto.request.CommentRequest;
import com.example.devlogqna.dto.response.CommentResponse;
import com.example.devlogqna.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "댓글 API")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/questions/{questionId}/comments")
    @Operation(summary = "댓글 목록")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long questionId) {
        return ResponseEntity.ok(commentService.getCommentsByQuestion(questionId));
    }

    @PostMapping("/questions/{questionId}/comments")
    @Operation(summary = "댓글 등록")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long questionId,
            @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.ok(commentService.createComment(questionId, request));
    }

    @DeleteMapping("/comments/{id}")
    @Operation(summary = "댓글 삭제")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentDeleteRequest request) {
        commentService.deleteComment(id, request.getPassword());
        return ResponseEntity.noContent().build();
    }
}
