package com.campuscross.wallet.service;

import com.campuscross.wallet.entity.Transaction;
import com.campuscross.wallet.entity.User;
import com.campuscross.wallet.entity.Wallet;
import com.campuscross.wallet.repository.TransactionRepository;
import com.campuscross.wallet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Simulated Banking Service
 * Handles deposits (bank → wallet) and withdrawals (wallet → bank)
 * WITHOUT actual Airwallex integration - just simulates money movement
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BankingService {
    
    private final WalletService walletService;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    
    // Simulated exchange rates
    private static final Map<String, BigDecimal> EXCHANGE_RATES = new HashMap<>() {{
        put("USD", new BigDecimal("1.00"));
        put("EUR", new BigDecimal("0.92"));
        put("GBP", new BigDecimal("0.79"));
        put("INR", new BigDecimal("83.12"));
    }};
    
    @Transactional
    public Transaction depositFromBank(String userId, BigDecimal amount, String currencyCode) {
        log.info("Processing simulated deposit: {} {} for user {}", amount, currencyCode, userId);
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Deposit amount must be positive");
        }
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Find a wallet for the requested currency for this user. If none exists, create one.
        Wallet wallet = null;
        try {
            wallet = walletService.getUserWallets(userId).stream()
                .filter(w -> w.getCurrencyCode() != null && w.getCurrencyCode().equalsIgnoreCase(currencyCode))
                .findFirst()
                .orElse(null);
        } catch (Exception ex) {
            log.warn("Failed to fetch user wallets for {}: {}", userId, ex.getMessage());
        }

        if (wallet == null) {
            // create a new wallet in requested currency (not default)
            wallet = walletService.createWallet(user, currencyCode + " Wallet", Wallet.WalletType.PERSONAL, currencyCode, false);
            log.info("Created new {} wallet {} for user {}", currencyCode, wallet.getWalletAddress(), userId);
        }

        walletService.addFunds(wallet.getId(), amount);
        
        Transaction transaction = Transaction.builder()
                .transactionId(generateTransactionId())
                .targetWallet(wallet)
                .amount(amount)
                .currencyCode(currencyCode)
                .type(Transaction.TransactionType.DEPOSIT)
                .status(Transaction.TransactionStatus.COMPLETED)
                .description("Simulated bank deposit")
                .completedAt(LocalDateTime.now())
                .build();
        
        transaction = transactionRepository.save(transaction);
        
        log.info("✅ Deposit successful: {} {} added to wallet {}", amount, currencyCode, wallet.getWalletAddress());
        return transaction;
    }
    
    @Transactional
    public Transaction withdrawToBank(String userId, BigDecimal amount, String currencyCode,
                                     String bankAccountNumber, String bankName) {
        log.info("Processing simulated withdrawal: {} {} for user {}", amount, currencyCode, userId);
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Withdrawal amount must be positive");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Wallet wallet = walletService.getDefaultWallet(userId);
        
        BigDecimal balanceInCurrency = walletService.getBalanceByCurrency(wallet.getId(), currencyCode);
        if (balanceInCurrency.compareTo(amount) >= 0) {
            deductDirectly(wallet.getId(), amount, currencyCode);
        } else {
            BigDecimal stillNeeded = amount.subtract(balanceInCurrency);
            convertAndDeduct(wallet, stillNeeded, currencyCode);
            if (balanceInCurrency.compareTo(BigDecimal.ZERO) > 0) {
                deductDirectly(wallet.getId(), balanceInCurrency, currencyCode);
            }
        }
        
        Transaction transaction = Transaction.builder()
                .transactionId(generateTransactionId())
                .sourceWallet(wallet)
                .amount(amount)
                .currencyCode(currencyCode)
                .type(Transaction.TransactionType.WITHDRAWAL)
                .status(Transaction.TransactionStatus.COMPLETED)
                .description("Simulated bank withdrawal to " + bankName + " ****" +
                             bankAccountNumber.substring(Math.max(0, bankAccountNumber.length() - 4)))
                .completedAt(LocalDateTime.now())
                .build();
        
        transaction = transactionRepository.save(transaction);
        
        log.info("✅ Withdrawal successful: {} {} deducted from wallet {}", amount, currencyCode, wallet.getWalletAddress());
        return transaction;
    }
    
    private void convertAndDeduct(Wallet wallet, BigDecimal amountNeeded, String targetCurrency) {
        log.info("Converting to {} to fulfill withdrawal of {}", targetCurrency, amountNeeded);
        
        Map<String, BigDecimal> availableBalances = new HashMap<>();
        addIfAvailable(availableBalances, wallet, "USD", targetCurrency);
        addIfAvailable(availableBalances, wallet, "EUR", targetCurrency);
        addIfAvailable(availableBalances, wallet, "GBP", targetCurrency);
        addIfAvailable(availableBalances, wallet, "INR", targetCurrency);
        
        if (availableBalances.isEmpty()) {
            throw new RuntimeException("Insufficient balance across all currencies");
        }
        
        BigDecimal stillNeeded = amountNeeded;
        for (Map.Entry<String, BigDecimal> entry : availableBalances.entrySet()) {
            if (stillNeeded.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            String fromCurrency = entry.getKey();
            BigDecimal availableAmount = entry.getValue();
            
            BigDecimal exchangeRate = getExchangeRate(fromCurrency, targetCurrency);
            BigDecimal amountToConvert = stillNeeded.divide(exchangeRate, 2, RoundingMode.HALF_UP);
            if (amountToConvert.compareTo(availableAmount) > 0) {
                amountToConvert = availableAmount;
            }
            
            deductDirectly(wallet.getId(), amountToConvert, fromCurrency);
            BigDecimal convertedAmount = amountToConvert.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
            stillNeeded = stillNeeded.subtract(convertedAmount);
            
            log.info("Converted {} {} to {} {} (rate: {})", amountToConvert, fromCurrency, convertedAmount, targetCurrency, exchangeRate);
        }
        
        if (stillNeeded.compareTo(new BigDecimal("0.01")) > 0) {
            throw new RuntimeException("Insufficient balance after conversion. Still need: " + stillNeeded + " " + targetCurrency);
        }
    }
    
    private void addIfAvailable(Map<String, BigDecimal> map, Wallet wallet, String currency, String excludeCurrency) {
        if (!currency.equals(excludeCurrency)) {
            BigDecimal balance = walletService.getBalanceByCurrency(wallet.getId(), currency);
            if (balance.compareTo(BigDecimal.ZERO) > 0) {
                map.put(currency, balance);
            }
        }
    }
    
    private void deductDirectly(Long walletId, BigDecimal amount, String currencyCode) {
        walletService.deductFunds(walletId, amount);
        log.info("Deducted {} {} from wallet", amount, currencyCode);
    }
    
    private BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        BigDecimal fromRate = EXCHANGE_RATES.getOrDefault(fromCurrency, BigDecimal.ONE);
        BigDecimal toRate = EXCHANGE_RATES.getOrDefault(toCurrency, BigDecimal.ONE);
        return toRate.divide(fromRate, 6, RoundingMode.HALF_UP);
    }
    
    public Map<String, Object> getDepositInstructions(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Wallet wallet = walletService.getDefaultWallet(userId);
        Map<String, Object> instructions = new HashMap<>();
        instructions.put("message", "This is a simulated banking system. Use /deposit to add funds.");
        instructions.put("walletAddress", wallet.getWalletAddress());
        instructions.put("supportedCurrencies", new String[]{"USD", "EUR", "GBP", "INR"});
        return instructions;
    }
    
    public Map<String, Object> getWithdrawalInstructions(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Wallet wallet = walletService.getDefaultWallet(userId);
        BigDecimal totalBalance = walletService.getTotalBalance(userId);
        Map<String, Object> instructions = new HashMap<>();
        instructions.put("message", "This is a simulated banking system. Use /withdraw to remove funds.");
        instructions.put("availableBalance", totalBalance);
        instructions.put("walletAddress", wallet.getWalletAddress());
        return instructions;
    }
    
    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(0, 16);
    }
}
