package com.exam.online_exam_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.exam.online_exam_system.model.Course;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
}