package com.exam.online_exam_system.service;

import java.util.List;

import com.exam.online_exam_system.model.Question;

public interface QuestionService {
    List<Question> getQuestionsByExamId(Long examId);
    Question createQuestion(Question question);
    Question getQuestionById(Long id); // Added
    Question updateQuestion(Long id, Question questionDetails); // Added
    void deleteQuestion(Long id); // Added
}