package com.capstone.FeedbackManagement.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FeedbackAssignment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private FeedbackForm form;

    private String studentEmail;

    @ManyToOne
    private User student;

    private boolean submitted = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant assignedAt;}
