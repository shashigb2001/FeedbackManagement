package com.capstone.FeedbackManagement.repo;

import com.capstone.FeedbackManagement.domain.FeedbackAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AssignmentRepo extends JpaRepository<FeedbackAssignment, Long> {
    List<FeedbackAssignment> findByStudentEmailAndSubmittedFalse(String email);
    List<FeedbackAssignment> findByStudentEmailAndSubmittedTrue(String email);
    Optional<FeedbackAssignment> findByFormIdAndStudentEmail(Long formId, String email);
    List<FeedbackAssignment> findByFormId(Long formId);
    List<FeedbackAssignment> findByFormIdAndSubmittedTrue(Long formId);

}
