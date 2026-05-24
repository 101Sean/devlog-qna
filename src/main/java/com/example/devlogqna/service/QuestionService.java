package com.example.devlogqna.service;

import com.example.devlogqna.dto.request.QuestionRequest;
import com.example.devlogqna.dto.request.QuestionUpdateRequest;
import com.example.devlogqna.dto.request.UnlockRequest;
import com.example.devlogqna.dto.response.QuestionListResponse;
import com.example.devlogqna.dto.response.QuestionPageResponse;
import com.example.devlogqna.dto.response.QuestionResponse;
import com.example.devlogqna.entity.Question;
import com.example.devlogqna.entity.QuestionStatus;
import com.example.devlogqna.entity.QuestionTag;
import com.example.devlogqna.entity.Tag;
import com.example.devlogqna.repository.AnswerRepository;
import com.example.devlogqna.repository.QuestionRepository;
import com.example.devlogqna.repository.QuestionTagRepository;
import com.example.devlogqna.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final TagRepository tagRepository;
    private final QuestionTagRepository questionTagRepository;
    private final AnswerRepository answerRepository;
    private final PasswordEncoder passwordEncoder;

    // 공개 질문 목록 (캐시 적용)
    /*
    @Cacheable(value = "publicQuestions", key = "'page:' + #page + ':tag:' + (#tag != null ? #tag : 'all')")
    public Page<QuestionListResponse> getPublicQuestions(int page, String tag) {
        PageRequest pageRequest = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Question> questions = questionRepository.findPublicQuestions(tag, pageRequest);
        return questions.map(this::toListResponse);
    }*/

    @Cacheable(value = "publicQuestions", key = "'page:' + #page + ':tag:' + (#tag != null ? #tag : 'all')")
    public QuestionPageResponse getPublicQuestions(int page, String tag) {
        if (tag != null && tag.isBlank()) {
            tag = null;
        }

        PageRequest pageRequest = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Question> questions = questionRepository.findPublicQuestions(tag, pageRequest);
        List<QuestionListResponse> list = questions.map(this::toListResponse).getContent();
        return new QuestionPageResponse(
                list,
                questions.getNumber(),
                questions.getTotalPages(),
                questions.getTotalElements(),
                questions.isLast()
        );
    }

    // 질문 상세 (공개 질문만 캐시, 비밀은 별도 처리)
    @Cacheable(value = "questionDetail", key = "#id", unless = "#result == null")
    public QuestionResponse getQuestion(Long id, String requestEmail) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        if (question.getIsSecret()) {
            // 비밀글인 경우 캐시하지 않고 null 반환 → 컨트롤러에서 처리
            throw new IllegalStateException("Secret question requires unlock");
        }
        return toResponse(question, true, requestEmail);
    }

    // 질문 등록
    @Transactional
    @CacheEvict(value = "publicQuestions", allEntries = true)
    public QuestionResponse createQuestion(QuestionRequest request) {
        // 비밀번호 해시
        String passwordHash = passwordEncoder.encode(request.getPassword());

        Question question = Question.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .category(request.getCategory())
                .isSecret(request.getIsSecret() != null ? request.getIsSecret() : false)
                .authorEmail(request.getAuthorEmail())
                .passwordHash(passwordHash)
                .notifyEmail(request.getNotifyEmail() != null ? request.getNotifyEmail() : false)
                .build();

        question = questionRepository.save(question);

        // 태그 처리
        if (request.getTags() != null && !request.getTags().isBlank()) {
            for (String tagName : request.getTags().split(",")) {
                // split(",")으로 문자열을 리스트로 분리
                String trimmedTag = tagName.trim();
                if (!trimmedTag.isEmpty()) {
                    Tag tag = tagRepository.findByName(trimmedTag)
                            .orElseGet(() -> tagRepository.save(new Tag(trimmedTag)));
                    QuestionTag questionTag = QuestionTag.builder()
                            .question(question)
                            .tag(tag)
                            .build();
                    questionTagRepository.save(questionTag);
                }
            }
        }

        return toResponse(question, true, request.getAuthorEmail());
    }

    // 질문 수정
    @Transactional
    @CacheEvict(value = {"publicQuestions", "questionDetail"}, allEntries = true)
    public QuestionResponse updateQuestion(Long id, QuestionUpdateRequest request) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        // 작성자 검증 (이메일 + 비밀번호)
        if (!question.getAuthorEmail().equals(request.getAuthorEmail())) {
            throw new IllegalArgumentException("Not the author");
        }
        if (!passwordEncoder.matches(request.getPassword(), question.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid password");
        }

        question.update(request.getTitle(), request.getContent(), request.getCategory());

        // 태그 재설정 (기존 태그 삭제 후 새로 등록)
        questionTagRepository.deleteByQuestionId(id);
        if (request.getTags() != null) {
            for (String tagName : request.getTags()) {
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(new Tag(tagName)));
                QuestionTag questionTag = QuestionTag.builder()
                        .question(question)
                        .tag(tag)
                        .build();
                questionTagRepository.save(questionTag);
            }
        }

        return toResponse(question, true, question.getAuthorEmail());
    }

    // 질문 삭제
    @Transactional
    @CacheEvict(value = {"publicQuestions", "questionDetail"}, allEntries = true)
    public void deleteQuestion(Long id, String email, String rawPassword) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        if (!question.getAuthorEmail().equals(email)) {
            throw new IllegalArgumentException("Not the author");
        }
        if (!passwordEncoder.matches(rawPassword, question.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid password");
        }
        questionRepository.delete(question);
    }

    // 관리자 전용 전체 질문 조회 (비밀글 포함)
    public Page<QuestionResponse> getAllQuestions(int page, QuestionStatus status, String category) {
        PageRequest pageRequest = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Question> questions = questionRepository.findAllWithFilters(status, category, pageRequest);
        return questions.map(q -> toResponse(q, true, q.getAuthorEmail()));
    }

    // 관리자 전용 질문 상태 변경
    @Transactional
    @CacheEvict(value = {"questionDetail", "publicQuestions"}, key = "#id")
    public QuestionResponse updateStatus(Long id, QuestionStatus status) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        question.changeStatus(status);
        return toResponse(question, true, question.getAuthorEmail());
    }

    // 관리자 전용 질문 삭제
    @Transactional
    @CacheEvict(value = {"publicQuestions", "questionDetail"}, allEntries = true)
    public void adminDeleteQuestion(Long id) {
        questionRepository.deleteById(id);
    }

    // 비밀글 열람
    @Transactional
    public QuestionResponse unlockSecretQuestion(Long id, String rawPassword) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        if (!question.getIsSecret()) {
            throw new IllegalArgumentException("Not a secret question");
        }
        // 이메일은 DB에서 직접 가져옴
        if (!passwordEncoder.matches(rawPassword, question.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid password");
        }
        return toResponse(question, true, question.getAuthorEmail());
    }

    // 조회수 증가 (Redis에서 처리할 예정이므로 여기서는 생략, 나중에 스케줄러로 일괄 반영)

    // 이메일 마스킹 함수
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@");
        String name = parts[0];
        String domain = parts[1];
        if (name.length() <= 2) {
            return name + "***@" + domain;
        }
        return name.substring(0, 2) + "***" + name.substring(name.length() - 1) + "@" + domain;
    }

    // 비밀글 전용 email api
    public String getAuthorEmailById(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        return question.getAuthorEmail();
    }

    // 관리자는 비밀글을 비밀번호 없이 열람
    public QuestionResponse getQuestionAsAdmin(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        return toResponse(question, true, null);
    }

    // DTO 변환
    private QuestionResponse toResponse(Question question, boolean isAuthor, String requestEmail) {
        List<String> tagNames = question.getQuestionTags().stream()
                .map(qt -> qt.getTag().getName())
                .collect(Collectors.toList());

        long likeCount = question.getLikes().size();  // Like 리스트 크기 (별도 레포지토리 쿼리 가능)

        return QuestionResponse.builder()
                .id(question.getId())
                .title(question.getTitle())
                .content(isAuthor ? question.getContent() : null) // 작성자만 내용 열람
                .category(question.getCategory())
                .isSecret(question.getIsSecret())
                .authorEmail(isAuthor ? question.getAuthorEmail() : null)
                .status(question.getStatus())
                .viewCount(question.getViewCount())
                .adminAnswered(question.getAdminAnswered())
                .tags(tagNames)
                .likeCount(likeCount)
                .createdAt(question.getCreatedAt())
                .updatedAt(question.getUpdatedAt())
                .build();
    }

    private QuestionListResponse toListResponse(Question question) {
        List<String> tagNames = question.getQuestionTags().stream()
                .map(qt -> qt.getTag().getName())
                .collect(Collectors.toList());

        long likeCount = question.getLikes().size();
        String maskedEmail = maskEmail(question.getAuthorEmail());

        return QuestionListResponse.builder()
                .id(question.getId())
                .title(question.getTitle())
                .category(question.getCategory())
                .status(question.getStatus())
                .viewCount(question.getViewCount())
                .likeCount(likeCount)
                .authorEmail(maskedEmail)
                .isSecret(question.getIsSecret())
                .tags(tagNames)
                .createdAt(question.getCreatedAt())
                .build();
    }
}
