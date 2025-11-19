package com.campuscross.wallet.service;

import com.campuscross.wallet.entity.User;
import com.campuscross.wallet.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    
    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final WalletService walletService;
    
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    
    @PostConstruct
    public void init() {
        // Update existing users with null KYC status
        updateUsersWithNullKycStatus();
    }
    
    private void updateUsersWithNullKycStatus() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (user.getKycStatus() == null) {
                user.setKycStatus(User.KycStatus.NOT_STARTED);
                userRepository.save(user);
                log.info("Updated KYC status for user: {}", user.getEmail());
            }
        }
    }
    private static final int LOCK_DURATION_HOURS = 24;
    
    @Transactional
    public User registerUser(String email, String password, String firstName, String lastName, 
                           String phoneNumber, String studentId, String campusName, String role) {
        
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }
        
        if (phoneNumber != null && userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new RuntimeException("Phone number already registered");
        }
        
        if (studentId != null && userRepository.existsByStudentId(studentId)) {
            throw new RuntimeException("Student ID already registered");
        }
        
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(phoneNumber)
                .studentId(studentId)
                .campusName(campusName)
                .role(determineUserRole(role))
                .status(User.UserStatus.PENDING_VERIFICATION)
                .kycStatus(User.KycStatus.NOT_STARTED)
                .build();
        
        user = userRepository.save(user);
        
        // Create default wallet for user
        walletService.createDefaultWallet(user);
        
        // Send verification email
        emailService.sendVerificationEmail(user);
        
        log.info("User registered successfully: {}", email);
        return user;
    }
    
    @Transactional
    public User authenticateUser(String studentId, String password, String ipAddress) {
        
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        
        if (!user.canAttemptLogin()) {
            log.warn("Login attempt on locked/inactive account: {}", studentId);
            throw new RuntimeException("Account is locked or inactive");
        }
        
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            handleFailedLogin(user);
            throw new RuntimeException("Invalid credentials");
        }
        
        // Reset failed attempts on successful login
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        user.setLastLoginAt(LocalDateTime.now());
        
        userRepository.save(user);
        
        log.info("User authenticated successfully: {} from IP: {}", studentId, ipAddress);
        return user;
    }
    
    @Transactional
    public void verifyEmail(String token) {
        // In a real implementation, you would validate the token
        // For now, we'll find the user by email token (simplified)
        User user = userRepository.findByEmail(token.substring(0, token.indexOf("-")))
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));
        
        user.setEmailVerified(true);
        
        if (user.getStatus() == User.UserStatus.PENDING_VERIFICATION) {
            user.setStatus(User.UserStatus.ACTIVE);
        }
        
        userRepository.save(user);
    }
    
    @Transactional
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        
        userRepository.save(user);
    }
    
    private void handleFailedLogin(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        
        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            user.setAccountLockedUntil(LocalDateTime.now().plusHours(LOCK_DURATION_HOURS));
        }
        
        userRepository.save(user);
    }

    private User.UserRole determineUserRole(String role) {
        if (role == null || role.isBlank()) {
            return User.UserRole.STUDENT;
        }
        try {
            return User.UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return User.UserRole.STUDENT;
        }
    }
}
