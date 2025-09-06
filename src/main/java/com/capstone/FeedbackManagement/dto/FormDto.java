package com.capstone.FeedbackManagement.dto;

import java.util.List;

public record FormDto(
        Long id,
        String title,
        String description,
        String createdBy,
        List<QuestionDto> questions
) {}
