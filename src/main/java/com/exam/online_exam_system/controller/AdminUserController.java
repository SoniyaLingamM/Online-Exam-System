package com.exam.online_exam_system.controller;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.exam.online_exam_system.model.User;
import com.exam.online_exam_system.service.UserService;
@Controller
@RequestMapping("/admin/users")
public class AdminUserController {
    private static final Logger logger = LoggerFactory.getLogger(AdminUserController.class);
    @Autowired
    private UserService userService;
    @GetMapping
    public String listUsers(Model model) {
        logger.debug("Admin requesting to list all users.");
        List<User> users = userService.findAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("pageTitle", "Manage Users");
        return "admin/user_list"; // Renders templates/admin/user_list.html
    }
    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id,
                             RedirectAttributes redirectAttributes,
                             Authentication authentication) { // Inject Authentication principal
        logger.info("Admin (user: {}) attempting to delete user with ID: {}", authentication.getName(), id);
        User userToDelete;
        try {
            userToDelete = userService.findById(id); // Assign and use this variable
        } catch (NoSuchElementException e) {
            logger.warn("Attempt to delete non-existent user with ID: {}. Requested by admin: {}", id, authentication.getName());
            redirectAttributes.addFlashAttribute("errorMessage", "User not found with ID: " + id + ". Cannot delete.");
            return "redirect:/admin/users";
        }
        String currentAdminUsername = authentication.getName();
        if (userToDelete.getUsername().equals(currentAdminUsername)) {
            logger.warn("Admin {} attempted to delete their own account (ID: {}). Action denied.", currentAdminUsername, id);
            redirectAttributes.addFlashAttribute("errorMessage", "Error: You cannot delete your own account.");
            return "redirect:/admin/users";
        }
        try {
            String deletedUsername = userToDelete.getUsername(); // Get username before deleting
            userService.deleteUserById(id);
            logger.info("User '{}' (ID: {}) deleted successfully by admin: {}", deletedUsername, id, currentAdminUsername);
            redirectAttributes.addFlashAttribute("successMessage", "User '" + deletedUsername + "' has been deleted successfully.");
        } catch (Exception e) {
            logger.error("Error during deletion of user ID: {} by admin: {}. Error: {}", id, currentAdminUsername, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user '" + userToDelete.getUsername() + "': " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
    