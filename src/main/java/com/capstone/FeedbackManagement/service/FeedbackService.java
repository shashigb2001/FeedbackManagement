package com.capstone.FeedbackManagement.service;

import com.capstone.FeedbackManagement.domain.*;
import com.capstone.FeedbackManagement.dto.AnswerDto;
import com.capstone.FeedbackManagement.dto.QuestionCreateDto;
import com.capstone.FeedbackManagement.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FeedbackService {
    @Autowired private FeedbackFormRepo formRepo;
    @Autowired private QuestionRepo questionRepo;
    @Autowired private AssignmentRepo assignmentRepo;
    @Autowired private AnswerRepo answerRepo;
    @Autowired private UserRepo userRepo;

    @Transactional
    public FeedbackForm createForm(User faculty, String title, String description, List<QuestionCreateDto> questions) {
        FeedbackForm form = FeedbackForm.builder()
                .title(title)
                .description(description)
                .createdBy(faculty)
                .build();

        List<Question> qEntities = questions == null ? Collections.emptyList() :
                questions.stream()
                        .map(dto -> Question.builder()
                                .form(form)
                                .prompt(dto.prompt())
                                .type(QuestionType.valueOf(dto.type().toUpperCase()))
                                .maxRating(dto.maxRating())
                                .build())
                        .collect(Collectors.toList());

        form.setQuestions(qEntities);

        return formRepo.save(form);
    }

    public Map<String, List<String>> assignForm(Long formId, List<String> emails) {
        FeedbackForm form = formRepo.findById(formId).orElseThrow();

        List<String> assigned = new ArrayList<>();
        List<String> alreadyAssigned = new ArrayList<>();
        List<String> notFound = new ArrayList<>();

        for (String email : emails) {
            Optional<User> optUser = userRepo.findByEmail(email);
            if (optUser.isEmpty()) {
                notFound.add(email);
                continue;
            }

            FeedbackAssignment existing = assignmentRepo.findByFormIdAndStudentEmail(formId, email).orElse(null);
            if (existing != null) {
                alreadyAssigned.add(email);
                continue;
            }

            FeedbackAssignment a = FeedbackAssignment.builder()
                    .form(form)
                    .student(optUser.get())
                    .studentEmail(email)
                    .build();

            assignmentRepo.save(a);
            assigned.add(email);
        }

        return Map.of(
                "assigned", assigned,
                "alreadyAssigned", alreadyAssigned,
                "notFound", notFound
        );
    }

    public List<FeedbackAssignment> pendingFor(String email) {
        return assignmentRepo.findByStudentEmailAndSubmittedFalse(email);
    }

    public List<FeedbackAssignment> completedFor(String email) {
        return assignmentRepo.findByStudentEmailAndSubmittedTrue(email);
    }


    @Transactional
    public void submitFeedback(User student, Long formId, List<AnswerDto> answers) {
        FeedbackForm form = formRepo.findById(formId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Form not found"));

        if (answers == null || answers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No answers provided");
        }

        for (AnswerDto a : answers) {
            Long qid = a.questionId();
            if (qid == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "questionId is required for each answer");
            }

            Question q = questionRepo.findById(qid)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question not found: " + qid));

            FeedbackAnswer ans = new FeedbackAnswer();
            ans.setForm(form);
            ans.setQuestion(q);
            ans.setStudent(student);

            if (q.getType() == QuestionType.RATING) {
                if (a.ratingAnswer() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Rating answer required for question " + q.getId());
                }
                int rating = a.ratingAnswer();
                int max = q.getMaxRating() != null ? q.getMaxRating() : 5;
                if (rating < 1 || rating > max) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Rating for question " + q.getId() + " must be between 1 and " + max);
                }
                ans.setRatingAnswer(rating);
            } else if (q.getType() == QuestionType.TEXT) {
                ans.setTextAnswer(a.textAnswer() != null ? a.textAnswer() : "");
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Unsupported question type for question " + q.getId());
            }

            answerRepo.save(ans);
        }

        assignmentRepo.findByFormIdAndStudentEmail(formId, student.getEmail())
                .ifPresent(asg -> {
                    asg.setSubmitted(true);
                    assignmentRepo.save(asg);
                });
    }

    public Map<String,Object> analytics(Long formId) {
        FeedbackForm form = formRepo.findById(formId).orElseThrow();
        Map<String,Object> out = new LinkedHashMap<>();
        out.put("formId", form.getId());
        out.put("title", form.getTitle());

        List<FeedbackAssignment> allAssigned = assignmentRepo.findByFormId(formId);
        long assignedCount = allAssigned == null ? 0L : allAssigned.size();
        List<FeedbackAssignment> completedAssignments = assignmentRepo.findByFormIdAndSubmittedTrue(formId);
        long submittedCount = completedAssignments == null ? 0L : completedAssignments.size();

        double submittedPercentage = 0.0;
        if (assignedCount > 0) {
            submittedPercentage = Math.round((100.0 * submittedCount / assignedCount) * 100.0) / 100.0;
        }

        out.put("assignedCount", assignedCount);
        out.put("submittedCount", submittedCount);
        out.put("submittedPercentage", submittedPercentage);

        List<FeedbackAnswer> formAnswers = answerRepo.findByFormId(formId);

        List<Map<String,Object>> qstats = new ArrayList<>();
        for (Question q : questionRepo.findByForm(form)) {
            Map<String,Object> qmap = new LinkedHashMap<>();
            qmap.put("questionId", q.getId());
            qmap.put("prompt", q.getPrompt());
            qmap.put("type", q.getType().name());

            List<FeedbackAnswer> answersForQ = formAnswers.stream()
                    .filter(a -> a.getQuestion() != null && a.getQuestion().getId() != null && a.getQuestion().getId().equals(q.getId()))
                    .toList();

            if (q.getType() == QuestionType.RATING) {
                // average & total responses (number of rating answers recorded)
                double avg = answersForQ.stream()
                        .mapToInt(a -> a.getRatingAnswer() == null ? 0 : a.getRatingAnswer())
                        .average()
                        .orElse(0.0);
                qmap.put("avgRating", Math.round(avg * 100.0) / 100.0);
                qmap.put("responseCount", answersForQ.size());

                int max = q.getMaxRating() != null ? q.getMaxRating() : 5;
                Map<Integer, Long> counts = new LinkedHashMap<>();
                for (int r = 1; r <= max; r++) {
                    final int star = r;
                    long c = answersForQ.stream()
                            .filter(a -> a.getRatingAnswer() != null && a.getRatingAnswer() == star)
                            .count();
                    counts.put(star, c);
                }
                qmap.put("ratingCounts", counts);

                Map<Integer, Double> percentagesOfSubmitted = new LinkedHashMap<>();
                for (int r = 1; r <= max; r++) {
                    long c = counts.getOrDefault(r, 0L);
                    double pct = (submittedCount == 0) ? 0.0 : (100.0 * c / submittedCount);
                    pct = Math.round(pct * 100.0) / 100.0;
                    percentagesOfSubmitted.put(r, pct);
                }
                qmap.put("ratingPercentagesOfSubmitted", percentagesOfSubmitted);

            } else {
                qmap.put("texts", answersForQ.stream()
                        .map(FeedbackAnswer::getTextAnswer)
                        .filter(Objects::nonNull)
                        .toList());
                qmap.put("responseCount", answersForQ.size());
            }

            qstats.add(qmap);
        }

        out.put("questions", qstats);
        return out;
    }

}
