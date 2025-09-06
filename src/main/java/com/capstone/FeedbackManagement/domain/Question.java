package com.capstone.FeedbackManagement.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Question {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private FeedbackForm form;

    private String prompt;

    @Enumerated(EnumType.STRING)
    private QuestionType type;

    private Integer maxRating;
}
