package com.exam.online_exam_system.controller;

import java.util.List; 
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller; 
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; 
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable; 
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping; 
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.exam.online_exam_system.model.Exam;
import com.exam.online_exam_system.model.Question;
import com.exam.online_exam_system.service.ExamService;
import com.exam.online_exam_system.service.QuestionService;

import jakarta.validation.Valid;
@Controller
@RequestMapping("/questions")
@PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')") // Secure all question operations at class level
public class QuestionController {
    @Autowired
    private QuestionService questionService;
    @Autowired
    private ExamService examService; 
    @GetMapping("/exam/{examId}")
    public String listQuestions(@PathVariable Long examId, Model model, RedirectAttributes redirectAttributes) {
        try {
            Exam exam = examService.getExamById(examId); 
            List<Question> questions = questionService.getQuestionsByExamId(examId);
            model.addAttribute("exam", exam); 
            model.addAttribute("questions", questions);
            model.addAttribute("examId", examId); 
            model.addAttribute("pageTitle", "Manage Questions for: " + exam.getTitle());
            return "question_list"; 
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Exam not found with ID: " + examId);
            return "redirect:/exams";
        }
    }
    @GetMapping("/create/{examId}")
    public String showCreateQuestionForm(@PathVariable Long examId, Model model, RedirectAttributes redirectAttributes) {
        try {
            Exam exam = examService.getExamById(examId);
            Question question = new Question();
            question.setExam(exam); 
            model.addAttribute("question", question);
            model.addAttribute("pageTitle", "Add New Question to: " + exam.getTitle());
            model.addAttribute("options", List.of("A", "B", "C", "D"));
            return "question_form"; 
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot add question: Exam not found with ID: " + examId);
            return "redirect:/exams";
        }
    }
    @GetMapping("/edit/{id}")
    public String showEditQuestionForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Question question = questionService.getQuestionById(id);
            model.addAttribute("question", question);
            model.addAttribute("pageTitle", "Edit Question for: " + question.getExam().getTitle());
            model.addAttribute("options", List.of("A", "B", "C", "D"));
            return "question_form";
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Question not found with ID: " + id);
            return "redirect:/exams";
        }
    }
    @PostMapping("/save")
    public String saveQuestion(@Valid @ModelAttribute("question") Question question,
                               BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (question.getExam() == null || question.getExam().getId() == null) {
            result.rejectValue("exam", "exam.required", "The question must be associated with an exam.");
        }
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", (question.getId() == null ? "Add New Question" : "Edit Question"));
            model.addAttribute("options", List.of("A", "B", "C", "D"));
            if (question.getExam() != null && question.getExam().getId() != null) {
                try {
                    Exam exam = examService.getExamById(question.getExam().getId());
                    model.addAttribute("pageTitle", (question.getId() == null ? "Add New Question to: " : "Edit Question for: ") + exam.getTitle());
                } catch (NoSuchElementException e) {
                }
            }
            return "question_form";
        }
        Long examIdToRedirect = null;
        try {
            Exam exam = examService.getExamById(question.getExam().getId());
            question.setExam(exam);
            examIdToRedirect = exam.getId(); 
            if (question.getId() == null) { 
                questionService.createQuestion(question);
                redirectAttributes.addFlashAttribute("successMessage", "Question added successfully to '" + exam.getTitle() + "'!");
            } else { 
                questionService.updateQuestion(question.getId(), question);
                redirectAttributes.addFlashAttribute("successMessage", "Question updated successfully for '" + exam.getTitle() + "'!");
            }
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving question: The associated exam was not found.");
            return "redirect:/exams"; 
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving question: " + e.getMessage());
            return (examIdToRedirect != null) ? "redirect:/questions/exam/" + examIdToRedirect : "redirect:/exams";
        }
        return "redirect:/questions/exam/" + examIdToRedirect;
    }

    @PostMapping("/delete/{id}")
    public String deleteQuestion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Long examId = null; 
        try {
            Question questionToDelete = questionService.getQuestionById(id);
            examId = questionToDelete.getExam().getId();

            questionService.deleteQuestion(id);
            redirectAttributes.addFlashAttribute("successMessage", "Question deleted successfully.");
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Question not found with ID: " + id);
            return "redirect:/exams";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting question: " + e.getMessage());
        }
        return (examId != null) ? "redirect:/questions/exam/" + examId : "redirect:/exams";
    }
}