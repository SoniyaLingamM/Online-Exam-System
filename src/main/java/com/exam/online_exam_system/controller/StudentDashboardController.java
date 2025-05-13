package com.exam.online_exam_system.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.exam.online_exam_system.model.User; 

@Controller
@RequestMapping("/student")
public class StudentDashboardController {

    @GetMapping("/dashboard")
    public String studentDashboard(Model model, @AuthenticationPrincipal User user) {
        model.addAttribute("pageTitle", "Student Dashboard");
        model.addAttribute("username", user.getUsername());
        return "student/dashboard";
    }
}