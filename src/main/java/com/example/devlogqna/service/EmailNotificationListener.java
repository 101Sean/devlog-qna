package com.example.devlogqna.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailNotificationListener implements MessageListener {

    private final JavaMailSender mailSender;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String body = new String(message.getBody());
        String[] parts = body.split("\\|\\|\\|");
        if (parts.length == 2) {
            String to = parts[0];
            String questionTitle = parts[1];

            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(to);
            mailMessage.setSubject("[QnA] 답변이 등록되었습니다.");
            mailMessage.setText("'" + questionTitle + "' 질문에 답변이 등록되었습니다.");
            mailSender.send(mailMessage);
        }
    }
}
