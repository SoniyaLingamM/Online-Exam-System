package com.exam.online_exam_system.controller;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.exam.online_exam_system.model.User;
@Controller
@RequestMapping("/admin")
public class AdminDashboardController {
    @GetMapping("/dashboard")
    public String adminDashboard(Model model, @AuthenticationPrincipal User user) {
        model.addAttribute("pageTitle", "Admin Dashboard");
        model.addAttribute("username", user.getUsername());
        return "admin/dashboard"; 
    }
}