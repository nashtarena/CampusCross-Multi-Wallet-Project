package com.campuscross.wallet.service;

import com.campuscross.wallet.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    public void sendVerificationEmail(User user) {
        String verificationToken = user.getEmail() + "-" + System.currentTimeMillis();
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Verify your CampusCross Wallet account");
        message.setText("Dear " + user.getFullName() + ",\n\n" +
                "Please verify your email address by clicking the link below:\n" +
                "https://campuscross.com/verify?token=" + verificationToken + "\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "Best regards,\n" +
                "CampusCross Team");
        
        try {
            mailSender.send(message);
            log.info("Verification email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", user.getEmail(), e);
        }
    }
    
    public void sendPasswordResetEmail(User user, String resetToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Reset your CampusCross Wallet password");
        message.setText("Dear " + user.getFullName() + ",\n\n" +
                "You requested to reset your password. Click the link below to proceed:\n" +
                "https://campuscross.com/reset-password?token=" + resetToken + "\n\n" +
                "This link will expire in 1 hour.\n\n" +
                "If you didn't request this, please ignore this email.\n\n" +
                "Best regards,\n" +
                "CampusCross Team");
        
        try {
            mailSender.send(message);
            log.info("Password reset email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", user.getEmail(), e);
        }
    }
    
    public void sendTransactionNotification(User user, String transactionId, String type, BigDecimal amount) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("CampusCross Wallet - " + type + " Notification");
        message.setText("Dear " + user.getFullName() + ",\n\n" +
                "Your " + type.toLowerCase() + " of " + amount + " has been processed.\n" +
                "Transaction ID: " + transactionId + "\n\n" +
                "Thank you for using CampusCross Wallet.\n\n" +
                "Best regards,\n" +
                "CampusCross Team");
        
        try {
            mailSender.send(message);
            log.info("Transaction notification sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send transaction notification to: {}", user.getEmail(), e);
        }
    }
    
    public void sendSecurityAlert(User user, String alertMessage) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("CampusCross Wallet - Security Alert");
        message.setText("Dear " + user.getFullName() + ",\n\n" +
                alertMessage + "\n\n" +
                "If this was not you, please secure your account immediately.\n\n" +
                "Best regards,\n" +
                "CampusCross Security Team");
        
        try {
            mailSender.send(message);
            log.info("Security alert sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send security alert to: {}", user.getEmail(), e);
        }
    }
}
