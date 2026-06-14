package com.example.devlogqna.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ChatSessionService {

    private final ConcurrentHashMap<String, String> sessionNames = new ConcurrentHashMap<>();
    private final StringRedisTemplate redisTemplate;

    public ChatSessionService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String getOrCreateName(String sessionId) {
        return sessionNames.computeIfAbsent(sessionId,
                id -> "익명" + ThreadLocalRandom.current().nextInt(1, 100));
    }

    public boolean isBanned(String sessionId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember("chat:banned:sessions", sessionId));
    }

    public void banSession(String sessionId) {
        redisTemplate.opsForSet().add("chat:banned:sessions", sessionId);
        redisTemplate.expire("chat:banned:sessions", Duration.ofHours(6));
    }

    public void removeSession(String sessionId) {
        sessionNames.remove(sessionId);
    }
}