package com.example.devlogqna.service;

import com.example.devlogqna.dto.request.CommentRequest;
import com.example.devlogqna.dto.response.CommentResponse;
import com.example.devlogqna.entity.Comment;
import com.example.devlogqna.entity.Question;
import com.example.devlogqna.repository.CommentRepository;
import com.example.devlogqna.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final QuestionRepository questionRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default-email}")
    private String adminEmail;

    @Value("${admin.default-name}")
    private String adminName;

    @Value("${admin.secret-key}")
    private String adminSecretKey;

    public List<CommentResponse> getCommentsByQuestion(Long questionId) {
        // 공개 질문만 댓글 조회 가능 (비밀 질문은 API에서 차단)
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        if (question.getIsSecret()) {
            throw new IllegalArgumentException("Cannot view comments on secret question");
        }
        return commentRepository.findByQuestionIdOrderByCreatedAtAsc(questionId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // 관리자 전용 메서드 (비밀글 여부 무시)
    public List<CommentResponse> getCommentsByQuestionAsAdmin(Long questionId) {
        return commentRepository.findByQuestionIdOrderByCreatedAtAsc(questionId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public CommentResponse createComment(Long questionId, CommentRequest request) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        if (question.getIsSecret()) {
            throw new IllegalArgumentException("Cannot comment on secret question");
        }

        // 현재 인증 정보 확인
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        String authorEmail, authorName, passwordHash;
        if (isAdmin) {
            authorEmail = adminEmail;
            authorName = adminName;
            passwordHash = passwordEncoder.encode(adminSecretKey);
        } else {
            if ("관리자".equals(request.getAuthorName())) {
                throw new IllegalArgumentException("'관리자' 이름은 사용할 수 없습니다.");
            }
            authorEmail = request.getAuthorEmail();
            authorName = request.getAuthorName();
            passwordHash = passwordEncoder.encode(request.getPassword());
        }

        Comment comment = Comment.builder()
                .question(question)
                .authorEmail(authorEmail)
                .authorName(authorName)
                .passwordHash(passwordHash)
                .content(request.getContent())
                .build();
        comment = commentRepository.save(comment);
        return toResponse(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, String rawPassword) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        if (!passwordEncoder.matches(rawPassword, comment.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid password");
        }
        commentRepository.delete(comment);
    }

    @Transactional
    public void adminDeleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    private CommentResponse toResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .authorName(comment.getAuthorName())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
