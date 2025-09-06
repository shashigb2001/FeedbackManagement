package com.capstone.FeedbackManagement.dto;
import java.util.List;
public record CreateFormRequest(String title, String description, List<QuestionCreateDto> questions) {}
