package com.campuscross.wallet.service;

import com.campuscross.wallet.entity.Wallet;
import com.campuscross.wallet.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionService {
    
    private final TransactionRepository transactionRepository;
    
    private static final BigDecimal HIGH_VALUE_THRESHOLD = new BigDecimal("10000");
    private static final BigDecimal SUSPICIOUS_FREQUENCY_THRESHOLD = new BigDecimal("5000");
    private static final int MAX_TRANSACTIONS_PER_HOUR = 10;
    private static final int MAX_TRANSACTIONS_PER_DAY = 50;
    
    public boolean isSuspiciousTransaction(Wallet sourceWallet, Wallet targetWallet, 
                                         BigDecimal amount, String ipAddress) {
        
        // Check for high-value transaction
        if (amount.compareTo(HIGH_VALUE_THRESHOLD) > 0) {
            log.warn("High-value transaction detected: {} from wallet {}", 
                    amount, sourceWallet.getWalletAddress());
            return true;
        }
        
        // Check transaction frequency
        if (isHighFrequencyTransaction(sourceWallet.getId())) {
            log.warn("High-frequency transaction detected from wallet {}", 
                    sourceWallet.getWalletAddress());
            return true;
        }
        
        // Check for self-transfer patterns (simplified)
        if (isSelfTransferPattern(sourceWallet, targetWallet)) {
            log.warn("Suspicious self-transfer pattern detected");
            return true;
        }
        
        // Check for rapid multiple transactions to same target
        if (isRapidMultipleTransactions(sourceWallet.getId(), targetWallet.getId())) {
            log.warn("Rapid multiple transactions to same target detected");
            return true;
        }
        
        // Check for round amounts (potential money laundering)
        if (isRoundAmount(amount) && amount.compareTo(SUSPICIOUS_FREQUENCY_THRESHOLD) > 0) {
            log.warn("Suspicious round amount transaction: {}", amount);
            return true;
        }
        
        return false;
    }
    
    public boolean isSuspiciousCampusPayment(Wallet wallet, BigDecimal amount, 
                                           String merchantId, String ipAddress) {
        
        // Check for unusually high campus payment
        if (amount.compareTo(new BigDecimal("1000")) > 0) {
            log.warn("Unusually high campus payment: {} from wallet {}", 
                    amount, wallet.getWalletAddress());
            return true;
        }
        
        // Check if merchant is blacklisted (simplified check)
        if (isBlacklistedMerchant(merchantId)) {
            log.warn("Transaction to blacklisted merchant: {}", merchantId);
            return true;
        }
        
        return false;
    }
    
    public boolean isSuspiciousRemittance(Wallet sourceWallet, Wallet targetWallet, 
                                         BigDecimal amount, String ipAddress) {
        
        // Check for high-value remittance
        if (amount.compareTo(new BigDecimal("5000")) > 0) {
            log.warn("High-value remittance detected: {} from wallet {}", 
                    amount, sourceWallet.getWalletAddress());
            return true;
        }
        
        // Check for cross-border patterns
        if (isCrossBorderPattern(sourceWallet, targetWallet)) {
            log.warn("Cross-border remittance pattern detected");
            return true;
        }
        
        return false;
    }
    
    private boolean isHighFrequencyTransaction(Long walletId) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        
        long transactionsLastHour = transactionRepository.findBySourceWalletId(walletId)
                .stream()
                .filter(t -> t.getCreatedAt().isAfter(oneHourAgo))
                .count();
        
        long transactionsLastDay = transactionRepository.findBySourceWalletId(walletId)
                .stream()
                .filter(t -> t.getCreatedAt().isAfter(oneDayAgo))
                .count();
        
        return transactionsLastHour > MAX_TRANSACTIONS_PER_HOUR || 
               transactionsLastDay > MAX_TRANSACTIONS_PER_DAY;
    }
    
    private boolean isSelfTransferPattern(Wallet sourceWallet, Wallet targetWallet) {
        // Simplified check - in reality, this would be more complex
        return sourceWallet.getUser().getId().equals(targetWallet.getUser().getId()) &&
               sourceWallet.getCurrencyCode().equals(targetWallet.getCurrencyCode());
    }
    
    private boolean isRapidMultipleTransactions(Long sourceWalletId, Long targetWalletId) {
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        
        long recentTransactions = transactionRepository.findBySourceWalletId(sourceWalletId)
                .stream()
                .filter(t -> t.getTargetWallet() != null && 
                           t.getTargetWallet().getId().equals(targetWalletId) &&
                           t.getCreatedAt().isAfter(fiveMinutesAgo))
                .count();
        
        return recentTransactions > 3;
    }
    
    private boolean isRoundAmount(BigDecimal amount) {
        // Check if amount is a round number (e.g., 1000.00, 5000.00)
        return amount.scale() <= 2 && 
               amount.remainder(BigDecimal.valueOf(100)).compareTo(BigDecimal.ZERO) == 0;
    }
    
    private boolean isBlacklistedMerchant(String merchantId) {
        // Simplified check - in reality, this would check against a database
        List<String> blacklistedMerchants = List.of("MERCHANT001", "MERCHANT002", "MERCHANT003");
        return blacklistedMerchants.contains(merchantId);
    }
    
    private boolean isCrossBorderPattern(Wallet sourceWallet, Wallet targetWallet) {
        // Simplified check - in reality, this would use IP geolocation and other factors
        return sourceWallet.getUser().getCampusName() != null &&
               targetWallet.getUser().getCampusName() != null &&
               !sourceWallet.getUser().getCampusName().equals(targetWallet.getUser().getCampusName());
    }
}
