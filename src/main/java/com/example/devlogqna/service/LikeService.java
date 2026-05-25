package com.example.devlogqna.service;

import com.example.devlogqna.dto.request.LikeRequest;
import com.example.devlogqna.dto.response.LikeResponse;
import com.example.devlogqna.entity.Question;
import com.example.devlogqna.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final StringRedisTemplate redisTemplate;
    private final QuestionRepository questionRepository;

    public LikeResponse toggleLike(Long questionId, LikeRequest request) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        if (question.getIsSecret()) {
            throw new IllegalArgumentException("Cannot like secret question");
        }

        String email = request.getUserEmail();
        String key = "qna:question:likes:" + questionId;
        System.out.println("DEBUG: key=" + key + ", size=" + redisTemplate.opsForSet().size(key));

        Boolean alreadyLiked = redisTemplate.opsForSet().isMember(key, email);
        if (Boolean.TRUE.equals(alreadyLiked)) {
            redisTemplate.opsForSet().remove(key, email);
        } else {
            redisTemplate.opsForSet().add(key, email);
        }

        long likeCount = redisTemplate.opsForSet().size(key);
        return new LikeResponse(questionId, likeCount);
    }

    public LikeResponse getLikeCount(Long questionId) {
        String key = "qna:question:likes:" + questionId;
        long count = redisTemplate.opsForSet().size(key);
        return new LikeResponse(questionId, count);
    }
}