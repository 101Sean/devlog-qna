package com.example.devlogqna.controller;

import com.example.devlogqna.dto.response.*;
import com.example.devlogqna.entity.Question;
import com.example.devlogqna.repository.QuestionRepository;
import com.example.devlogqna.service.AnswerService;
import com.example.devlogqna.service.CommentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import com.example.devlogqna.dto.request.CommentRequest;
import com.example.devlogqna.dto.request.QuestionRequest;
import com.example.devlogqna.service.QuestionService;
import com.example.devlogqna.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequiredArgsConstructor
public class UserPageController {

    private final QuestionService questionService;
    private final QuestionRepository questionRepository;
    private final TagService tagService;
    private final CommentService commentService;
    private final AnswerService answerService;

    // Thymleaf
    @GetMapping("/")
    public String listQuestions(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(required = false) String tag,
                                Model model) {
        QuestionPageResponse questions = questionService.getPublicQuestions(page, tag);
        List<TagResponse> tags = tagService.getActiveTags();

        // 관리자 여부 확인하여 모델에 추가
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);

        model.addAttribute("questions", questions);
        model.addAttribute("tags", tags);
        return "user/list";
    }

    @GetMapping("/questions/new")
    public String newQuestionForm(Model model) {
        model.addAttribute("questionRequest", new QuestionRequest());
        return "user/form";
    }

    @PostMapping("/questions/new")
    public String createQuestion(@Valid QuestionRequest request) {
        questionService.createQuestion(request);
        return "redirect:/";
    }

    @GetMapping("/questions/{id}")
    public String questionDetail(@PathVariable Long id, Model model,
                                 Authentication authentication, HttpSession session) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));

        model.addAttribute("isAdmin", isAdmin);

        questionService.incrementViewCount(id);

        Set<Long> unlockedQuestions = (Set<Long>) session.getAttribute("unlockedQuestions");
        boolean isUnlocked = unlockedQuestions != null && unlockedQuestions.contains(id);

        QuestionResponse question;
        try {
            if (isAdmin || isUnlocked) {
                // 관리자이거나 이미 비밀번호를 입력한 경우
                question = questionService.getQuestionAsAdmin(id);
            } else {
                question = questionService.getQuestion(id, null);
            }
        } catch (IllegalStateException e) {
            return "redirect:/questions/" + id + "/unlock";
        }

        List<CommentResponse> comments;
        if (isAdmin) {
            comments = commentService.getCommentsByQuestionAsAdmin(id);
        } else {
            try {
                comments = commentService.getCommentsByQuestion(id);
            } catch (IllegalArgumentException e) {
                comments = Collections.emptyList(); // 비밀글에 대한 댓글 없음
            }
        }

        List<AnswerResponse> answers = answerService.getAnswersByQuestion(id);
        model.addAttribute("question", question);
        model.addAttribute("comments", comments);
        model.addAttribute("answers", answers);
        model.addAttribute("commentRequest", new CommentRequest()); // 댓글 폼용

        return "user/detail";
    }

    @GetMapping("/questions/{id}/unlock")
    public String unlockPage(@PathVariable Long id, Model model) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        model.addAttribute("questionId", id);
        model.addAttribute("questionTitle", question.getTitle());
        model.addAttribute("authorEmail", question.getAuthorEmail());
        return "user/unlock";
    }

    @PostMapping("/questions/{id}/unlock")
    public String processUnlock(@PathVariable Long id,
                                @RequestParam("password") String rawPassword,
                                HttpSession session) {
        try {
            QuestionResponse question = questionService.unlockSecretQuestion(id, rawPassword);

            // 성공 시 세션에 저장하고 상세 페이지로 이동
            Set<Long> unlockedQuestions = (Set<Long>) session.getAttribute("unlockedQuestions");
            if (unlockedQuestions == null) {
                unlockedQuestions = new HashSet<>();
            }
            unlockedQuestions.add(id);
            session.setAttribute("unlockedQuestions", unlockedQuestions);

            return "redirect:/questions/" + id;
        } catch (IllegalArgumentException e) {
            return "redirect:/questions/" + id + "/unlock?error";
        }
    }

    @PostMapping("/questions/{id}/comments/new")
    public String addComment(@PathVariable Long id,
                             @ModelAttribute CommentRequest commentRequest,
                             Model model) {
        commentService.createComment(id, commentRequest);
        return "redirect:/questions/" + id;
    }

    @GetMapping("/chat")
    public String chatPage(Model model, HttpSession session) {
        session.setAttribute("chat", "active");
        model.addAttribute("jsessionId", session.getId());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);
        return "chat";
    }

    @GetMapping("/questions/{id}/author-email")
    @ResponseBody
    public String getAuthorEmail(@PathVariable Long id) {
        return questionService.getAuthorEmailById(id);
    }
}
