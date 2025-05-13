package com.exam.online_exam_system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.exam.online_exam_system.model.Result;
import com.exam.online_exam_system.repository.ResultRepository;

@Service
public class ResultServiceImpl implements ResultService {

    @Autowired
    private ResultRepository resultRepository;

    @Override
    public void saveResult(Result result) {
        resultRepository.save(result);  
    }
}
