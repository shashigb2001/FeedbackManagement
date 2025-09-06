package com.capstone.FeedbackManagement.dto;

import java.time.Instant;

public record StudentSubmissionAnswerDto(
        String question,
        String type,
        Object answer,
        Instant submittedAt
) {}
