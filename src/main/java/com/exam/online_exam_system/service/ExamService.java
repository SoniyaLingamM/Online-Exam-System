package com.exam.online_exam_system.service;

import java.util.List; 

import com.exam.online_exam_system.model.Exam; 

public interface ExamService {

    Exam createExam(Exam exam);

    List<Exam> getAllExams();

    Exam getExamById(Long id);

    void updateExam(Exam exam); 

    void deleteExam(Long id);   
}