package com.example.devlogqna.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String id;
    private String sender;
    private String content;
    private String roomId;
    private String timestamp;
    private String sessionId;

    public static ChatMessage createNew() {
        ChatMessage msg = new ChatMessage();
        msg.setId(UUID.randomUUID().toString());
        return msg;
    }
}
