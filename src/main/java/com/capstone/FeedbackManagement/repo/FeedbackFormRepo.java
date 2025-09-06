package com.capstone.FeedbackManagement.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.capstone.FeedbackManagement.domain.FeedbackForm;
import com.capstone.FeedbackManagement.domain.User;

public interface FeedbackFormRepo extends JpaRepository<FeedbackForm, Long> {
    List<FeedbackForm> findByCreatedBy(User createdBy);
}
