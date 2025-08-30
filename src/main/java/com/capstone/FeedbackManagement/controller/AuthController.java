package com.capstone.FeedbackManagement.controller;

import com.capstone.FeedbackManagement.domain.User;
import com.capstone.FeedbackManagement.dto.JwtResponse;
import com.capstone.FeedbackManagement.dto.LoginRequest;
import com.capstone.FeedbackManagement.dto.RegisterRequest;
import com.capstone.FeedbackManagement.repo.UserRepo;
import com.capstone.FeedbackManagement.security.JwtService;
import com.capstone.FeedbackManagement.service.UserService;
import com.capstone.FeedbackManagement.domain.*;
import com.capstone.FeedbackManagement.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {
    @Autowired private UserService userService;
    @Autowired private AuthenticationManager authManager;
    @Autowired private JwtService jwtService;
    @Autowired private UserRepo userRepo;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        User u = userService.registerStudent(req.fullName(), req.email(), req.password());
        return ResponseEntity.ok().body("Registered: " + u.getEmail());
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        UserDetails principal = (UserDetails) auth.getPrincipal();
        User user = userRepo.findByEmail(principal.getUsername()).orElseThrow();
        String token = jwtService.generateToken(principal);
        return ResponseEntity.ok(new JwtResponse(token, user.getRole().name(), user.getFullName()));
    }


    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestHeader("Authorization") String bearer,
            @RequestBody ChangePasswordRequest req) {
        String email = jwtService.extractUsername(bearer.substring(7));
        userService.changePassword(email, req.oldPassword(), req.newPassword());
        return ResponseEntity.ok("Password updated successfully");
    }

}
