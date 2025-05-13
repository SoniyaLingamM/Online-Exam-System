package com.exam.online_exam_system.service;

import com.exam.online_exam_system.model.Role;
import com.exam.online_exam_system.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    public Role getRoleByName(String roleName) {
        return roleRepository.findByName(roleName);
    }
}
