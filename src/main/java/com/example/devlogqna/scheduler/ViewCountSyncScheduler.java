package com.example.devlogqna.scheduler;

import com.example.devlogqna.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class ViewCountSyncScheduler {

    private final StringRedisTemplate redisTemplate;
    private final QuestionRepository questionRepository;

    @Transactional
    @Scheduled(fixedRate = 300000) // 5분마다 실행
    public void syncViewCounts() {
        Set<String> keys = redisTemplate.keys("qna:question:views:*");
        if (keys == null || keys.isEmpty()) return;

        for (String key : keys) {
            try {
                Long questionId = Long.parseLong(key.split(":")[3]);
                String deltaStr = redisTemplate.opsForValue().get(key);
                if (deltaStr == null) continue;

                Long delta = Long.parseLong(deltaStr);
                questionRepository.incrementViewCount(questionId, delta);
                redisTemplate.delete(key);
            } catch (Exception e) {
                System.err.println("조회수 동기화 중 오류: " + e.getMessage());
            }
        }
    }
}