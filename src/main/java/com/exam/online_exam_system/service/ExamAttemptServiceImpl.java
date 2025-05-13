package com.exam.online_exam_system.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exam.online_exam_system.model.Exam;
import com.exam.online_exam_system.model.ExamAttempt;
import com.exam.online_exam_system.model.Question;
import com.exam.online_exam_system.model.StudentAnswer;
import com.exam.online_exam_system.model.User; 
import com.exam.online_exam_system.repository.ExamAttemptRepository;
import com.exam.online_exam_system.repository.ExamRepository;
import com.exam.online_exam_system.repository.QuestionRepository;
import com.exam.online_exam_system.repository.StudentAnswerRepository;

@Service
public class ExamAttemptServiceImpl implements ExamAttemptService {

    private static final Logger logger = LoggerFactory.getLogger(ExamAttemptServiceImpl.class);

    @Autowired
    private ExamAttemptRepository examAttemptRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private StudentAnswerRepository studentAnswerRepository;


    @Override
    public ExamAttempt saveExamAttempt(ExamAttempt examAttempt) {
        logger.debug("Saving ExamAttempt ID: {}", examAttempt.getId());
        return examAttemptRepository.save(examAttempt);
    }

    @Override
    @Transactional
    public ExamAttempt startExamAttempt(Long examId, User student) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> {
                    logger.warn("Cannot start attempt: Exam not found with ID: {}", examId);
                    return new NoSuchElementException("Exam not found: " + examId);
                });

        ExamAttempt attempt = new ExamAttempt();
        attempt.setExam(exam);
        attempt.setUser(student);
        attempt.setStartTime(LocalDateTime.now());
        attempt.setTotalQuestionsAttempted(0);
        attempt.setScore(0);

        logger.info("Student {} starting exam attempt for Exam ID: {}", student.getUsername(), exam.getId());
        ExamAttempt savedAttempt = examAttemptRepository.save(attempt);
        logger.info("Exam attempt created with ID: {}", savedAttempt.getId());
        return savedAttempt;
    }

    @Override
    @Transactional
    public ExamAttempt submitExamAttempt(Long attemptId, Map<String, String> submittedAnswers, User student) {
        logger.info("Attempting to submit exam for attemptId: {}, studentId: {}", attemptId, student.getId());
        ExamAttempt attempt = examAttemptRepository.findById(attemptId)
            .orElseThrow(() -> {
                logger.error("ExamAttempt not found with id: {} during submission.", attemptId);
                return new NoSuchElementException("ExamAttempt not found: " + attemptId);
            });

        if (!attempt.getUser().getId().equals(student.getId())) {
            logger.warn("Security violation: Student {} (ID: {}) attempting to submit attempt ID: {} not belonging to them.",
                        student.getUsername(), student.getId(), attemptId);
            throw new SecurityException("User does not own this exam attempt.");
        }

        if (attempt.getSubmissionTime() != null) {
            logger.info("Attempt ID: {} was already submitted at {}. Returning existing attempt.", attemptId, attempt.getSubmissionTime());
            return attempt;
        }

        Exam exam = attempt.getExam();
        if (exam == null) {
            logger.error("Critical error: Exam object is null for attempt ID: {}", attemptId);
            throw new IllegalStateException("ExamAttempt (ID: " + attemptId + ") is not associated with an Exam.");
        }

        List<Question> questionsForExam = questionRepository.findByExamId(exam.getId());
        if (questionsForExam.isEmpty() && (submittedAnswers != null && !submittedAnswers.isEmpty())) {
            logger.warn("Attempt ID: {} for Exam ID: {} has no questions, but answers were submitted. This is unusual.", attemptId, exam.getId());
        } else if (questionsForExam.isEmpty()) {
             logger.info("Attempt ID: {} for Exam ID: {} has no questions. Score will be 0.", attemptId, exam.getId());
        }

        int currentScore = 0;
        int questionsAnsweredCount = 0;
        attempt.getStudentAnswers().clear(); 

        if (submittedAnswers != null) {
            for (Map.Entry<String, String> entry : submittedAnswers.entrySet()) {
                Long questionId;
                try {
                    questionId = Long.valueOf(entry.getKey());
                } catch (NumberFormatException e) {
                    logger.warn("Invalid question ID format ('{}') in submission for attempt ID: {}", entry.getKey(), attemptId);
                    continue;
                }
                String selectedOption = entry.getValue();

                Optional<Question> questionOpt = questionsForExam.stream()
                                                 .filter(q -> q.getId().equals(questionId))
                                                 .findFirst();

                if (questionOpt.isPresent()) {
                    Question question = questionOpt.get();
                    questionsAnsweredCount++;
                    boolean isCorrect = Objects.equals(selectedOption, question.getCorrectAnswer());
                    if (isCorrect) {
                        currentScore++;
                    }
                    StudentAnswer studentAnswer = new StudentAnswer();
                    studentAnswer.setExamAttempt(attempt);
                    studentAnswer.setQuestion(question);
                    studentAnswer.setSelectedOption(selectedOption);
                    studentAnswer.setCorrect(isCorrect);
                    attempt.addStudentAnswer(studentAnswer); 
                } else {
                    logger.warn("For attempt ID: {}, submitted answer for Question ID: {} which was not found in Exam ID: {}",
                                attemptId, questionId, exam.getId());
                }
            }
        }

        attempt.setScore(currentScore);
        attempt.setTotalQuestionsAttempted(questionsAnsweredCount);
        attempt.setSubmissionTime(LocalDateTime.now());
        logger.info("Attempt ID: {} submitted successfully by student {}. Score: {}/{}, Questions Answered: {}",
                    attemptId, student.getUsername(), currentScore, questionsForExam.size(), questionsAnsweredCount);

        return examAttemptRepository.save(attempt);
    }

    @Override
    public List<ExamAttempt> getAttemptsForUser(User user) {
        logger.debug("Fetching attempts for user: {}", user.getUsername());
        // This now correctly matches the updated repository method
        return examAttemptRepository.findByUser(user, Sort.by(Sort.Direction.DESC, "submissionTime"));
    }

    @Override
    public ExamAttempt getAttemptByIdAndUser(Long attemptId, User user) {
        logger.debug("Fetching attempt ID: {} for user: {}", attemptId, user.getUsername());
        ExamAttempt attempt = examAttemptRepository.findById(attemptId)
            .orElseThrow(() -> {
                logger.warn("Attempt ID: {} not found when requested by user: {}", attemptId, user.getUsername());
                return new NoSuchElementException("Exam attempt not found: " + attemptId);
            });

        if (!attempt.getUser().getId().equals(user.getId())) {
            logger.warn("Security violation: User {} attempting to access attempt ID: {} not belonging to them.",
                        user.getUsername(), attemptId);
            throw new SecurityException("You are not authorized to view this exam attempt.");
        }
        return attempt;
    }

    @Override
    public List<ExamAttempt> getAttemptsByExamId(Long examId) {
        logger.debug("Fetching all attempts for Exam ID: {}", examId);
        return examAttemptRepository.findByExamId(examId);
    }

    @Override
    public List<ExamAttempt> getAllAttempts() {
        logger.debug("Fetching all exam attempts for instructor/admin view.");
        return examAttemptRepository.findAll(Sort.by(Sort.Direction.DESC, "submissionTime"));
    }

    public StudentAnswerRepository getStudentAnswerRepository() {
        return studentAnswerRepository;
    }

    public void setStudentAnswerRepository(StudentAnswerRepository studentAnswerRepository) {
        this.studentAnswerRepository = studentAnswerRepository;
    }
}