package com.campuscross.wallet.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campuscross.wallet.entity.RiskScore;
import com.campuscross.wallet.entity.Transaction;
import com.campuscross.wallet.repository.RiskScoreRepository;
import com.campuscross.wallet.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionService {
    
    private final TransactionRepository transactionRepository;
    private final RiskScoreRepository riskScoreRepository;
    
    // Thresholds
    private static final int MAX_TRANSACTIONS_PER_HOUR = 10;
    private static final int MAX_TRANSACTIONS_PER_DAY = 50;
    private static final BigDecimal LARGE_TRANSACTION_THRESHOLD = new BigDecimal("5000.00");
    private static final BigDecimal UNUSUAL_AMOUNT_THRESHOLD = new BigDecimal("1000.00");
    
    /**
     * Analyze transaction for fraud risk
     */
    @Transactional
    public RiskScore analyzeTransaction(Transaction transaction, Long userId) {
        log.info("Analyzing transaction {} for fraud risk", transaction.getId());
        
        List<String> riskFactors = new ArrayList<>();
        BigDecimal riskScore = BigDecimal.ZERO;
        
        // 1. Check velocity - transactions in last hour
        long transactionsLastHour = countRecentTransactions(userId, 1);
        if (transactionsLastHour > MAX_TRANSACTIONS_PER_HOUR) {
            riskFactors.add("HIGH_VELOCITY_HOURLY: " + transactionsLastHour + " transactions in last hour");
            riskScore = riskScore.add(new BigDecimal("30.00"));
        }
        
        // 2. Check velocity - transactions in last day
        long transactionsLastDay = countRecentTransactions(userId, 24);
        if (transactionsLastDay > MAX_TRANSACTIONS_PER_DAY) {
            riskFactors.add("HIGH_VELOCITY_DAILY: " + transactionsLastDay + " transactions in last 24 hours");
            riskScore = riskScore.add(new BigDecimal("20.00"));
        }
        
        // 3. Check for large transaction amount
        if (transaction.getAmount().compareTo(LARGE_TRANSACTION_THRESHOLD) > 0) {
            riskFactors.add("LARGE_AMOUNT: Transaction amount " + transaction.getAmount() + 
                    " exceeds threshold " + LARGE_TRANSACTION_THRESHOLD);
            riskScore = riskScore.add(new BigDecimal("25.00"));
        }
        
        // 4. Check for unusual transaction pattern
        BigDecimal avgTransactionAmount = calculateAverageTransactionAmount(userId);
        if (avgTransactionAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal ratio = transaction.getAmount().divide(avgTransactionAmount, 2, java.math.RoundingMode.HALF_UP);
            if (ratio.compareTo(new BigDecimal("5.00")) > 0) {
                riskFactors.add("UNUSUAL_PATTERN: Transaction is " + ratio + "x larger than user's average");
                riskScore = riskScore.add(new BigDecimal("15.00"));
            }
        }
        
        // 5. Check for rapid successive transfers to same recipient
        if ("P2P".equals(transaction.getTransactionType())) {
            long sameRecipientCount = countTransactionsToSameRecipient(transaction.getFromAccountId(), 
                    transaction.getToAccountId(), 1);
            if (sameRecipientCount > 3) {
                riskFactors.add("RAPID_SAME_RECIPIENT: " + sameRecipientCount + 
                        " transfers to same recipient in last hour");
                riskScore = riskScore.add(new BigDecimal("20.00"));
            }
        }
        
        // 6. Check for off-hours activity (midnight to 5 AM)
        int hour = LocalDateTime.now().getHour();
        if (hour >= 0 && hour < 5) {
            riskFactors.add("OFF_HOURS: Transaction at " + hour + ":00");
            riskScore = riskScore.add(new BigDecimal("10.00"));
        }
        
        // Cap risk score at 100
        if (riskScore.compareTo(new BigDecimal("100.00")) > 0) {
            riskScore = new BigDecimal("100.00");
        }
        
        // Determine risk level
        String riskLevel = determineRiskLevel(riskScore);
        
        // Create risk score record
        RiskScore risk = RiskScore.builder()
                .transactionId(transaction.getId())
                .userId(userId)
                .riskScore(riskScore)
                .riskLevel(riskLevel)
                .riskFactors(String.join("; ", riskFactors))
                .status("PENDING")
                .build();
        
        risk = riskScoreRepository.save(risk);
        
        log.info("Risk analysis complete. Score: {}, Level: {}, Factors: {}", 
                riskScore, riskLevel, riskFactors.size());
        
        return risk;
    }
    
    /**
     * Check if transaction should be blocked
     */
    public boolean shouldBlockTransaction(RiskScore riskScore) {
        return "CRITICAL".equals(riskScore.getRiskLevel());
    }
    
    /**
     * Check if transaction needs manual review
     */
    public boolean needsManualReview(RiskScore riskScore) {
        return "HIGH".equals(riskScore.getRiskLevel()) || "CRITICAL".equals(riskScore.getRiskLevel());
    }
    
    /**
     * Count recent transactions for a user
     */
    private long countRecentTransactions(Long userId, int hours) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
        return transactionRepository.findAll().stream()
                .filter(txn -> txn.getCreatedAt().isAfter(cutoff))
                .filter(txn -> {
                    // Check if transaction involves this user
                    return true; // Simplified - in real implementation, would check wallet ownership
                })
                .count();
    }
    
    /**
     * Calculate average transaction amount for a user
     */
    private BigDecimal calculateAverageTransactionAmount(Long userId) {
        List<Transaction> userTransactions = transactionRepository.findAll().stream()
                .filter(txn -> "completed".equals(txn.getStatus()))
                .limit(20) // Last 20 transactions
                .toList();
        
        if (userTransactions.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal total = userTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return total.divide(new BigDecimal(userTransactions.size()), 2, java.math.RoundingMode.HALF_UP);
    }
    
    /**
     * Count transactions to same recipient in recent hours
     */
    private long countTransactionsToSameRecipient(Long fromAccountId, Long toAccountId, int hours) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
        return transactionRepository.findAll().stream()
                .filter(txn -> txn.getCreatedAt().isAfter(cutoff))
                .filter(txn -> fromAccountId.equals(txn.getFromAccountId()) && 
                              toAccountId.equals(txn.getToAccountId()))
                .count();
    }
    
    /**
     * Determine risk level from score
     */
    private String determineRiskLevel(BigDecimal score) {
        if (score.compareTo(new BigDecimal("75.00")) >= 0) {
            return "CRITICAL";
        } else if (score.compareTo(new BigDecimal("50.00")) >= 0) {
            return "HIGH";
        } else if (score.compareTo(new BigDecimal("25.00")) >= 0) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
    
    /**
     * Get pending reviews (for admin dashboard)
     */
    @Transactional(readOnly = true)
    public List<RiskScore> getPendingReviews() {
        return riskScoreRepository.findByStatus("PENDING");
    }
    
    /**
     * Approve a flagged transaction
     */
    @Transactional
    public void approveTransaction(Long riskScoreId, Long reviewerId) {
        RiskScore risk = riskScoreRepository.findById(riskScoreId)
                .orElseThrow(() -> new RuntimeException("Risk score not found"));
        
        risk.setStatus("APPROVED");
        risk.setReviewedAt(LocalDateTime.now());
        risk.setReviewedBy(reviewerId);
        riskScoreRepository.save(risk);
        
        log.info("Transaction {} approved by reviewer {}", risk.getTransactionId(), reviewerId);
    }
    
    /**
     * Reject a flagged transaction
     */
    @Transactional
    public void rejectTransaction(Long riskScoreId, Long reviewerId) {
        RiskScore risk = riskScoreRepository.findById(riskScoreId)
                .orElseThrow(() -> new RuntimeException("Risk score not found"));
        
        risk.setStatus("REJECTED");
        risk.setReviewedAt(LocalDateTime.now());
        risk.setReviewedBy(reviewerId);
        riskScoreRepository.save(risk);
        
        log.info("Transaction {} rejected by reviewer {}", risk.getTransactionId(), reviewerId);
    }
}