package com.exam.online_exam_system.controller;

import java.util.List;
import java.util.NoSuchElementException; 

import org.slf4j.Logger;
import org.slf4j.LoggerFactory; 
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

import com.exam.online_exam_system.model.Course;
import com.exam.online_exam_system.model.Exam;
import com.exam.online_exam_system.service.CourseService;
import com.exam.online_exam_system.service.ExamService;

import jakarta.validation.Valid;
@Controller
@RequestMapping("/exams") 
public class ExamController {
    private static final Logger logger = LoggerFactory.getLogger(ExamController.class);
    @Autowired 
    private ExamService examService; 
    @Autowired 
    private CourseService courseService;
    @GetMapping
    public String listExams(Model model) {
        logger.debug("Listing all exams.");
        List<Exam> exams = examService.getAllExams();
        model.addAttribute("exams", exams);
        model.addAttribute("pageTitle", "Available Exams");
        return "exam_list";
    }
    @GetMapping("/create")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')") // <<< ANNOTATION
    public String showCreateExamForm(Model model) {
        logger.debug("Showing create exam form.");
        model.addAttribute("exam", new Exam());
        model.addAttribute("allCourses", courseService.getAllCourses());
        model.addAttribute("pageTitle", "Create New Exam");
        return "exam_form";
    }
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public String showEditExamForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        logger.debug("Showing edit exam form for exam ID: {}", id);
        try {
            Exam exam = examService.getExamById(id);
            model.addAttribute("exam", exam);
            model.addAttribute("allCourses", courseService.getAllCourses());
            model.addAttribute("pageTitle", "Edit Exam");
            return "exam_form";
        } catch (NoSuchElementException e) {
            logger.warn("Exam not found for edit with ID: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", "Exam not found with ID: " + id);
            return "redirect:/exams";
        }
    }
    @PostMapping("/save")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public String saveExam(@Valid @ModelAttribute("exam") Exam exam, BindingResult result,
                           Model model, RedirectAttributes redirectAttributes) {
        logger.debug("Attempting to save exam: {}", exam.getTitle());
        if (exam.getCourse() != null && exam.getCourse().getId() != null) {
            try {
                Course selectedCourse = courseService.getCourseById(exam.getCourse().getId());
                exam.setCourse(selectedCourse);
            } catch (NoSuchElementException e) {
                logger.warn("Selected course ID {} not found for exam '{}'", exam.getCourse().getId(), exam.getTitle());
                result.rejectValue("course", "course.invalid", "The selected course is invalid.");
            }
        } else {
            exam.setCourse(null); // If no course ID submitted, explicitly set to null
        }
        if (result.hasErrors()) {
            logger.warn("Validation errors while saving exam: {}", result.getAllErrors());
            model.addAttribute("pageTitle", exam.getId() == null ? "Create New Exam" : "Edit Exam");
            model.addAttribute("allCourses", courseService.getAllCourses());
            return "exam_form";
        }
        try {
            String action = (exam.getId() == null) ? "created" : "updated";
            if (exam.getId() == null) {
                examService.createExam(exam);
            } else {
                examService.updateExam(exam);
            }
            redirectAttributes.addFlashAttribute("successMessage", "Exam '" + exam.getTitle() + "' " + action + " successfully!");
            logger.info("Exam '{}' {} successfully.", exam.getTitle(), action);
        } catch (Exception e) {
            logger.error("Error saving exam '{}': {}", exam.getTitle(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving exam: " + e.getMessage());
            model.addAttribute("pageTitle", exam.getId() == null ? "Create New Exam" : "Edit Exam");
            model.addAttribute("allCourses", courseService.getAllCourses());
            // model.addAttribute("allTags", tagService.getAllTags()); // Tags removed
            model.addAttribute("exam", exam);
            return "exam_form";
        }
        return "redirect:/exams";
    }
    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public String deleteExam(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        String examTitle = "[Unknown Exam]";
        try {
            Exam examToDelete = examService.getExamById(id);
            examTitle = examToDelete.getTitle();
            examService.deleteExam(id);
            redirectAttributes.addFlashAttribute("successMessage", "Exam '" + examTitle + "' deleted successfully.");
            logger.info("Exam ID {} (Title: '{}') deleted successfully.", id, examTitle);
        } catch (NoSuchElementException e) {
            logger.warn("Attempt to delete non-existent exam or exam already deleted, ID: {}. Error: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Exam not found for deletion with ID: " + id + ". It may have already been deleted.");
        } catch (IllegalStateException e) {
            logger.warn("Could not delete exam ID {} (Title: '{}'). Reason: {}", id, examTitle, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error deleting exam ID {} (Title: '{}'): {}", id, examTitle, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting exam '" + examTitle + "': An unexpected issue occurred.");
        }
        return "redirect:/exams";
    }
}