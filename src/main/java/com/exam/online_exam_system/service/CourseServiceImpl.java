package com.exam.online_exam_system.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exam.online_exam_system.model.Course;
import com.exam.online_exam_system.repository.CourseRepository; 

@Service
public class CourseServiceImpl implements CourseService {

    private static final Logger logger = LoggerFactory.getLogger(CourseServiceImpl.class);

    @Autowired
    private CourseRepository courseRepository;

    @Override
    @Transactional
    public Course saveCourse(Course course) {
        logger.info("Saving course: {}", course.getName());
        try {
            Course savedCourse = courseRepository.save(course);
            logger.info("Course saved successfully with ID: {}", savedCourse.getId());
            return savedCourse;
        } catch (Exception e) {
            logger.error("Error saving course {}: {}", course.getName(), e.getMessage(), e);
            throw new RuntimeException("Could not save course: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Course> getAllCourses() {
        logger.debug("Fetching all courses");
        return courseRepository.findAll();
    }

    @Override
    public Course getCourseById(Long id) {
        logger.debug("Fetching course by ID: {}", id);
        return courseRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Course not found with ID: {}", id);
                    return new NoSuchElementException("Course not found with ID: " + id);
                });
    }

    @Override
    @Transactional
    public void deleteCourseById(Long id) {
        logger.info("Attempting to delete course with ID: {}", id);
        if (!courseRepository.existsById(id)) {
            logger.warn("Cannot delete. Course not found with ID: {}", id);
            throw new NoSuchElementException("Course not found with ID: " + id + " for deletion.");
        }
        courseRepository.deleteById(id);
        logger.info("Successfully deleted course with ID: {}", id);
    }
}