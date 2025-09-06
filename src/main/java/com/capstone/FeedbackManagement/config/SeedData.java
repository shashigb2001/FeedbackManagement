package com.capstone.FeedbackManagement.config;

import com.capstone.FeedbackManagement.domain.Role;
import com.capstone.FeedbackManagement.domain.User;
import com.capstone.FeedbackManagement.repo.UserRepo;
import com.capstone.FeedbackManagement.domain.*;
import com.capstone.FeedbackManagement.repo.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SeedData {

    @Bean
    CommandLineRunner seed(UserRepo users, PasswordEncoder encoder) {
        return args -> {
            if (users.findByEmail("admin@gmail.com").isEmpty()) {
                users.save(User.builder().email("admin@gmail.com").fullName("Admin").password(encoder.encode("admin123")).role(Role.ADMIN).enabled(true).build());
            }

        };
    }
}
