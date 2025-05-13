package com.exam.online_exam_system.repository;

import java.util.List;

import org.springframework.data.domain.Sort; 
import org.springframework.data.jpa.repository.JpaRepository;

import com.exam.online_exam_system.model.ExamAttempt;
import com.exam.online_exam_system.model.User;

public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, Long> {
    List<ExamAttempt> findByUser(User user, Sort sort);

    List<ExamAttempt> findByExamId(Long examId);

}