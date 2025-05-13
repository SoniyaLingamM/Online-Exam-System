package com.exam.online_exam_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.exam.online_exam_system.model.Exam;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
}
