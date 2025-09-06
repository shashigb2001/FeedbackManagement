package com.capstone.FeedbackManagement.dto;

import java.time.Instant;
import java.util.List;

public record StudentPendingDto(
        Long assignmentId,
        Long formId,
        String title,
        String description,
        String createdBy,
        Instant assignedAt,
        List<QuestionDto> questions
) {}
