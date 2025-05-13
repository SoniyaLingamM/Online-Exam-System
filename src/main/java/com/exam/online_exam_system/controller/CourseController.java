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
import org.springframework.web.bind.annotation.ModelAttribute; // For @PathVariable
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.exam.online_exam_system.model.Course;
import com.exam.online_exam_system.service.CourseService;

import jakarta.validation.Valid;
@Controller
@RequestMapping("/courses")
@PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')") // Secure course management
public class CourseController {
    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);
    @Autowired
    private CourseService courseService;
    @GetMapping
    public String showCoursesPage(Model model) {
        logger.debug("Accessing courses page");
        List<Course> courseList = courseService.getAllCourses();
        model.addAttribute("courses", courseList);
        if (!model.containsAttribute("newCourse")) {
             model.addAttribute("newCourse", new Course());
        }
        model.addAttribute("pageTitle", "Manage Courses");
        return "course_management";
    }
    @PostMapping("/add")
    public String addCourse(@Valid @ModelAttribute("newCourse") Course course,
                            BindingResult result,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        if (result.hasErrors()) {
            logger.warn("Validation errors found while adding course: {}", result.getAllErrors());
            List<Course> courseList = courseService.getAllCourses(); // Repopulate list
            model.addAttribute("courses", courseList);
            model.addAttribute("pageTitle", "Manage Courses");
            return "course_management";
        }
        try {
            courseService.saveCourse(course);
            redirectAttributes.addFlashAttribute("successMessage", "Course '" + course.getName() + "' added successfully!");
        } catch (Exception e) { // Catch specific exceptions like IllegalArgumentException for duplicates
            logger.error("Error adding course", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding course: " + e.getMessage());
            redirectAttributes.addFlashAttribute("newCourse", course); // Send back failed course data
        }
        return "redirect:/courses";
    }
    @GetMapping("/edit/{id}")
    public String showEditCourseForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Course course = courseService.getCourseById(id);
            model.addAttribute("courseToEdit", course); // Use a different attribute name for clarity
            model.addAttribute("pageTitle", "Edit Course");
            return "course_edit_form"; // Assuming a new template course_edit_form.html
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Course not found.");
            return "redirect:/courses";
        }
    }
    @PostMapping("/update")
    public String updateCourse(@Valid @ModelAttribute("courseToEdit") Course course,
                             BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Course");
            return "course_edit_form";
        }
        try {
            courseService.saveCourse(course); // saveCourse can handle updates if ID is present
            redirectAttributes.addFlashAttribute("successMessage", "Course updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating course: " + e.getMessage());
            model.addAttribute("pageTitle", "Edit Course");
            model.addAttribute("courseToEdit", course);
            return "course_edit_form";
        }
        return "redirect:/courses";
    }
    @PostMapping("/delete/{id}")
    public String deleteCourse(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Course course = courseService.getCourseById(id); // Get name for message
            courseService.deleteCourseById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Course '" + course.getName() + "' deleted successfully.");
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Course not found.");
        } catch (Exception e) { // Catch DataIntegrityViolationException if FK prevents deletion
            logger.error("Error deleting course ID {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting course: " + e.getMessage() + ". It might be in use.");
        }
        return "redirect:/courses";
    }
}