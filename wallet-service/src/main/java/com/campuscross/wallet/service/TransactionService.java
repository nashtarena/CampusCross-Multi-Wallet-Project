package com.campuscross.wallet.service;

import com.campuscross.wallet.entity.Transaction;
import com.campuscross.wallet.entity.Wallet;
import com.campuscross.wallet.repository.TransactionRepository;
import com.campuscross.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {
    
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final WalletService walletService;
    private final FraudDetectionService fraudDetectionService;
    
    @Transactional
    public Transaction createP2PTransfer(Long sourceWalletId, String targetWalletAddress, 
                                       BigDecimal amount, String description, String ipAddress) {
        
        Wallet sourceWallet = walletRepository.findById(sourceWalletId)
                .orElseThrow(() -> new RuntimeException("Source wallet not found"));
        
        Wallet targetWallet = walletRepository.findByWalletAddress(targetWalletAddress)
                .orElseThrow(() -> new RuntimeException("Target wallet not found"));
        
        if (sourceWallet.getId().equals(targetWallet.getId())) {
            throw new RuntimeException("Cannot transfer to same wallet");
        }
        
        // Check for fraud
        if (fraudDetectionService.isSuspiciousTransaction(sourceWallet, targetWallet, amount, ipAddress)) {
            throw new RuntimeException("Transaction flagged as suspicious");
        }
        
        // Create transaction
        Transaction transaction = Transaction.builder()
                .transactionId(generateTransactionId())
                .sourceWallet(sourceWallet)
                .targetWallet(targetWallet)
                .amount(amount)
                .currencyCode(sourceWallet.getCurrencyCode())
                .type(Transaction.TransactionType.P2P_TRANSFER)
                .status(Transaction.TransactionStatus.PROCESSING)
                .description(description)
                .ipAddress(ipAddress)
                .build();
        
        transaction = transactionRepository.save(transaction);
        
        try {
            // Process the transfer
            walletService.deductFunds(sourceWalletId, amount);
            walletService.addFunds(targetWallet.getId(), amount);
            
            transaction.markCompleted();
            transactionRepository.save(transaction);
            
            log.info("P2P transfer completed: {} from {} to {}", 
                    amount, sourceWallet.getWalletAddress(), targetWallet.getWalletAddress());
            
        } catch (Exception e) {
            transaction.markFailed(e.getMessage());
            transactionRepository.save(transaction);
            throw e;
        }
        
        return transaction;
    }
    
    @Transactional
    public Transaction createCampusPayment(Long walletId, BigDecimal amount, String merchantId, 
                                         String campusLocation, String description, String ipAddress) {
        
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        
        // Check for fraud
        if (fraudDetectionService.isSuspiciousCampusPayment(wallet, amount, merchantId, ipAddress)) {
            throw new RuntimeException("Transaction flagged as suspicious");
        }
        
        // Create transaction
        Transaction transaction = Transaction.builder()
                .transactionId(generateTransactionId())
                .sourceWallet(wallet)
                .amount(amount)
                .currencyCode(wallet.getCurrencyCode())
                .type(Transaction.TransactionType.CAMPUS_PAYMENT)
                .status(Transaction.TransactionStatus.PROCESSING)
                .description(description)
                .merchantId(merchantId)
                .campusLocation(campusLocation)
                .ipAddress(ipAddress)
                .feeAmount(amount.multiply(new BigDecimal("0.02"))) // 2% fee
                .build();
        
        transaction = transactionRepository.save(transaction);
        
        try {
            walletService.deductFunds(walletId, amount);
            
            transaction.markCompleted();
            transactionRepository.save(transaction);
            
            log.info("Campus payment completed: {} at {} for merchant {}", 
                    amount, campusLocation, merchantId);
            
        } catch (Exception e) {
            transaction.markFailed(e.getMessage());
            transactionRepository.save(transaction);
            throw e;
        }
        
        return transaction;
    }
    
    @Transactional
    public Transaction createRemittance(Long sourceWalletId, String targetWalletAddress, 
                                      BigDecimal amount, String targetCurrency, BigDecimal exchangeRate,
                                      String description, String ipAddress) {
        
        Wallet sourceWallet = walletRepository.findById(sourceWalletId)
                .orElseThrow(() -> new RuntimeException("Source wallet not found"));
        
        Wallet targetWallet = walletRepository.findByWalletAddress(targetWalletAddress)
                .orElseThrow(() -> new RuntimeException("Target wallet not found"));
        
        // Calculate target amount
        BigDecimal targetAmount = amount.multiply(exchangeRate);
        
        // Check for fraud
        if (fraudDetectionService.isSuspiciousRemittance(sourceWallet, targetWallet, amount, ipAddress)) {
            throw new RuntimeException("Transaction flagged as suspicious");
        }
        
        // Create transaction
        Transaction transaction = Transaction.builder()
                .transactionId(generateTransactionId())
                .sourceWallet(sourceWallet)
                .targetWallet(targetWallet)
                .amount(targetAmount)
                .currencyCode(targetCurrency)
                .originalAmount(amount)
                .originalCurrency(sourceWallet.getCurrencyCode())
                .exchangeRate(exchangeRate)
                .type(Transaction.TransactionType.REMITTANCE_OUTBOUND)
                .status(Transaction.TransactionStatus.PROCESSING)
                .description(description)
                .ipAddress(ipAddress)
                .feeAmount(amount.multiply(new BigDecimal("0.015"))) // 1.5% fee
                .build();
        
        transaction = transactionRepository.save(transaction);
        
        try {
            walletService.deductFunds(sourceWalletId, amount);
            walletService.addFunds(targetWallet.getId(), targetAmount);
            
            transaction.markCompleted();
            transactionRepository.save(transaction);
            
            log.info("Remittance completed: {} {} -> {} {} (rate: {})", 
                    amount, sourceWallet.getCurrencyCode(), 
                    targetAmount, targetCurrency, exchangeRate);
            
        } catch (Exception e) {
            transaction.markFailed(e.getMessage());
            transactionRepository.save(transaction);
            throw e;
        }
        
        return transaction;
    }
    
    @Transactional
    public void refundTransaction(String transactionId, String reason) {
        Transaction originalTransaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        
        if (!originalTransaction.isCompleted()) {
            throw new RuntimeException("Cannot refund incomplete transaction");
        }
        
        if (originalTransaction.getSourceWallet() == null || originalTransaction.getTargetWallet() == null) {
            throw new RuntimeException("Cannot refund transaction without source and target wallets");
        }
        
        // Create refund transaction
        Transaction refundTransaction = Transaction.builder()
                .transactionId(generateTransactionId())
                .sourceWallet(originalTransaction.getTargetWallet())
                .targetWallet(originalTransaction.getSourceWallet())
                .amount(originalTransaction.getAmount())
                .currencyCode(originalTransaction.getCurrencyCode())
                .type(Transaction.TransactionType.REFUND)
                .status(Transaction.TransactionStatus.PROCESSING)
                .description("Refund for transaction: " + transactionId + ". Reason: " + reason)
                .referenceId(transactionId)
                .build();
        
        refundTransaction = transactionRepository.save(refundTransaction);
        
        try {
            walletService.deductFunds(originalTransaction.getTargetWallet().getId(), originalTransaction.getAmount());
            walletService.addFunds(originalTransaction.getSourceWallet().getId(), originalTransaction.getAmount());
            
            refundTransaction.markCompleted();
            transactionRepository.save(refundTransaction);
            
            log.info("Refund processed for transaction: {}", transactionId);
            
        } catch (Exception e) {
            refundTransaction.markFailed(e.getMessage());
            transactionRepository.save(refundTransaction);
            throw e;
        }
    }
    
    @Transactional
    public void cancelTransaction(String transactionId) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        
        if (!transaction.canBeCancelled()) {
            throw new RuntimeException("Transaction cannot be cancelled");
        }
        
        transaction.setStatus(Transaction.TransactionStatus.CANCELLED);
        transactionRepository.save(transaction);
        
        log.info("Transaction cancelled: {}", transactionId);
    }
    
    public Page<Transaction> getUserTransactions(Long userId, Pageable pageable) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    public List<Transaction> getUserTransactionsByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findByUserIdAndDateRange(userId, startDate, endDate);
    }
    
    public Transaction getTransaction(String transactionId) {
        return transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }
    
    public List<Transaction> getFlaggedTransactions() {
        return transactionRepository.findFlaggedTransactions();
    }
    
    public List<Transaction> getFailedTransactions() {
        return transactionRepository.findFailedTransactions();
    }
    
    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(0, 16);
    }
}
