package com.capstone.FeedbackManagement.controller;

import com.capstone.FeedbackManagement.domain.FeedbackForm;
import com.capstone.FeedbackManagement.domain.User;
import com.capstone.FeedbackManagement.dto.AssignFormRequest;
import com.capstone.FeedbackManagement.dto.CreateFormRequest;
import com.capstone.FeedbackManagement.dto.FormDto;
import com.capstone.FeedbackManagement.dto.QuestionDto;
import com.capstone.FeedbackManagement.repo.FeedbackFormRepo;
import com.capstone.FeedbackManagement.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import com.capstone.FeedbackManagement.security.JwtService;
import com.capstone.FeedbackManagement.service.FeedbackService;

@RestController
@RequestMapping("/api/faculty")
@CrossOrigin
public class FacultyController {
    @Autowired private FeedbackService feedbackService;
    @Autowired private UserRepo userRepo;
    @Autowired private FeedbackFormRepo formRepo;
    @Autowired private JwtService jwtService;

    private User me(String bearer) { String email = jwtService.extractUsername(bearer.substring(7)); return userRepo.findByEmail(email).orElseThrow(); }


    @PostMapping("/forms")
    @PreAuthorize("hasRole('FACULTY')")
    public @ResponseBody Map<String,Object> createForm(@RequestHeader("Authorization") String bearer,
                                                       @RequestBody CreateFormRequest req) {
        try {
            User faculty = me(bearer);

            FeedbackForm form = feedbackService.createForm(faculty, req.title(),req.description(), req.questions());

            return Map.of(
                    "status", "success",
                    "id", form.getId(),
                    "title", form.getTitle(),
                    "questions", form.getQuestions().stream()
                            .map(q -> new QuestionDto(
                                    q.getId(),
                                    q.getPrompt(),
                                    q.getType().name(),
                                    q.getMaxRating()
                            ))
                            .toList()
            );
        } catch (Exception e) {
            return Map.of("status", "error", "message", e.getMessage());
        }
    }


    @PostMapping("/forms/{formId}/assign")
    @PreAuthorize("hasRole('FACULTY')")
    public @ResponseBody Map<String,Object> assign(@PathVariable("formId") Long formId,
                                                   @RequestBody AssignFormRequest req) {
        try {
            Map<String, List<String>> result = feedbackService.assignForm(formId, req.emails());
            return Map.of(
                    "status", "success",
                    "assigned", result.get("assigned"),
                    "alreadyAssigned", result.get("alreadyAssigned"),
                    "notFound", result.get("notFound")
            );
        } catch (Exception e) {
            return Map.of("status", "error", "message", e.getMessage());
        }
    }


    @GetMapping("/forms")
    @PreAuthorize("hasRole('FACULTY')")
    public List<FormDto> myForms(@RequestHeader("Authorization") String bearer) {
        User user = me(bearer);
        return formRepo.findByCreatedBy(user)
                .stream()
                .map(form -> new FormDto(
                        form.getId(),
                        form.getTitle(),
                        form.getDescription() == null ? "" : form.getDescription(),
                        form.getCreatedBy() != null ? form.getCreatedBy().getFullName() : "Unknown",
                        form.getQuestions()
                                .stream()
                                .map(q -> new QuestionDto(
                                        q.getId(),
                                        q.getPrompt(),
                                        q.getType().name(),
                                        q.getMaxRating()
                                ))
                                .toList()
                ))
                .toList();
    }


    @GetMapping("/forms/analytics")
    @PreAuthorize("hasRole('FACULTY')")
    public List<Map<String,Object>> allMyFormAnalytics(@RequestHeader("Authorization") String bearer) {
        User faculty = me(bearer);

        List<FeedbackForm> forms = formRepo.findByCreatedBy(faculty);

        return forms.stream().map(form -> {
            Map<String,Object> full = feedbackService.analytics(form.getId());
            String title = full.getOrDefault("title", form.getTitle()) + "";
            Object assigned = full.getOrDefault("assignedCount", full.getOrDefault("assigned", 0));
            Object submitted = full.getOrDefault("submittedCount", full.getOrDefault("submitted", 0));
            Object submittedPct = full.getOrDefault("submittedPercentage", full.getOrDefault("submittedPercent", 0.0));

            return Map.<String,Object>of(
                    "formId", form.getId(),
                    "title", title,
                    "assigned", assigned,
                    "submitted", submitted,
                    "submittedPercent", submittedPct
            );
        }).toList();
    }


    @GetMapping("/forms/{formId}/analytics")
    @PreAuthorize("hasRole('FACULTY')")
    public @ResponseBody Map<String,Object> singleFormAnalytics(@RequestHeader("Authorization") String bearer,
                                                                @PathVariable("formId") Long formId) {
        try {
            User faculty = me(bearer);

            Optional<FeedbackForm> opt = formRepo.findById(formId);
            if (opt.isEmpty()) {
                return Map.of("status", "error", "message", "Form not found");
            }

            FeedbackForm form = opt.get();
            if (form.getCreatedBy() == null || !Objects.equals(form.getCreatedBy().getId(), faculty.getId())) {
                return Map.of("status", "error", "message", "Not authorized to view analytics for this form");
            }

            Map<String, Object> analytics = feedbackService.analytics(formId);
            return Map.of("status", "success", "formId", formId, "analytics", analytics);
        } catch (Exception e) {
            return Map.of("status", "error", "message", e.getMessage());
        }
    }

}
