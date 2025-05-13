package com.exam.online_exam_system.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exam.online_exam_system.model.Exam;
import com.exam.online_exam_system.repository.ExamAttemptRepository;
import com.exam.online_exam_system.repository.ExamRepository;
import com.exam.online_exam_system.repository.QuestionRepository;

@Service
public class ExamServiceImpl implements ExamService {

    private static final Logger logger = LoggerFactory.getLogger(ExamServiceImpl.class);

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private QuestionRepository questionRepository; 

    @Autowired
    private ExamAttemptRepository examAttemptRepository;

    @Override
    @Transactional
    public Exam createExam(Exam exam) {
        logger.info("Creating new exam with title: {}", exam.getTitle());
        return examRepository.save(exam);
    }

    @Override
    public List<Exam> getAllExams() {
        logger.debug("Fetching all exams.");
        return examRepository.findAll();
    }

    @Override
    public Exam getExamById(Long id) {
        logger.debug("Fetching exam by ID: {}", id);
        return examRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Exam not found with ID: {}", id);
                    return new NoSuchElementException("Exam not found with ID: " + id);
                });
    }

    @Override
    @Transactional
    public void updateExam(Exam examDetails) {
        logger.info("Attempting to update exam with ID: {}", examDetails.getId());
        Exam existingExam = getExamById(examDetails.getId());

        existingExam.setTitle(examDetails.getTitle());
        existingExam.setDescription(examDetails.getDescription());
        existingExam.setDurationMinutes(examDetails.getDurationMinutes());
        existingExam.setPassingScore(examDetails.getPassingScore());
        existingExam.setStartTime(examDetails.getStartTime());
        existingExam.setEndTime(examDetails.getEndTime());
        existingExam.setCourse(examDetails.getCourse());

        examRepository.save(existingExam);
        logger.info("Successfully updated exam with ID: {}", existingExam.getId());
    }

    @Override
    @Transactional
    public void deleteExam(Long id) {
        logger.info("Attempting to delete exam with ID: {}", id);
        Exam examToDelete = examRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Cannot delete. Exam not found with ID: {}", id);
                    return new NoSuchElementException("Exam not found with ID: " + id + ". Deletion aborted.");
                });

        if (!examAttemptRepository.findByExamId(id).isEmpty()) {
            logger.warn("Attempt to delete exam ID {} which has student attempts. Deletion prevented.", id);
            throw new IllegalStateException("Cannot delete exam: Students have made attempts for this exam. " +
                                            "Consider archiving or deleting attempts first (if policy allows).");
        }

        if (!questionRepository.findByExamId(id).isEmpty()) {
        }

        examRepository.delete(examToDelete);
        logger.info("Successfully deleted exam '{}' with ID: {}", examToDelete.getTitle(), id);
    }
}