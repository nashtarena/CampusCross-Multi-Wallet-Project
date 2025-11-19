package com.campuscross.wallet.controller;

import com.campuscross.wallet.dto.RegisterRequest;
import com.campuscross.wallet.entity.User;
import com.campuscross.wallet.service.AuthenticationService;
import com.campuscross.wallet.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final AuthenticationService authenticationService;
    private final JwtUtil jwtUtil;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = authenticationService.registerUser(
                    request.email(),
                    request.password(),
                    request.firstName(),
                    request.lastName(),
                    request.phoneNumber(),
                    request.studentId(),
                    request.campusName(),
                    request.role()
            );
            
            String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole().toString());
            
            return ResponseEntity.ok(new AuthResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getRole().toString(),
                    user.getStatus().toString(),
                    user.getKycStatus() != null ? user.getKycStatus().toString() : "NOT_STARTED",
                    "User registered successfully",
                    token
            ));
        } catch (Exception e) {
            log.error("Registration failed", e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            String ipAddress = getClientIpAddress(httpRequest);
            User user = authenticationService.authenticateUser(request.studentId(), request.password(), ipAddress);
            
            String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole().toString());
            
            return ResponseEntity.ok(new AuthResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getRole().toString(),
                    user.getStatus().toString(),
                    user.getKycStatus() != null ? user.getKycStatus().toString() : "NOT_STARTED",
                    "Login successful",
                    token
            ));
        } catch (Exception e) {
            log.error("Login failed", e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody EmailVerificationRequest request) {
        try {
            authenticationService.verifyEmail(request.token());
            return ResponseEntity.ok(new SuccessResponse("Email verified successfully"));
        } catch (Exception e) {
            log.error("Email verification failed", e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            authenticationService.resetPassword(request.email(), request.newPassword());
            return ResponseEntity.ok(new SuccessResponse("Password reset email sent"));
        } catch (Exception e) {
            log.error("Password reset failed", e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
    
    // Request/Response DTOs
    public record LoginRequest(
            String studentId,
            String password
    ) {}
    
    public record EmailVerificationRequest(
            String token
    ) {}
    
    public record ResetPasswordRequest(
            String email,
            String newPassword
    ) {}
    
    public record AuthResponse(
            String userId,
            String email,
            String fullName,
            String role,
            String status,
            String kycStatus,
            String message,
            String token
    ) {}
    
    public record SuccessResponse(
            String message
    ) {}
    
    public record ErrorResponse(
            String error
    ) {}
}
