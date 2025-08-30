package com.capstone.FeedbackManagement.controller;

import com.capstone.FeedbackManagement.domain.User;
import com.capstone.FeedbackManagement.dto.ChangePasswordRequest;
import com.capstone.FeedbackManagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.capstone.FeedbackManagement.security.JwtService;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {
    @Autowired private UserService userService;

    @PostMapping("/faculty")
    @PreAuthorize("hasRole('ADMIN')")
    public User createFaculty(@RequestBody User body) {
        return userService.createFaculty(body.getFullName(), body.getEmail(), body.getPassword());
    }

    @GetMapping("/faculty")
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> listFaculty() { return userService.listFaculty(); }

    @DeleteMapping("/faculty/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteFaculty(@PathVariable Long id) { userService.deleteUser(id); }


}
