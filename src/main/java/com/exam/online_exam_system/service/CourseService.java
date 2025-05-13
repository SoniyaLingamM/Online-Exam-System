package com.exam.online_exam_system.service;

import java.util.List;

import com.exam.online_exam_system.model.Course;

public interface CourseService {
    Course saveCourse(Course course);
    List<Course> getAllCourses();
    Course getCourseById(Long id); 
    void deleteCourseById(Long id); 
}