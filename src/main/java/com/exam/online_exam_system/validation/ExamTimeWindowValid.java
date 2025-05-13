package com.exam.online_exam_system.validation; 

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = ExamTimeWindowValidator.class) 
@Target({ ElementType.TYPE }) 
@Retention(RetentionPolicy.RUNTIME) 
public @interface ExamTimeWindowValid { 

    String message() default "Exam end time must be after the start time.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}