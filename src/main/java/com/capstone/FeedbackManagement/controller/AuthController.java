package com.capstone.FeedbackManagement.controller;

import com.capstone.FeedbackManagement.domain.User;
import com.capstone.FeedbackManagement.dto.ChangePasswordRequest;
import com.capstone.FeedbackManagement.dto.JwtResponse;
import com.capstone.FeedbackManagement.dto.RegisterRequest;
import com.capstone.FeedbackManagement.security.JwtService;
import com.capstone.FeedbackManagement.security.MyUserDetailsService;
import com.capstone.FeedbackManagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    @Autowired private AuthenticationManager authManager;
    @Autowired private UserService userService;
    @Autowired private JwtService jwtService;
    @Autowired private MyUserDetailsService myUserDetailsService;

    private User authenticateAndFetchUser(String email, String password) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));
        UserDetails ud = (UserDetails) auth.getPrincipal();
        return userService.getByEmail(ud.getUsername());
    }

    @PostMapping("/login/student")
    public ResponseEntity<?> loginStudent(@RequestBody RegisterRequest req) {
        User user = authenticateAndFetchUser(req.email(), req.password());
        if (!"STUDENT".equalsIgnoreCase(user.getRole().name())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Forbidden", "message", "Not a student account"));
        }
        UserDetails ud = myUserDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(ud);
        return ResponseEntity.ok(new JwtResponse(token, user.getRole().name(), user.getFullName()));
    }

    @PostMapping("/login/faculty")
    public ResponseEntity<?> loginFaculty(@RequestBody RegisterRequest req) {
        User user = authenticateAndFetchUser(req.email(), req.password());
        if (!"FACULTY".equalsIgnoreCase(user.getRole().name())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Forbidden", "message", "Not a faculty account"));
        }
        UserDetails ud = myUserDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(ud);
        return ResponseEntity.ok(new JwtResponse(token, user.getRole().name(), user.getFullName()));
    }

    @PostMapping("/login/admin")
    public ResponseEntity<?> loginAdmin(@RequestBody RegisterRequest req) {
        User user = authenticateAndFetchUser(req.email(), req.password());
        if (!"ADMIN".equalsIgnoreCase(user.getRole().name())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Forbidden", "message", "Not an admin account"));
        }
        UserDetails ud = myUserDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(ud);
        return ResponseEntity.ok(new JwtResponse(token, user.getRole().name(), user.getFullName()));
    }


    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(@RequestBody RegisterRequest req) {
        User created = userService.registerStudent(req.fullName(), req.email(), req.password());
        UserDetails userDetails = myUserDetailsService.loadUserByUsername(created.getEmail());
        String token = jwtService.generateToken(userDetails);
        return ResponseEntity.ok(new JwtResponse(token, created.getRole().name(), created.getFullName()));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestHeader("Authorization") String bearer,
            @RequestBody ChangePasswordRequest req) {
        try {
            String token = bearer.substring(7);
            String email = jwtService.extractUsername(token);

            userService.changePassword(email, req.oldPassword(), req.newPassword());

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Password updated successfully"
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "Bad Request",
                    "message", ex.getMessage()
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Error",
                    "message", "An unexpected error occurred"
            ));
        }
    }


    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        String username;
        try {
            username = jwtService.extractUsername(token);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid token");
        }

        UserDetails userDetails;
        try {
            userDetails = myUserDetailsService.loadUserByUsername(username);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("User not found for token");
        }

        if (jwtService.isTokenValid(token, userDetails)) {
            User user = userService.getByEmail(username);
            return ResponseEntity.ok(new JwtResponse(token, user.getRole().name(), user.getFullName()));
        } else {
            return ResponseEntity.status(401).body("Token expired or invalid");
        }
    }
}
