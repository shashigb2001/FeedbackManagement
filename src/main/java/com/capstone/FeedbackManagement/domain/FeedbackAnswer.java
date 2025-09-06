package com.capstone.FeedbackManagement.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FeedbackAnswer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne private FeedbackForm form;
    @ManyToOne private Question question;
    @ManyToOne private User student;

    private Integer ratingAnswer;
    @Column(length=2000) private String textAnswer;
    private Instant createdAt = Instant.now();
}
