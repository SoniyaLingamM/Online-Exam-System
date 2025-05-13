package com.exam.online_exam_system.config; 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.exam.online_exam_system.model.User;
import com.exam.online_exam_system.repository.UserRepository;
@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Override
    public void run(String... args) throws Exception {
        logger.info("DataInitializer running to ensure admin user exists...");
        if (userRepository.findByUsername("admin") == null) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin123")); // Use a reasonably secure default for dev
            adminUser.setRole("ADMIN"); // Ensure this string matches what User.getAuthorities() expects
            adminUser.setEnabled(true);
            userRepository.save(adminUser);
            logger.info(">>> Created default ADMIN user: username='admin', password='admin123'");
        } else {
            logger.info("Admin user 'admin' already exists. No action taken by DataInitializer for admin.");
        }
        logger.info("DataInitializer finished.");
    }
}