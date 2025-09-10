package com.capstone.FeedbackManagement.controller;

import com.capstone.FeedbackManagement.domain.*;
import com.capstone.FeedbackManagement.dto.*;
import com.capstone.FeedbackManagement.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;
import java.time.Instant;
import java.util.Date;
import java.lang.reflect.Method;
import com.capstone.FeedbackManagement.security.JwtService;
import com.capstone.FeedbackManagement.service.FeedbackService;

@RestController
@RequestMapping("/api/student")
@CrossOrigin
public class StudentController {
    @Autowired private AssignmentRepo assignmentRepo;
    @Autowired private FeedbackFormRepo formRepo;
    @Autowired private QuestionRepo questionRepo;
    @Autowired private FeedbackService feedbackService;
    @Autowired private UserRepo userRepo;
    @Autowired private JwtService jwtService;
    @Autowired private AnswerRepo answerRepo;

    private User me(String bearer) {
        String email = jwtService.extractUsername(bearer.substring(7));
        return userRepo.findByEmail(email).orElseThrow();
    }

    private Instant toInstant(Object ts) {
        if (ts == null) return null;
        if (ts instanceof Instant) return (Instant) ts;
        if (ts instanceof Date) return ((Date) ts).toInstant();
        if (ts instanceof Number) return Instant.ofEpochMilli(((Number) ts).longValue());
        if (ts instanceof CharSequence) {
            try { return Instant.parse(ts.toString()); } catch (Exception ignored) {}
        }
        return null;
    }


    private Object callFirstExistingGetter(Object obj, String... names) {
        if (obj == null) return null;
        Class<?> cls = obj.getClass();
        for (String name : names) {
            try {
                Method m = cls.getMethod(name);
                if (m != null) {
                    return m.invoke(obj);
                }
            } catch (NoSuchMethodException ignored) {
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }


    private Instant extractAssignedAt(Object assignment) {
        Object cand = callFirstExistingGetter(assignment,
                "getAssignedAt", "getAssignedOn", "getAssignedTime", "getAssignedDate");
        return toInstant(cand);
    }


    @GetMapping("/pending")
    @PreAuthorize("hasRole('STUDENT')")
    public List<Map<String,Object>> pending(@RequestHeader("Authorization") String bearer) {
        String email = me(bearer).getEmail();
        List<FeedbackAssignment> list = feedbackService.pendingFor(email);

        return list.stream().map(a -> {
            FeedbackForm form = a.getForm();
            Instant assignedAt = extractAssignedAt(a);

            Map<String,Object> m = new HashMap<>();
            m.put("formId", form.getId());
            m.put("title", form.getTitle());
            m.put("description", form.getDescription() == null ? "" : form.getDescription());
            m.put("createdBy", form.getCreatedBy() != null ? form.getCreatedBy().getFullName() : "Unknown");
            m.put("assignedAt", assignedAt);
            return m;
        }).collect(Collectors.toList());
    }


    @GetMapping("/completed")
    @PreAuthorize("hasRole('STUDENT')")
    public List<Map<String,Object>> completed(@RequestHeader("Authorization") String bearer) {
        User student = me(bearer);
        List<FeedbackAssignment> assignments = feedbackService.completedFor(student.getEmail());

        return assignments.stream().map(a -> {
            FeedbackForm form = a.getForm();

            Map<String,Object> m = new HashMap<>();
            m.put("formId", form.getId());
            m.put("title", form.getTitle());
            m.put("description", form.getDescription() == null ? "" : form.getDescription());
            m.put("createdBy", form.getCreatedBy() != null ? form.getCreatedBy().getFullName() : "Unknown");
            return m;
        }).collect(Collectors.toList());
    }



    @PostMapping("/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public Map<String,Object> submit(@RequestHeader("Authorization") String bearer,
                                     @RequestBody SubmitFeedbackRequest req) {
        feedbackService.submitFeedback(me(bearer), req.formId(), req.answers());
        return Map.of("status", "success", "formId", req.formId());
    }

    @GetMapping("/pending/{formId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> pendingSingle(@RequestHeader("Authorization") String bearer,
                                           @PathVariable("formId") Long formId) {
        String email = me(bearer).getEmail();
        List<FeedbackAssignment> list = feedbackService.pendingFor(email);

        Optional<FeedbackAssignment> opt = list.stream()
                .filter(a -> a.getForm() != null && Objects.equals(a.getForm().getId(), formId))
                .findFirst();

        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Pending form not found"));
        }

        FeedbackAssignment a = opt.get();
        FeedbackForm form = a.getForm();

        List<QuestionDto> questions = questionRepo.findByForm(form)
                .stream()
                .map(q -> new QuestionDto(
                        q.getId(),
                        q.getPrompt(),
                        q.getType().name(),
                        q.getMaxRating()
                ))
                .collect(Collectors.toList());

        Instant assignedAt = extractAssignedAt(a);

        StudentPendingDto dto = new StudentPendingDto(
                a.getId(),
                form.getId(),
                form.getTitle(),
                form.getDescription() == null ? "" : form.getDescription(),
                form.getCreatedBy() != null ? form.getCreatedBy().getFullName() : "Unknown",
                assignedAt,
                questions
        );

        return ResponseEntity.ok(dto);
    }


    @GetMapping("/completed/{formId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> completedSingle(@RequestHeader("Authorization") String bearer,
                                             @PathVariable("formId") Long formId) {
        User student = me(bearer);
        List<FeedbackAssignment> assignments = feedbackService.completedFor(student.getEmail());

        Optional<FeedbackAssignment> opt = assignments.stream()
                .filter(a -> a.getForm() != null && Objects.equals(a.getForm().getId(), formId))
                .findFirst();

        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Completed form not found"));
        }

        FeedbackAssignment a = opt.get();
        FeedbackForm form = a.getForm();

        List<FeedbackAnswer> answers = answerRepo.findByFormAndStudent(form, student);
        List<StudentSubmissionAnswerDto> qaList = answers.stream().map(ans -> {
            Object answerVal = ans.getQuestion().getType() == QuestionType.RATING ? ans.getRatingAnswer() : ans.getTextAnswer();
            return new StudentSubmissionAnswerDto(
                    ans.getQuestion().getPrompt(),
                    ans.getQuestion().getType().name(),
                    answerVal,
                    toInstant(ans.getCreatedAt())
            );
        }).collect(Collectors.toList());

        Instant assignedAt = extractAssignedAt(a);

        StudentCompletedDto dto = new StudentCompletedDto(
                a.getId(),
                form.getId(),
                form.getTitle(),
                form.getDescription() == null ? "" : form.getDescription(),
                form.getCreatedBy() != null ? form.getCreatedBy().getFullName() : "Unknown",
                assignedAt,
                qaList
        );

        return ResponseEntity.ok(dto);
    }


}
