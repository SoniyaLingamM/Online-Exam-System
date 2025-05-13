package com.exam.online_exam_system.repository; 

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.exam.online_exam_system.model.StudentAnswer;

public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {

    List<StudentAnswer> findByExamAttemptId(Long examAttemptId);
}