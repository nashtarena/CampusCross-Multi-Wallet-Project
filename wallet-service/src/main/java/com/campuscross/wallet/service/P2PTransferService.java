package com.campuscross.wallet.service;

import com.campuscross.wallet.dto.TransferRequest;
import com.campuscross.wallet.entity.CurrencyAccount;
import com.campuscross.wallet.entity.Transaction;
import com.campuscross.wallet.entity.User;
import com.campuscross.wallet.entity.Wallet;
import com.campuscross.wallet.repository.CurrencyAccountRepository;
import com.campuscross.wallet.repository.TransactionRepository;
import com.campuscross.wallet.repository.UserRepository;
import com.campuscross.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class P2PTransferService {
    
    private final WalletRepository walletRepository;
    private final CurrencyAccountRepository currencyAccountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    
    private static final BigDecimal MAX_TRANSFER_AMOUNT = new BigDecimal("10000.00");
    private static final BigDecimal MIN_TRANSFER_AMOUNT = new BigDecimal("1.00");
    
    /**
     * Execute P2P transfer between two users
     */
    @Transactional
    public Transaction executeTransfer(TransferRequest request) {
        log.info("Executing P2P transfer: {} {} from user {} to {}",
                request.getAmount(), request.getCurrencyCode(),
                request.getFromUserId(),
                request.getToStudentId() != null ? request.getToStudentId() : request.getToMobileNumber());
        
        // 1. Validate request
        validateTransferRequest(request);
        
        // 2. Check for duplicate transaction (idempotency)
        if (request.getIdempotencyKey() != null) {
            Transaction existingTxn = transactionRepository
                    .findByReferenceId(request.getIdempotencyKey())
                    .orElse(null);
            if (existingTxn != null) {
                log.warn("Duplicate transaction detected with idempotency key: {}", request.getIdempotencyKey());
                return existingTxn;
            }
        }
        
        // 3. Find sender's wallet
        Wallet senderWallet = walletRepository.findByUserId(request.getFromUserId())
                .orElseThrow(() -> new RuntimeException("Sender wallet not found"));
        
        // 4. Find recipient by student ID or mobile number
        User recipient = findRecipient(request);
        Wallet recipientWallet = walletRepository.findByUserId(recipient.getId())
                .orElseThrow(() -> new RuntimeException("Recipient wallet not found"));
        
        // 5. Check if sender and recipient are different
        if (senderWallet.getUserId().equals(recipientWallet.getUserId())) {
            throw new RuntimeException("Cannot transfer to yourself");
        }
        
        // 6. Get sender's currency account
        CurrencyAccount senderAccount = currencyAccountRepository
                .findByWalletIdAndCurrencyCode(senderWallet.getId(), request.getCurrencyCode())
                .orElseThrow(() -> new RuntimeException("Sender account not found for currency: " + request.getCurrencyCode()));
        
        // 7. Get recipient's currency account
        CurrencyAccount recipientAccount = currencyAccountRepository
                .findByWalletIdAndCurrencyCode(recipientWallet.getId(), request.getCurrencyCode())
                .orElseThrow(() -> new RuntimeException("Recipient account not found for currency: " + request.getCurrencyCode()));
        
        // 8. Check sufficient balance
        if (senderAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance. Available: " + senderAccount.getBalance() + 
                    ", Required: " + request.getAmount());
        }
        
        // 9. Execute double-entry transfer
        senderAccount.setBalance(senderAccount.getBalance().subtract(request.getAmount()));
        recipientAccount.setBalance(recipientAccount.getBalance().add(request.getAmount()));
        
        currencyAccountRepository.save(senderAccount);
        currencyAccountRepository.save(recipientAccount);
        
        // 10. Create transaction record
        Transaction transaction = Transaction.builder()
                .transactionType("P2P")
                .fromAccountId(senderAccount.getId())
                .toAccountId(recipientAccount.getId())
                .amount(request.getAmount())
                .currencyCode(request.getCurrencyCode())
                .status("completed")
                .referenceId(request.getIdempotencyKey() != null ? 
                        request.getIdempotencyKey() : UUID.randomUUID().toString())
                .description(request.getDescription())
                .completedAt(LocalDateTime.now())
                .build();
        
        transaction = transactionRepository.save(transaction);
        
        log.info("P2P transfer completed successfully. Transaction ID: {}", transaction.getId());
        return transaction;
    }
    
    /**
     * Find recipient by student ID or mobile number
     */
    private User findRecipient(TransferRequest request) {
        if (request.getToStudentId() != null && !request.getToStudentId().isEmpty()) {
            return userRepository.findByStudentId(request.getToStudentId())
                    .orElseThrow(() -> new RuntimeException("Recipient not found with student ID: " + request.getToStudentId()));
        } else if (request.getToMobileNumber() != null && !request.getToMobileNumber().isEmpty()) {
            return userRepository.findByMobileNumber(request.getToMobileNumber())
                    .orElseThrow(() -> new RuntimeException("Recipient not found with mobile number: " + request.getToMobileNumber()));
        } else {
            throw new RuntimeException("Recipient identifier (student ID or mobile number) is required");
        }
    }
    
    /**
     * Validate transfer request
     */
    private void validateTransferRequest(TransferRequest request) {
        if (request.getFromUserId() == null) {
            throw new RuntimeException("Sender user ID is required");
        }
        
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Transfer amount must be greater than zero");
        }
        
        if (request.getAmount().compareTo(MIN_TRANSFER_AMOUNT) < 0) {
            throw new RuntimeException("Transfer amount must be at least " + MIN_TRANSFER_AMOUNT);
        }
        
        if (request.getAmount().compareTo(MAX_TRANSFER_AMOUNT) > 0) {
            throw new RuntimeException("Transfer amount cannot exceed " + MAX_TRANSFER_AMOUNT);
        }
        
        if (request.getCurrencyCode() == null || request.getCurrencyCode().isEmpty()) {
            throw new RuntimeException("Currency code is required");
        }
        
        if (request.getToStudentId() == null && request.getToMobileNumber() == null) {
            throw new RuntimeException("Recipient identifier (student ID or mobile number) is required");
        }
    }
}