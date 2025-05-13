package com.exam.online_exam_system.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.exam.online_exam_system.model.User;

@Controller
@RequestMapping("/instructor")
public class InstructorDashboardController {
    @GetMapping("/dashboard")
    public String instructorDashboard(Model model, @AuthenticationPrincipal User user) {
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("pageTitle", "Instructor Dashboard");
        model.addAttribute("username", user.getUsername());
        return "instructor/dashboard"; 
    }
}