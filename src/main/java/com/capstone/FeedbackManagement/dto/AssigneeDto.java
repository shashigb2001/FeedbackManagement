package com.capstone.FeedbackManagement.dto;

import java.time.Instant;

public record AssigneeDto(
        Long id,
        String fullName,
        String email,
        Instant assignedAt,
        boolean submitted
) {}
