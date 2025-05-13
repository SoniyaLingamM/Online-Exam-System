package com.exam.online_exam_system.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exam.online_exam_system.dto.UserRegistrationDto;
import com.exam.online_exam_system.model.User;
import com.exam.online_exam_system.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User findByUsername(String username) {
        logger.debug("Finding user by username: {}", username);
        return userRepository.findByUsername(username);
    }

    @Override
    public User findById(Long id) {
        logger.debug("Finding user by ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", id);
                    return new NoSuchElementException("User not found with id: " + id);
                });
    }

    @Override
    @Transactional
    public void registerUser(UserRegistrationDto registrationDto) {
        logger.info("Attempting to register user with username: {}", registrationDto.getUsername());

        if (registrationDto.getUsername() == null || registrationDto.getUsername().trim().isEmpty()) {
            logger.warn("Registration failed: Username is empty or null.");
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (registrationDto.getPassword() == null || registrationDto.getPassword().isEmpty()) {
            logger.warn("Registration failed for {}: Password is empty or null.", registrationDto.getUsername());
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        logger.debug("Initial DTO validation (username/password presence) passed for {}.", registrationDto.getUsername());

        if (userRepository.findByUsername(registrationDto.getUsername()) != null) {
            logger.warn("Registration failed: Username '{}' already exists.", registrationDto.getUsername());
            throw new RuntimeException("Username already exists: " + registrationDto.getUsername());
        }
        logger.info("Username {} is available.", registrationDto.getUsername());

        if (registrationDto.getPassword() != null && registrationDto.getConfirmPassword() != null &&
            !registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            logger.warn("Registration failed for {}: Passwords do not match.", registrationDto.getUsername());
            throw new RuntimeException("Passwords do not match");
        }
        logger.info("Passwords match for {}.", registrationDto.getUsername());

        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        logger.info("Password encoded for {}.", user.getUsername());

        String role = registrationDto.getRole();
        if (role == null || role.trim().isEmpty()) {
            logger.warn("Registration failed for {}: Role is empty or null.", registrationDto.getUsername());
            throw new IllegalArgumentException("Role cannot be empty.");
        }
        role = role.toUpperCase();
        if (!role.equals("STUDENT") && !role.equals("INSTRUCTOR")) {
            logger.warn("Registration failed for {}: Invalid role '{}'. Must be STUDENT or INSTRUCTOR.", registrationDto.getUsername(), role);
            throw new IllegalArgumentException("Invalid role for registration: " + role + ". Must be STUDENT or INSTRUCTOR.");
        }
        user.setRole(role);
        user.setEnabled(true);

        logger.info("User object prepared for saving: Username='{}', Role='{}', Enabled={}", 
                    user.getUsername(), user.getRole(), user.isEnabled());

        try {
            logger.info("Attempting to save user: {} to database.", user.getUsername());
            User savedUser = userRepository.save(user); 
            logger.info("Successfully SAVED user: {} with ID: {}. User details from DB after save: Username='{}', Role='{}', Enabled={}",
                        savedUser.getUsername(), savedUser.getId(), savedUser.getUsername(), savedUser.getRole(), savedUser.isEnabled());
            
            User foundUser = userRepository.findById(savedUser.getId()).orElse(null);
            if (foundUser != null) {
                logger.info("VERIFIED: User {} (ID: {}) found in DB immediately after save via findById. Role: {}", 
                            foundUser.getUsername(), foundUser.getId(), foundUser.getRole());
            } else {
                logger.warn("VERIFICATION FAILED: User {} (ID: {}) NOT found in DB immediately after save via findById.", 
                            savedUser.getUsername(), savedUser.getId());
            }

        } catch (DataIntegrityViolationException e) {
            Throwable rootCause = e.getMostSpecificCause();
            String sqlState = "N/A"; 
            int errorCode = 0;      
            String rootCauseMessage = (rootCause != null) ? rootCause.getMessage() : "N/A";

            if (rootCause instanceof java.sql.SQLException sqlEx) {
                sqlState = sqlEx.getSQLState();
                errorCode = sqlEx.getErrorCode();
            }
            logger.error("DATABASE INTEGRITY ERROR during user save for username {}: {}. Root cause: {}. SQL State: {}, Error Code: {}", 
                         user.getUsername(), e.getMessage(), rootCauseMessage, sqlState, errorCode, e);
            throw new RuntimeException("Could not save user due to a database integrity issue. Username or other unique field might already exist.", e);
        } catch (Exception e) {
            logger.error("UNEXPECTED DATABASE ERROR during user save for username {}: {}", 
                         user.getUsername(), e.getMessage(), e);
            throw new RuntimeException("Could not save user to the database due to an unexpected error. Please check logs.", e);
        }
        logger.info("registerUser method for {} completed successfully.", registrationDto.getUsername());
    }

    @Override
    @Transactional
    public User createUserByAdmin(User user) {
        logger.info("Admin attempting to create user: {}", user.getUsername());
        if (userRepository.findByUsername(user.getUsername()) != null) {
            logger.warn("Admin creation failed: Username {} already exists.", user.getUsername());
            throw new RuntimeException("Username already exists: " + user.getUsername());
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()){
            logger.warn("Admin creation failed for {}: Password is required.", user.getUsername());
            throw new IllegalArgumentException("Password is required when admin creates user.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            logger.warn("Admin creation failed for {}: Role is required.", user.getUsername());
            throw new IllegalArgumentException("Role cannot be empty when admin creates user.");
        }
        user.setRole(user.getRole().toUpperCase());
        user.setEnabled(user.isEnabled()); 
        
        logger.info("Admin creating user object: Username='{}', Role='{}', Enabled={}", user.getUsername(), user.getRole(), user.isEnabled());
        User savedUser = userRepository.save(user);
        logger.info("Admin successfully created user: {} with ID: {}", savedUser.getUsername(), savedUser.getId());
        return savedUser;
    }

    @Override
    @Transactional
    public void updateUser(User userUpdates) {
        logger.info("Attempting to update user with ID: {}", userUpdates.getId());
        User existingUser = findById(userUpdates.getId());

        if (!existingUser.getUsername().equals(userUpdates.getUsername())) {
            logger.info("Username change detected for user ID {}: from '{}' to '{}'", 
                        existingUser.getId(), existingUser.getUsername(), userUpdates.getUsername());
            if (userRepository.findByUsername(userUpdates.getUsername()) != null) {
                logger.warn("Update failed for user ID {}: New username '{}' already exists.", 
                            existingUser.getId(), userUpdates.getUsername());
                throw new RuntimeException("Cannot update user. Username '" + userUpdates.getUsername() + "' already exists.");
            }
            existingUser.setUsername(userUpdates.getUsername());
        }


        if (userUpdates.getPassword() != null && !userUpdates.getPassword().trim().isEmpty()) {
            logger.debug("New password provided for user ID: {}. Encoding and updating.", existingUser.getId());
            existingUser.setPassword(passwordEncoder.encode(userUpdates.getPassword().trim()));
        } else {
            logger.debug("No new password provided for user ID: {}. Password remains unchanged.", existingUser.getId());
        }

        if (userUpdates.getRole() != null && !userUpdates.getRole().trim().isEmpty()) {
            existingUser.setRole(userUpdates.getRole().toUpperCase());
        }
        existingUser.setEnabled(userUpdates.isEnabled()); 

        userRepository.save(existingUser);
        logger.info("Successfully updated user ID: {}. New details: Username='{}', Role='{}', Enabled={}", 
                    existingUser.getId(), existingUser.getUsername(), existingUser.getRole(), existingUser.isEnabled());
    }

    @Override
    @Transactional
    public void deleteUserById(Long id) {
        logger.info("Attempting to delete user with ID: {}", id);
        if (!userRepository.existsById(id)) {
            logger.warn("Cannot delete. User not found with ID: {}", id);
            throw new NoSuchElementException("Cannot delete. User not found with id: " + id);
        }
        userRepository.deleteById(id);
        logger.info("Successfully deleted user with ID: {}", id);
    }

    @Override
    public List<User> findAllUsers() {
        logger.debug("Fetching all users.");
        return userRepository.findAll();
    }
}