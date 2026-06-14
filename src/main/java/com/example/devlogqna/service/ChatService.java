package com.example.devlogqna.service;

import com.example.devlogqna.dto.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    private static final String ROOM_PREFIX = "chat:zset:room:";

    public void sendMessage(ChatMessage message) {
        if (message.getId() == null) {
            message.setId(UUID.randomUUID().toString());
        }

        String roomKey = ROOM_PREFIX + message.getRoomId();
        try {
            long score = Instant.parse(message.getTimestamp()).toEpochMilli();
            String json = objectMapper.writeValueAsString(message);
            redisTemplate.opsForZSet().add(roomKey, json, score);
        } catch (Exception e) {
            throw new RuntimeException("메시지 저장 오류", e);
        }
        messagingTemplate.convertAndSend("/topic/room/" + message.getRoomId(), message);
    }

    public List<ChatMessage> getRecentMessages(String roomId) {
        String roomKey = ROOM_PREFIX + roomId;

        // 6시간 이전 데이터 자동 삭제
        long cutoff = Instant.now().minusSeconds(60 * 60 * 6).toEpochMilli();
        redisTemplate.opsForZSet().removeRangeByScore(roomKey, 0, cutoff);

        Set<String> jsonSet = redisTemplate.opsForZSet().range(roomKey, 0, -1);
        if (jsonSet == null || jsonSet.isEmpty()) {
            return Collections.emptyList();
        }

        return jsonSet.stream()
                .map(json -> {
                    try {
                        return objectMapper.readValue(json, ChatMessage.class);
                    } catch (Exception e) { return null; }
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingLong(m -> Instant.parse(m.getTimestamp()).toEpochMilli()))
                .collect(Collectors.toList());
    }

    public boolean deleteMessage(String roomId, String messageId) {
        String roomKey = ROOM_PREFIX + roomId;
        Set<String> all = redisTemplate.opsForZSet().range(roomKey, 0, -1);
        if (all == null) return false;

        for (String json : all) {
            try {
                ChatMessage msg = objectMapper.readValue(json, ChatMessage.class);
                if (messageId.equals(msg.getId())) {
                    redisTemplate.opsForZSet().remove(roomKey, json);

                    ChatMessage notice = ChatMessage.createNew();
                    notice.setSender("시스템");
                    notice.setContent("관리자가 메시지를 삭제했습니다.");
                    notice.setTimestamp(Instant.now().toString());
                    notice.setRoomId(roomId);
                    notice.setId(UUID.randomUUID().toString());

                    messagingTemplate.convertAndSend("/topic/room/" + roomId, notice);
                    return true;
                }
            } catch (Exception e) { }
        }
        return false;
    }
}