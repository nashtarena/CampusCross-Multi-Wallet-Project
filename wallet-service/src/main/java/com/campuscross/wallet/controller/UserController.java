package com.campuscross.wallet.controller;

import com.campuscross.wallet.entity.User;
import com.campuscross.wallet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/by-college/{campusName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUsersByCollege(@PathVariable String campusName) {
        try {
            List<User> users = userRepository.findByCampusName(campusName);

            List<UserResponse> responses = users.stream()
                    .map(user -> new UserResponse(
                            user.getId(),
                            user.getFullName(),
                            user.getEmail(),
                            user.getStudentId(),
                            user.getRole().toString(),
                            user.getKycStatus() != null ? user.getKycStatus().toString() : "NOT_STARTED",
                            user.getCampusName()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Failed to fetch users by college: {}", campusName, e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> getCurrentUserProfile() {
        try {
            // Get user from JWT context (you'll need to extract this from
            // SecurityContextHolder)
            String email = org.springframework.security.core.context.SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getName();

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return ResponseEntity.ok(new UserResponse(
                    user.getId(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getStudentId(),
                    user.getRole().toString(),
                    user.getKycStatus() != null ? user.getKycStatus().toString() : "NOT_STARTED",
                    user.getCampusName()));
        } catch (Exception e) {
            log.error("Failed to fetch user profile", e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/lookup/{identifier}")
    public ResponseEntity<?> lookupUser(@PathVariable String identifier) {
        try {
            // Try to find by student ID first
            Optional<User> user = userRepository.findByStudentId(identifier);

            // If not found, try phone number
            if (user.isEmpty()) {
                user = userRepository.findByPhoneNumber(identifier);
            }

            // If not found, try email
            if (user.isEmpty()) {
                user = userRepository.findByEmail(identifier);
            }

            if (user.isEmpty()) {
                return ResponseEntity.status(404).body(
                        new ErrorResponse("User not found with identifier: " + identifier));
            }

            User foundUser = user.get();
            return ResponseEntity.ok(new UserResponse(
                    foundUser.getId(),
                    foundUser.getFullName(),
                    foundUser.getEmail(),
                    foundUser.getStudentId(),
                    foundUser.getRole().toString(),
                    foundUser.getKycStatus() != null ? foundUser.getKycStatus().toString() : "NOT_STARTED",
                    foundUser.getCampusName()));
        } catch (Exception e) {
            log.error("Failed to lookup user: {}", identifier, e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    public record UserResponse(
            String userId,
            String fullName,
            String email,
            String studentId,
            String role,
            String kycStatus,
            String campusName) {
    }

    public record ErrorResponse(String error) {
    }
}