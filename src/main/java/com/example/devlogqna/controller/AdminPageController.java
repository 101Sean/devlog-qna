package com.example.devlogqna.controller;

import com.example.devlogqna.dto.request.AnswerRequest;
import com.example.devlogqna.dto.response.AnswerResponse;
import com.example.devlogqna.dto.response.QuestionResponse;
import com.example.devlogqna.service.AnswerService;
import com.example.devlogqna.service.CommentService;
import com.example.devlogqna.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class AdminPageController {

    private final QuestionService questionService;
    private final AnswerService answerService;
    private final CommentService commentService;

    // Thymeleaf 페이지
    @GetMapping("/admin/login")
    public String loginForm() {
        return "admin/login";
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<QuestionResponse> questions = questionService.getAllQuestions(page, null, null);
        model.addAttribute("questions", questions);
        return "admin/dashboard";
    }

    // 관리자 API (세션 기반, @ResponseBody 사용)
    @DeleteMapping("/admin/questions/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        questionService.adminDeleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

    // 답변 등록
    @PostMapping("/admin/questions/{id}/answer")
    @ResponseBody
    public ResponseEntity<AnswerResponse> createAnswer(@PathVariable Long id,
                                                       @RequestBody AnswerRequest request,
                                                       Principal principal) {
        return ResponseEntity.ok(answerService.createAnswer(id, request, principal.getName()));
    }

    // 답변 수정
    @PutMapping("/admin/answers/{id}")
    @ResponseBody
    public ResponseEntity<AnswerResponse> updateAnswer(@PathVariable Long id,
                                                       @RequestBody AnswerRequest request) {
        return ResponseEntity.ok(answerService.updateAnswer(id, request));
    }

    // 댓글 삭제
    @DeleteMapping("/admin/comments/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.adminDeleteComment(id);
        return ResponseEntity.noContent().build();
    }
}