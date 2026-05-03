package com.example.devlogqna.service;

import com.example.devlogqna.dto.request.LikeRequest;
import com.example.devlogqna.dto.response.LikeResponse;
import com.example.devlogqna.entity.Question;
import com.example.devlogqna.entity.QuestionLike;
import com.example.devlogqna.repository.QuestionLikeRepository;
import com.example.devlogqna.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeService {

    private final QuestionLikeRepository likeRepository;
    private final QuestionRepository questionRepository;

    public LikeResponse toggleLike(Long questionId, LikeRequest request) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        if (question.getIsSecret()) {
            throw new IllegalArgumentException("Cannot like secret question");
        }

        String email = request.getUserEmail();
        return likeRepository.findByQuestionIdAndUserEmail(questionId, email)
                .map(like -> {
                    likeRepository.delete(like);
                    long count = likeRepository.countByQuestionId(questionId);
                    return new LikeResponse(questionId, count);
                })
                .orElseGet(() -> {
                    QuestionLike newLike = QuestionLike.builder()
                            .question(question)
                            .userEmail(email)
                            .build();
                    likeRepository.save(newLike);
                    long count = likeRepository.countByQuestionId(questionId);
                    return new LikeResponse(questionId, count);
                });
    }

    public LikeResponse getLikeCount(Long questionId) {
        long count = likeRepository.countByQuestionId(questionId);
        return new LikeResponse(questionId, count);
    }
}
