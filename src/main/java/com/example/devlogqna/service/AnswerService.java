package com.example.devlogqna.service;

import com.example.devlogqna.dto.request.AnswerRequest;
import com.example.devlogqna.dto.response.AnswerResponse;
import com.example.devlogqna.entity.Answer;
import com.example.devlogqna.entity.Question;
import com.example.devlogqna.entity.QuestionStatus;
import com.example.devlogqna.entity.User;
import com.example.devlogqna.repository.AnswerRepository;
import com.example.devlogqna.repository.QuestionRepository;
import com.example.devlogqna.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;  // Pub/Sub 용
    private final CacheManager cacheManager;

    // 특정 질문의 답변 목록
    public List<AnswerResponse> getAnswersByQuestion(Long questionId) {
        return answerRepository.findByQuestionIdOrderByCreatedAtAsc(questionId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // 답변 등록 (어드민)
    @Transactional
    @CacheEvict(value = {"questionDetail", "publicQuestions"}, key = "#questionId")
    public AnswerResponse createAnswer(Long questionId, AnswerRequest request, String adminUsername) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        Answer answer = Answer.builder()
                .question(question)
                .answeredBy(admin)
                .content(request.getContent())
                .build();
        answer = answerRepository.save(answer);

        // 질문 상태 업데이트
        question.markAnswered();
        questionRepository.save(question);

        // 이메일 알림 발송 (비동기 Pub/Sub)
        if (Boolean.TRUE.equals(question.getNotifyEmail())) {
            String message = String.format("%s|||%s", question.getAuthorEmail(), question.getTitle());
            redisTemplate.convertAndSend("email:notification", message);
        }

        return toResponse(answer);
    }

    // 답변 수정
    @Transactional
    public AnswerResponse updateAnswer(Long answerId, AnswerRequest request) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Answer not found"));
        answer.update(request.getContent());

        // 연관된 질문 ID를 가져와서 캐시 무효화
        Long questionId = answer.getQuestion().getId();
        Objects.requireNonNull(cacheManager.getCache("questionDetail")).evict(questionId);

        return toResponse(answer);
    }

    // 답변 삭제
    @Transactional
    public void deleteAnswer(Long answerId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Answer not found"));

        Question question = answer.getQuestion();
        Long questionId = question.getId();

        answerRepository.delete(answer);

        if (answerRepository.countByQuestionId(questionId) == 0) {
            question.markUnanswered();
            questionRepository.save(question);
        }

        Objects.requireNonNull(cacheManager.getCache("questionDetail")).evict(questionId);
        Objects.requireNonNull(cacheManager.getCache("publicQuestions")).clear();
    }

    private AnswerResponse toResponse(Answer answer) {
        return AnswerResponse.builder()
                .id(answer.getId())
                .content(answer.getContent())
                .createdAt(answer.getCreatedAt())
                .build();
    }
}
