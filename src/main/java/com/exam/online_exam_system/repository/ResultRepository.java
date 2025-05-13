package com.exam.online_exam_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.exam.online_exam_system.model.Result;

public interface ResultRepository extends JpaRepository<Result, Long> {
    List<Result> findByUserId(Long userId);
}
