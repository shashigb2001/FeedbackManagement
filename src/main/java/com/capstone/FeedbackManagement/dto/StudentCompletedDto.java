package com.capstone.FeedbackManagement.dto;

import java.time.Instant;
import java.util.List;

public record StudentCompletedDto(
        Long assignmentId,
        Long formId,
        String title,
        String description,
        String createdBy,
        Instant assignedAt,
        Instant submittedAt,
        List<StudentSubmissionAnswerDto> answers
) {}
