package com.capstone.FeedbackManagement.dto;

import java.util.List;

public record SubmitFeedbackRequest(Long formId, List<AnswerDto> answers) {}
