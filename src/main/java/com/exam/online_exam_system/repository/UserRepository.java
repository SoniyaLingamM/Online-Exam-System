package com.exam.online_exam_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.exam.online_exam_system.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
