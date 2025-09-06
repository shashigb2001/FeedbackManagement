package com.capstone.FeedbackManagement.repo;

import com.capstone.FeedbackManagement.domain.FeedbackAnswer;
import com.capstone.FeedbackManagement.domain.FeedbackForm;
import com.capstone.FeedbackManagement.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnswerRepo extends JpaRepository<FeedbackAnswer, Long> {
    List<FeedbackAnswer> findByFormId(Long formId);
    List<FeedbackAnswer> findByFormAndStudent(FeedbackForm form, User student);

}
