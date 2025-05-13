package com.exam.online_exam_system.validation; 

import java.time.LocalDateTime; 

import com.exam.online_exam_system.model.Exam;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
public class ExamTimeWindowValidator implements ConstraintValidator<ExamTimeWindowValid, Exam> {

    @Override
    public void initialize(ExamTimeWindowValid constraintAnnotation) {
    }

    @Override
    public boolean isValid(Exam exam, ConstraintValidatorContext context) {
        if (exam == null) {
            return true;
        }

        LocalDateTime startTime = exam.getStartTime();
        LocalDateTime endTime = exam.getEndTime();

        if (startTime != null && endTime != null) {
            if (endTime.isBefore(startTime) || endTime.isEqual(startTime)) {
                return false;
            }
        }

        return true;
    }
}