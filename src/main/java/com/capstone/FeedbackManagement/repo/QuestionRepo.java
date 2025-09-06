package com.capstone.FeedbackManagement.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.capstone.FeedbackManagement.domain.Question;
import com.capstone.FeedbackManagement.domain.FeedbackForm;

public interface QuestionRepo extends JpaRepository<Question, Long> {
    List<Question> findByForm(FeedbackForm form);
}
