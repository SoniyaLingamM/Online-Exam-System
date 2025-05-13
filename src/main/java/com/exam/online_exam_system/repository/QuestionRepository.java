package com.exam.online_exam_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.exam.online_exam_system.model.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByExamId(Long examId);
}
