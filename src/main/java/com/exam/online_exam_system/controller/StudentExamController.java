package com.exam.online_exam_system.controller;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; 

import com.exam.online_exam_system.model.Exam; 
import com.exam.online_exam_system.model.ExamAttempt;
import com.exam.online_exam_system.model.Question;
import com.exam.online_exam_system.model.StudentAnswer;
import com.exam.online_exam_system.model.User;
import com.exam.online_exam_system.repository.StudentAnswerRepository;
import com.exam.online_exam_system.service.ExamAttemptService;
import com.exam.online_exam_system.service.ExamService;
import com.exam.online_exam_system.service.QuestionService;

@Controller
@RequestMapping("/student")
public class StudentExamController {

    private static final Logger logger = LoggerFactory.getLogger(StudentExamController.class);

    @Autowired
    private ExamService examService;

    @Autowired
    private ExamAttemptService examAttemptService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private StudentAnswerRepository studentAnswerRepository;

    @GetMapping("/exams/{examId}/instructions")
    public String showExamInstructions(@PathVariable Long examId, Model model,
                                       @AuthenticationPrincipal User user, RedirectAttributes redirectAttributes) {
        try {
            Exam exam = examService.getExamById(examId);
            if (!exam.isCurrentlyActive()) {
                logger.warn("Student {} (ID: {}) attempted to view instructions for inactive exam ID: {}",
                            (user != null ? user.getUsername() : "UNKNOWN"), (user != null ? user.getId() : "N/A"), examId);
                redirectAttributes.addFlashAttribute("errorMessage", "This exam is currently not active or available.");
                return "redirect:/exams";
            }

            model.addAttribute("exam", exam);
            model.addAttribute("pageTitle", "Exam Instructions: " + exam.getTitle());
            List<Question> questions = questionService.getQuestionsByExamId(examId);
            model.addAttribute("totalQuestions", questions.size());
            return "student/exam_instructions";
        } catch (NoSuchElementException e) {
            logger.warn("Exam ID {} not found for instructions page when requested by student {}.",
                        examId, (user != null ? user.getUsername() : "UNKNOWN"), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Exam not found.");
            return "redirect:/exams";
        }
    }

    @PostMapping("/exams/{examId}/start")
    public String startExam(@PathVariable Long examId, @AuthenticationPrincipal User student,
                            RedirectAttributes redirectAttributes) {
        try {
            Exam exam = examService.getExamById(examId); 
            if (!exam.isCurrentlyActive()) {
                logger.warn("Student {} (ID: {}) attempted to start inactive exam ID: {}",
                            student.getUsername(), student.getId(), examId);
                redirectAttributes.addFlashAttribute("errorMessage", "This exam cannot be started as it is not currently active.");
                return "redirect:/exams";
            }
            ExamAttempt attempt = examAttemptService.startExamAttempt(examId, student);
            logger.info("Student {} (ID: {}) starting exam attempt ID: {} for exam ID: {}",
                        student.getUsername(), student.getId(), attempt.getId(), examId);
            return "redirect:/student/attempts/" + attempt.getId() + "/conduct";
        } catch (NoSuchElementException e) {
            logger.warn("Could not start exam for exam ID {}: Exam not found. Requested by student {}.",
                        examId, student.getUsername(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Could not start exam: Exam not found.");
            return "redirect:/exams";
        } catch (Exception e) {
            logger.error("Error starting exam for student {} (ID: {}) and exam ID {}: {}",
                         student.getUsername(), student.getId(), examId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error starting exam: " + e.getMessage());
            return "redirect:/exams";
        }
    }
    @GetMapping("/attempts/{attemptId}/conduct")
    public String conductExamPageAllQuestions(@PathVariable Long attemptId, Model model,
                                              @AuthenticationPrincipal User student, RedirectAttributes redirectAttributes) {
        try {
            ExamAttempt attempt = examAttemptService.getAttemptByIdAndUser(attemptId, student);

            if (attempt.getSubmissionTime() != null) {
                logger.info("Student {} attempting to conduct already submitted exam attempt ID: {}.", student.getUsername(), attemptId);
                redirectAttributes.addFlashAttribute("infoMessage", "This exam has already been submitted.");
                return "redirect:/student/attempts/" + attemptId + "/result";
            }

            Exam exam = attempt.getExam();
            if (exam == null) {
                logger.error("Critical: Exam object is null for attempt ID {} (student {})", attemptId, student.getUsername());
                redirectAttributes.addFlashAttribute("errorMessage", "Exam details not found for this attempt.");
                return "redirect:/exams";
            }

            List<Question> questions = questionService.getQuestionsByExamId(exam.getId());
             if (questions.isEmpty()) {
                logger.warn("No questions found for exam ID {} (attempt ID {}). Auto-submitting.", exam.getId(), attemptId);
                examAttemptService.submitExamAttempt(attemptId, Collections.emptyMap(), student);
                redirectAttributes.addFlashAttribute("infoMessage", "Exam has no questions and was submitted.");
                return "redirect:/student/attempts/" + attemptId + "/result";
            }
            LocalDateTime startTime = attempt.getStartTime();
            long durationInSeconds = (long) exam.getDurationMinutes() * 60;
            long elapsedSeconds = ChronoUnit.SECONDS.between(startTime, LocalDateTime.now());
            long remainingSeconds = durationInSeconds - elapsedSeconds;
            if (remainingSeconds <= 0) {
                logger.info("Time expired for attempt ID {}. Forcing submission for student {}.", attemptId, student.getUsername());
                examAttemptService.submitExamAttempt(attemptId, Collections.emptyMap(), student); // Submit with empty answers
                redirectAttributes.addFlashAttribute("infoMessage", "Time for the exam expired. Your exam was submitted automatically.");
                return "redirect:/student/attempts/" + attemptId + "/result";
            }
            model.addAttribute("attempt", attempt);
            model.addAttribute("exam", exam);
            model.addAttribute("questions", questions); 
            model.addAttribute("pageTitle", "Taking Exam: " + exam.getTitle());
            model.addAttribute("remainingTimeInSeconds", remainingSeconds > 0 ? remainingSeconds : 0);
            return "student/exam_conduct"; 
        } catch (NoSuchElementException e) {
            logger.warn("Exam attempt ID {} not found for student {}.", attemptId, student.getUsername(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Exam attempt not found.");
            return "redirect:/exams";
        } catch (SecurityException e) {
            logger.warn("Security exception for student {} trying to access attempt ID {}: {}", student.getUsername(), attemptId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/exams";
        }
    }
    @PostMapping("/attempts/{attemptId}/submit")
    public String submitExamAnswers(@PathVariable Long attemptId,
                                    @RequestParam Map<String, String> allAnswers,
                                    @AuthenticationPrincipal User student,
                                    RedirectAttributes redirectAttributes) {
        try {
            logger.info("Student {} (ID: {}) submitting answers for attempt ID: {}. Raw answers received: {}",
                        student.getUsername(), student.getId(), attemptId, allAnswers);

            Map<String, String> submittedQuestionAnswers = new HashMap<>();
            if (allAnswers != null) {
                for (Map.Entry<String, String> entry : allAnswers.entrySet()) {
                    String paramName = entry.getKey();
                    String paramValue = entry.getValue();
                    if (paramName.startsWith("answers[") && paramName.endsWith("]")) {
                        try {
                            String questionIdStr = paramName.substring("answers[".length(), paramName.length() - 1);
                            submittedQuestionAnswers.put(questionIdStr, paramValue);
                        } catch (Exception e) {
                            logger.warn("Could not parse question ID from parameter name: '{}' for attempt ID {}. Error: {}",
                                        paramName, attemptId, e.getMessage());
                        }
                    }
                }
            }
            logger.info("Parsed answers for attempt ID {}: {}", attemptId, submittedQuestionAnswers);

            ExamAttempt submittedAttempt = examAttemptService.submitExamAttempt(attemptId, submittedQuestionAnswers, student);
            logger.info("Exam attempt ID {} submitted successfully. Score: {}", submittedAttempt.getId(), submittedAttempt.getScore());

            redirectAttributes.addFlashAttribute("successMessage", "Exam submitted successfully!");
            return "redirect:/student/attempts/" + submittedAttempt.getId() + "/result";

        } catch (NoSuchElementException e) {
            logger.warn("Error submitting attempt ID {}: Attempt or related entity not found. Requested by student {}.", attemptId, student.getUsername(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error submitting exam: Attempt not found.");
            return "redirect:/student/my-results";
        } catch (IllegalStateException e) {
            logger.warn("Error submitting attempt ID {} (student {}): {}", attemptId, student.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/student/attempts/" + attemptId + "/result";
        } catch (SecurityException e) {
            logger.warn("Security error submitting attempt ID {} for student {}: {}", attemptId, student.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/student/my-results";
        } catch (Exception e) {
            logger.error("Unexpected error submitting attempt ID {} for student {}: {}", attemptId, student.getUsername(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred while submitting your exam.");
            return "redirect:/student/my-results";
        }
    }
    @GetMapping("/attempts/{attemptId}/result")
    public String showAttemptResult(@PathVariable Long attemptId, Model model,
                                    @AuthenticationPrincipal User student, RedirectAttributes redirectAttributes) {
        try {
            logger.debug("Student {} (ID: {}) requesting result for attempt ID: {}", student.getUsername(), student.getId(), attemptId);
            ExamAttempt attempt = examAttemptService.getAttemptByIdAndUser(attemptId, student);

            if (attempt.getSubmissionTime() == null) {
                logger.warn("Attempt ID {} accessed for result by student {} (ID: {}) but not yet submitted.",
                            attemptId, student.getUsername(), student.getId());
                redirectAttributes.addFlashAttribute("infoMessage", "This exam attempt has not been completed or submitted yet.");
                return "redirect:/student/attempts/" + attemptId + "/conduct";
            }

            Exam exam = attempt.getExam();
            if (exam == null) {
                logger.error("Critical error: Exam object is null for submitted attempt ID {} by student {} (ID: {}).",
                            attemptId, student.getUsername(), student.getId());
                redirectAttributes.addFlashAttribute("errorMessage", "Error displaying result: Core exam details are missing for this attempt.");
                return "redirect:/student/my-results";
            }
            
            List<Question> questionsForThisExam = questionService.getQuestionsByExamId(exam.getId());
            model.addAttribute("totalQuestionsInExam", questionsForThisExam.size());
            List<StudentAnswer> studentAnswers = studentAnswerRepository.findByExamAttemptId(attempt.getId());
            
            model.addAttribute("attempt", attempt);
            model.addAttribute("exam", exam);
            model.addAttribute("studentAnswers", studentAnswers);
            model.addAttribute("pageTitle", "Exam Result & Review: " + exam.getTitle());

            return "student/exam_result_summary";
        } catch (NoSuchElementException e) {
            logger.warn("Attempt result request: Exam attempt ID {} not found for student {} (ID: {}).",
                        attemptId, student.getUsername(), student.getId(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Exam attempt or result not found.");
            return "redirect:/student/my-results";
        } catch (SecurityException e) {
            logger.warn("Security exception for student {} (ID: {}) trying to access result of attempt ID {}: {}",
                        student.getUsername(), student.getId(), attemptId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/student/my-results";
        }
    }

    @GetMapping("/my-results")
    public String listMyResults(@AuthenticationPrincipal User student, Model model) {
        logger.debug("Student {} (ID: {}) requesting 'My Results' page.", student.getUsername(), student.getId());
        List<ExamAttempt> attempts = examAttemptService.getAttemptsForUser(student);
        model.addAttribute("attempts", attempts);
        model.addAttribute("pageTitle", "My Exam History");
        return "student/my_results_list";
    }
}