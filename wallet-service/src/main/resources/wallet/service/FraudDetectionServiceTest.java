package com.campuscross.wallet.service;

import com.campuscross.wallet.dto.TransferRequest;
import com.campuscross.wallet.entity.CurrencyAccount;
import com.campuscross.wallet.entity.RiskScore;
import com.campuscross.wallet.entity.Transaction;
import com.campuscross.wallet.entity.User;
import com.campuscross.wallet.entity.Wallet;
import com.campuscross.wallet.repository.CurrencyAccountRepository;
import com.campuscross.wallet.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class FraudDetectionServiceTest {
    
    @Autowired
    private FraudDetectionService fraudDetectionService;
    
    @Autowired
    private P2PTransferService p2pTransferService;
    
    @Autowired
    private WalletService walletService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CurrencyAccountRepository currencyAccountRepository;
    
    @Test
    public void testLargeTransactionDetection() {
        // Create sender and recipient
        User sender = createUserWithBalance("large.sender@uni.edu", "LARGE001", new BigDecimal("10000.00"));
        User recipient = createUser("large.recipient@uni.edu", "LARGE002");
        
        // Create large transfer ($6000 - exceeds $5000 threshold)
        TransferRequest request = TransferRequest.builder()
                .fromUserId(sender.getId())
                .toStudentId(recipient.getStudentId())
                .amount(new BigDecimal("6000.00"))
                .currencyCode("USD")
                .description("Large transfer")
                .build();
        
        Transaction transaction = p2pTransferService.executeTransfer(request);
        
        // Analyze for fraud
        RiskScore riskScore = fraudDetectionService.analyzeTransaction(transaction, sender.getId());
        
        // Verify
        assertNotNull(riskScore);
        assertTrue(riskScore.getRiskScore().compareTo(new BigDecimal("20.00")) > 0);
        assertTrue(riskScore.getRiskFactors().contains("LARGE_AMOUNT"));
        
        System.out.println("✅ Large transaction detected!");
        System.out.println("   Risk Score: " + riskScore.getRiskScore());
        System.out.println("   Risk Level: " + riskScore.getRiskLevel());
        System.out.println("   Factors: " + riskScore.getRiskFactors());
    }
    
    @Test
    public void testVelocityChecking() {
        // Create sender and recipient
        User sender = createUserWithBalance("velocity.sender@uni.edu", "VEL001", new BigDecimal("10000.00"));
        User recipient = createUser("velocity.recipient@uni.edu", "VEL002");
        
        // Execute multiple rapid transfers
        for (int i = 0; i < 12; i++) {
            TransferRequest request = TransferRequest.builder()
                    .fromUserId(sender.getId())
                    .toStudentId(recipient.getStudentId())
                    .amount(new BigDecimal("50.00"))
                    .currencyCode("USD")
                    .description("Rapid transfer " + i)
                    .idempotencyKey("velocity-test-" + i)
                    .build();
            
            p2pTransferService.executeTransfer(request);
        }
        
        // Create one more transaction to analyze
        TransferRequest finalRequest = TransferRequest.builder()
                .fromUserId(sender.getId())
                .toStudentId(recipient.getStudentId())
                .amount(new BigDecimal("50.00"))
                .currencyCode("USD")
                .description("Final transfer")
                .idempotencyKey("velocity-test-final")
                .build();
        
        Transaction transaction = p2pTransferService.executeTransfer(finalRequest);
        
        // Analyze for fraud
        RiskScore riskScore = fraudDetectionService.analyzeTransaction(transaction, sender.getId());
        
        // Verify velocity detected
        assertNotNull(riskScore);
        assertTrue(riskScore.getRiskFactors().contains("HIGH_VELOCITY"));
        
        System.out.println("✅ High velocity detected!");
        System.out.println("   Risk Score: " + riskScore.getRiskScore());
        System.out.println("   Risk Level: " + riskScore.getRiskLevel());
    }
    
    @Test
    public void testNormalTransactionLowRisk() {
        // Create sender and recipient
        User sender = createUserWithBalance("normal.sender@uni.edu", "NORM001", new BigDecimal("1000.00"));
        User recipient = createUser("normal.recipient@uni.edu", "NORM002");
        
        // Normal small transfer
        TransferRequest request = TransferRequest.builder()
                .fromUserId(sender.getId())
                .toStudentId(recipient.getStudentId())
                .amount(new BigDecimal("50.00"))
                .currencyCode("USD")
                .description("Normal transfer")
                .build();
        
        Transaction transaction = p2pTransferService.executeTransfer(request);
        
        // Analyze for fraud
        RiskScore riskScore = fraudDetectionService.analyzeTransaction(transaction, sender.getId());
        
        // Verify low risk
        assertNotNull(riskScore);
        assertEquals("LOW", riskScore.getRiskLevel());
        assertTrue(riskScore.getRiskScore().compareTo(new BigDecimal("25.00")) < 0);
        
        System.out.println("✅ Normal transaction - Low risk!");
        System.out.println("   Risk Score: " + riskScore.getRiskScore());
        System.out.println("   Risk Level: " + riskScore.getRiskLevel());
    }
    
    @Test
    public void testManualReviewWorkflow() {
        // Create sender and recipient
        User sender = createUserWithBalance("review.sender@uni.edu", "REV001", new BigDecimal("10000.00"));
        User recipient = createUser("review.recipient@uni.edu", "REV002");
        
        // Execute multiple transfers to trigger velocity + large amount
        for (int i = 0; i < 12; i++) {
            TransferRequest req = TransferRequest.builder()
                    .fromUserId(sender.getId())
                    .toStudentId(recipient.getStudentId())
                    .amount(new BigDecimal("100.00"))
                    .currencyCode("USD")
                    .idempotencyKey("review-" + i)
                    .build();
            p2pTransferService.executeTransfer(req);
        }
        
        // Large transfer requiring review (velocity + large amount = HIGH risk)
        TransferRequest request = TransferRequest.builder()
                .fromUserId(sender.getId())
                .toStudentId(recipient.getStudentId())
                .amount(new BigDecimal("6000.00"))
                .currencyCode("USD")
                .description("Large transfer needing review")
                .build();
        
        Transaction transaction = p2pTransferService.executeTransfer(request);
        RiskScore riskScore = fraudDetectionService.analyzeTransaction(transaction, sender.getId());
        
        // Check if needs review
        boolean needsReview = fraudDetectionService.needsManualReview(riskScore);
        
        System.out.println("   Risk Score: " + riskScore.getRiskScore());
        System.out.println("   Risk Level: " + riskScore.getRiskLevel());
        System.out.println("   Needs Review: " + needsReview);
        
        assertTrue(needsReview, "High risk transaction should need manual review");
        
        // Approve the transaction
        Long reviewerId = 999L; // Admin user ID
        fraudDetectionService.approveTransaction(riskScore.getId(), reviewerId);
        
        System.out.println("✅ Manual review workflow working!");
    }
    
    // Helper methods
    private User createUserWithBalance(String email, String studentId, BigDecimal balance) {
        User user = User.builder()
                .email(email)
                .name("Test User")
                .studentId(studentId)
                .mobileNumber("+1" + System.currentTimeMillis())
                .build();
        user = userRepository.save(user);
        
        Wallet wallet = walletService.createWallet(user.getId());
        
        CurrencyAccount usdAccount = currencyAccountRepository
                .findByWalletIdAndCurrencyCode(wallet.getId(), "USD")
                .orElseThrow();
        usdAccount.setBalance(balance);
        currencyAccountRepository.save(usdAccount);
        
        return user;
    }
    
    private User createUser(String email, String studentId) {
        User user = User.builder()
                .email(email)
                .name("Test User")
                .studentId(studentId)
                .mobileNumber("+1" + System.currentTimeMillis())
                .build();
        user = userRepository.save(user);
        walletService.createWallet(user.getId());
        return user;
    }
}