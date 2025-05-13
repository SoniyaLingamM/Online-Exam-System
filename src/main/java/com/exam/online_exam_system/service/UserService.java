package com.exam.online_exam_system.service;

import java.util.List;

import com.exam.online_exam_system.dto.UserRegistrationDto;
import com.exam.online_exam_system.model.User;

public interface UserService {
    User findByUsername(String username);
    User findById(Long id); 
    void registerUser(UserRegistrationDto registrationDto); 
    User createUserByAdmin(User user); 
    void updateUser(User user); 
    void deleteUserById(Long id); 
    List<User> findAllUsers(); 
}