package com.example.devlogqna.service;

import com.example.devlogqna.dto.request.CommentRequest;
import com.example.devlogqna.dto.response.CommentResponse;
import com.example.devlogqna.entity.Comment;
import com.example.devlogqna.entity.Question;
import com.example.devlogqna.repository.CommentRepository;
import com.example.devlogqna.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public CommentResponse createComment(Long questionId, CommentRequest request) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        if (question.getIsSecret()) {
            throw new IllegalArgumentException("Cannot comment on secret question");
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());
        Comment comment = Comment.builder()
                .question(question)
                .authorEmail(request.getAuthorEmail())
                .authorName(request.getAuthorName())
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

    private CommentResponse toResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .authorName(comment.getAuthorName())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
