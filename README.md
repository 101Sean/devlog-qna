# DevlogQnA

> 기술 블로그 Q&A 시스템
> Spring Boot 4.0 + MySQL + Redis + Security + WebSocket

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-brightgreen)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://openjdk.org/projects/jdk/21/)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)

## 📌 소개
DevlogQnA는 기술 블로그에 쓰일 **비가입 기반 Q&A 플랫폼**입니다.  
블로그 방문자는 익명으로 질문을 등록하고, 공개 질문에는 댓글을 달 수 있으며,  
어드민(블로그 주인)은 답변과 전체 질문을 관리할 수 있습니다.

### ✨ 주요 기능
- **익명 질문/댓글** (이메일 + 비밀번호로 작성자 검증)
- **비밀 질문** (작성자 + 어드민만 열람 가능)
- **태그 기반 분류** (다대다 관계)
- **질문 상태 워크플로우** (OPEN → IN_PROGRESS → RESOLVED → CLOSED)
- **좋아요** 기능
- **어드민 답변** (이메일 알림 발송)
- **실시간 채팅 상담** (WebSocket STOMP, v2.0 예정)
- **Redis 기반 캐싱 / Rate Limiting**

## 🛠 기술 스택

| 분류 | 기술                                            |
|------|-----------------------------------------------|
| Language | Java 21                                       |
| Framework | Spring Boot 4.0.6                             |
| Security | Spring Security, JWT (Access + Refresh Token) |
| Persistence | Spring Data JPA, MySQL 8.0                    |
| Cache / Broker | Redis (Lettuce)                               |
| Messaging | WebSocket (STOMP)                             |
| Email | Java Mail Sender (Gmail SMTP)                 |
| View | Thymeleaf (관리자 화면)                            |
| Documentation | Springdoc OpenAPI (Swagger UI)                |
| Build | Gradle                                        |
| Container | Docker Compose (MySQL + Redis)                |
