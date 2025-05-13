package com.exam.online_exam_system.service;

import java.util.List;
import java.util.Map; 

import com.exam.online_exam_system.model.ExamAttempt;
import com.exam.online_exam_system.model.User;

public interface ExamAttemptService {

    ExamAttempt saveExamAttempt(ExamAttempt examAttempt);

    ExamAttempt startExamAttempt(Long examId, User student);

    ExamAttempt submitExamAttempt(Long attemptId, Map<String, String> submittedAnswers, User student);

    List<ExamAttempt> getAttemptsForUser(User user);

    ExamAttempt getAttemptByIdAndUser(Long attemptId, User user);

    List<ExamAttempt> getAttemptsByExamId(Long examId);

    List<ExamAttempt> getAllAttempts();
}