package com.exam.online_exam_system.config; 
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpServletRequest;
@ControllerAdvice 
public class GlobalControllerAdvice {
    @ModelAttribute("httpServletRequest") // The name it will be available as in the model
    public HttpServletRequest httpServletRequest(HttpServletRequest request) {
        return request;
    }
}