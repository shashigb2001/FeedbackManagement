package com.capstone.FeedbackManagement.service;

import com.capstone.FeedbackManagement.domain.Role;
import com.capstone.FeedbackManagement.domain.User;
import com.capstone.FeedbackManagement.repo.UserRepo;
import com.capstone.FeedbackManagement.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired private UserRepo userRepo;
    @Autowired private PasswordEncoder encoder;

    public User registerStudent(String fullName, String email, String password) {
        if (userRepo.existsByEmail(email)) throw new RuntimeException("Email already used");
        User u = User.builder().email(email).fullName(fullName).password(encoder.encode(password)).role(Role.STUDENT).enabled(true).build();
        return userRepo.save(u);
    }

    public User createFaculty(String fullName, String email, String password) {
        if (userRepo.existsByEmail(email)) throw new RuntimeException("Email already used");
        User u = User.builder().email(email).fullName(fullName).password(encoder.encode(password)).role(Role.FACULTY).enabled(true).build();
        return userRepo.save(u);
    }

    public List<User> listFaculty() { return userRepo.findAll().stream().filter(u -> u.getRole()==Role.FACULTY).toList(); }

    public void deleteUser(Long id) { userRepo.deleteById(id); }

    public User getByEmail(String email) { return userRepo.findByEmail(email).orElseThrow(); }


    public void changePassword(String email, String oldPassword, String newPassword) {
        User u = getByEmail(email);
        if (!encoder.matches(oldPassword, u.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }
        u.setPassword(encoder.encode(newPassword));
        userRepo.save(u);
    }


}
