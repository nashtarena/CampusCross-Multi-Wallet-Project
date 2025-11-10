package com.campuscross.wallet.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campuscross.wallet.dto.ConversionRequest;
import com.campuscross.wallet.entity.CurrencyAccount;
import com.campuscross.wallet.entity.Transaction;
import com.campuscross.wallet.entity.Wallet;
import com.campuscross.wallet.repository.CurrencyAccountRepository;
import com.campuscross.wallet.repository.TransactionRepository;
import com.campuscross.wallet.repository.WalletRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyConversionService {
    
    private final WalletRepository walletRepository;
    private final CurrencyAccountRepository currencyAccountRepository;
    private final TransactionRepository transactionRepository;
    
    private static final List<String> SUPPORTED_CURRENCIES = Arrays.asList("USD", "EUR", "GBP", "JPY");
    private static final BigDecimal MIN_CONVERSION_AMOUNT = new BigDecimal("1.00");
    
    /**
     * Execute currency conversion within a user's wallet
     */
    @Transactional
    public Transaction executeConversion(ConversionRequest request) {
        log.info("Executing currency conversion: {} {} to {} for user {}",
                request.getAmount(), request.getFromCurrency(),
                request.getToCurrency(), request.getUserId());
        
        // 1. Validate request
        validateConversionRequest(request);
        
        // 2. Check for duplicate conversion (idempotency)
        if (request.getIdempotencyKey() != null) {
            Transaction existingTxn = transactionRepository
                    .findByReferenceId(request.getIdempotencyKey())
                    .orElse(null);
            if (existingTxn != null) {
                log.warn("Duplicate conversion detected with idempotency key: {}", request.getIdempotencyKey());
                return existingTxn;
            }
        }
        
        // 3. Find user's wallet
        Wallet wallet = walletRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Wallet not found for user ID: " + request.getUserId()));
        
        // 4. Get source currency account
        CurrencyAccount fromAccount = currencyAccountRepository
                .findByWalletIdAndCurrencyCode(wallet.getId(), request.getFromCurrency())
                .orElseThrow(() -> new RuntimeException("Source currency account not found: " + request.getFromCurrency()));
        
        // 5. Get target currency account
        CurrencyAccount toAccount = currencyAccountRepository
                .findByWalletIdAndCurrencyCode(wallet.getId(), request.getToCurrency())
                .orElseThrow(() -> new RuntimeException("Target currency account not found: " + request.getToCurrency()));
        
        // 6. Check sufficient balance in source currency
        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance in " + request.getFromCurrency() + 
                    ". Available: " + fromAccount.getBalance() + ", Required: " + request.getAmount());
        }
        
        // 7. Calculate converted amount using exchange rate
        BigDecimal convertedAmount = request.getAmount()
                .multiply(request.getExchangeRate())
                .setScale(4, RoundingMode.HALF_UP);
        
        log.info("Converting {} {} to {} {} at rate {}",
                request.getAmount(), request.getFromCurrency(),
                convertedAmount, request.getToCurrency(),
                request.getExchangeRate());
        
        // 8. Execute double-entry conversion
        // Debit from source currency
        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        currencyAccountRepository.save(fromAccount);
        
        // Credit to target currency
        toAccount.setBalance(toAccount.getBalance().add(convertedAmount));
        currencyAccountRepository.save(toAccount);
        
        // 9. Create conversion transaction record
        String description = String.format("Converted %s %s to %s %s at rate %s",
                request.getAmount(), request.getFromCurrency(),
                convertedAmount, request.getToCurrency(),
                request.getExchangeRate());
        
        Transaction transaction = Transaction.builder()
                .transactionType("CONVERSION")
                .fromAccountId(fromAccount.getId())
                .toAccountId(toAccount.getId())
                .amount(request.getAmount())
                .currencyCode(request.getFromCurrency())
                .status("completed")
                .referenceId(request.getIdempotencyKey() != null ? 
                        request.getIdempotencyKey() : UUID.randomUUID().toString())
                .description(description)
                .completedAt(LocalDateTime.now())
                .build();
        
        transaction = transactionRepository.save(transaction);
        
        log.info("Currency conversion completed successfully. Transaction ID: {}", transaction.getId());
        return transaction;
    }
    
    /**
     * Get conversion history for a user
     */
    @Transactional(readOnly = true)
    public List<Transaction> getConversionHistory(Long userId) {
        log.info("Fetching conversion history for user ID: {}", userId);
        
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for user ID: " + userId));
        
        // Get all currency account IDs for this wallet
        List<CurrencyAccount> accounts = currencyAccountRepository.findByWalletId(wallet.getId());
        
        // Get all conversion transactions
        return transactionRepository.findAll().stream()
                .filter(txn -> "CONVERSION".equals(txn.getTransactionType()))
                .filter(txn -> accounts.stream()
                        .anyMatch(acc -> acc.getId().equals(txn.getFromAccountId()) || 
                                        acc.getId().equals(txn.getToAccountId())))
                .toList();
    }
    
    /**
     * Calculate preview of conversion (without executing it)
     */
    public BigDecimal previewConversion(BigDecimal amount, BigDecimal exchangeRate) {
        if (amount == null || exchangeRate == null) {
            throw new RuntimeException("Amount and exchange rate are required");
        }
        
        return amount.multiply(exchangeRate).setScale(4, RoundingMode.HALF_UP);
    }
    
    /**
     * Validate conversion request
     */
    private void validateConversionRequest(ConversionRequest request) {
        if (request.getUserId() == null) {
            throw new RuntimeException("User ID is required");
        }
        
        if (request.getFromCurrency() == null || request.getFromCurrency().isEmpty()) {
            throw new RuntimeException("Source currency is required");
        }
        
        if (request.getToCurrency() == null || request.getToCurrency().isEmpty()) {
            throw new RuntimeException("Target currency is required");
        }
        
        if (request.getFromCurrency().equals(request.getToCurrency())) {
            throw new RuntimeException("Source and target currencies must be different");
        }
        
        if (!SUPPORTED_CURRENCIES.contains(request.getFromCurrency())) {
            throw new RuntimeException("Unsupported source currency: " + request.getFromCurrency());
        }
        
        if (!SUPPORTED_CURRENCIES.contains(request.getToCurrency())) {
            throw new RuntimeException("Unsupported target currency: " + request.getToCurrency());
        }
        
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Conversion amount must be greater than zero");
        }
        
        if (request.getAmount().compareTo(MIN_CONVERSION_AMOUNT) < 0) {
            throw new RuntimeException("Conversion amount must be at least " + MIN_CONVERSION_AMOUNT);
        }
        
        if (request.getExchangeRate() == null || request.getExchangeRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Valid exchange rate is required");
        }
    }
}