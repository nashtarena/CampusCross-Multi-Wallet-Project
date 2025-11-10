package com.campuscross.wallet.controller;

import com.campuscross.wallet.entity.User;
import com.campuscross.wallet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserRepository userRepository;
    
    /**
     * Create a new user
     * POST /api/v1/users
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        log.info("Creating user: {}", user.getEmail());
        User savedUser = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }
    
    /**
     * Get user by ID
     * GET /api/v1/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        log.info("Fetching user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        return ResponseEntity.ok(user);
    }
    
    /**
     * Get user by email
     * GET /api/v1/users/email/{email}
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        log.info("Fetching user with email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return ResponseEntity.ok(user);
    }
    
    /**
     * Get user by student ID
     * GET /api/v1/users/student/{studentId}
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<User> getUserByStudentId(@PathVariable String studentId) {
        log.info("Fetching user with student ID: {}", studentId);
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("User not found with student ID: " + studentId));
        return ResponseEntity.ok(user);
    }
    
    /**
     * Get all users
     * GET /api/v1/users
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("Fetching all users");
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }
}