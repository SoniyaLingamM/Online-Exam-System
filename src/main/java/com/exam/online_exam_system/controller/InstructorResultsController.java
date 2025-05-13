package com.exam.online_exam_system.controller;

import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired; // Import QuestionService
import org.springframework.security.access.prepost.PreAuthorize; // Import Question model
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.exam.online_exam_system.model.Exam;
import com.exam.online_exam_system.model.ExamAttempt;
import com.exam.online_exam_system.model.Question;
import com.exam.online_exam_system.service.ExamAttemptService;
import com.exam.online_exam_system.service.ExamService;
import com.exam.online_exam_system.service.QuestionService;
@Controller
@RequestMapping("/instructor/results") 
@PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')") 
public class InstructorResultsController {
    private static final Logger logger = LoggerFactory.getLogger(InstructorResultsController.class);
    @Autowired
    private ExamAttemptService examAttemptService;
    @Autowired
    private ExamService examService;
    @Autowired
    private QuestionService questionService; 
    @GetMapping("/exam/{examId}")
    public String viewResultsForExam(@PathVariable Long examId, Model model, RedirectAttributes redirectAttributes) {
        logger.debug("Instructor viewing results for exam ID: {}", examId);
        try {
            Exam exam = examService.getExamById(examId);
            List<ExamAttempt> attempts = examAttemptService.getAttemptsByExamId(examId);
            List<Question> questions = questionService.getQuestionsByExamId(examId); 
            model.addAttribute("exam", exam);
            model.addAttribute("attempts", attempts);
            model.addAttribute("totalQuestionsInExam", questions.size());
            model.addAttribute("pageTitle", "Results for Exam: " + exam.getTitle());
            return "instructor/exam_attempts_list"; 
        } catch (NoSuchElementException e) {
            logger.warn("Attempted to view results for non-existent exam ID: {}", examId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Exam not found with ID: " + examId);
            return "redirect:/exams"; 
        }
    }
    @GetMapping("/all")
    public String viewAllExamAttempts(Model model) {
        logger.debug("Instructor viewing all exam attempts.");
        List<ExamAttempt> attempts = examAttemptService.getAllAttempts();
        model.addAttribute("attempts", attempts);
        model.addAttribute("pageTitle", "All Student Exam Attempts");
        return "instructor/exam_attempts_list";
    }
}