package com.example.devlogqna.controller;

import com.example.devlogqna.dto.request.LikeRequest;
import com.example.devlogqna.dto.response.LikeResponse;
import com.example.devlogqna.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/questions/{questionId}/like")
@RequiredArgsConstructor
@Tag(name = "Likes", description = "좋아요 API")
public class LikeController {

    private final LikeService likeService;

    @PostMapping
    @Operation(summary = "좋아요 토글 (추가/취소)")
    public ResponseEntity<LikeResponse> toggleLike(
            @PathVariable Long questionId,
            @Valid @RequestBody LikeRequest request) {
        return ResponseEntity.ok(likeService.toggleLike(questionId, request));
    }

    @GetMapping("count")
    @Operation(summary = "좋아요 개수 조회")
    public ResponseEntity<LikeResponse> getLikeCount(@PathVariable Long questionId) {
        return ResponseEntity.ok(likeService.getLikeCount(questionId));
    }
}
