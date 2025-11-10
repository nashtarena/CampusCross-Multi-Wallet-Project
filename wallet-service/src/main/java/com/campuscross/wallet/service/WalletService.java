package com.campuscross.wallet.service;

import com.campuscross.wallet.entity.CurrencyAccount;
import com.campuscross.wallet.entity.User;
import com.campuscross.wallet.entity.Wallet;
import com.campuscross.wallet.repository.CurrencyAccountRepository;
import com.campuscross.wallet.repository.UserRepository;
import com.campuscross.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {
    
    private final WalletRepository walletRepository;
    private final CurrencyAccountRepository currencyAccountRepository;
    private final UserRepository userRepository;
    
    private static final List<String> SUPPORTED_CURRENCIES = Arrays.asList("USD", "EUR", "GBP", "JPY");
    
    /**
     * Create a new wallet for a user with all supported currencies
     */
    @Transactional
    public Wallet createWallet(Long userId) {
        log.info("Creating wallet for user ID: {}", userId);
        
        // Check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        // Check if wallet already exists
        if (walletRepository.existsByUserId(userId)) {
            log.info("Wallet already exists for user ID: {}", userId);
            return walletRepository.findByUserId(userId).get();
        }
        
        // Create wallet
        Wallet wallet = Wallet.builder()
                .userId(userId)
                .status("active")
                .build();
        wallet = walletRepository.save(wallet);
        
        // Create currency accounts for all supported currencies
        for (String currency : SUPPORTED_CURRENCIES) {
            CurrencyAccount account = CurrencyAccount.builder()
                    .walletId(wallet.getId())
                    .currencyCode(currency)
                    .balance(BigDecimal.ZERO)
                    .build();
            currencyAccountRepository.save(account);
        }
        
        log.info("Wallet created successfully with ID: {} for user: {}", wallet.getId(), userId);
        return wallet;
    }
    
    /**
     * Get all balances for a wallet (multi-currency view)
     */
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getBalance(Long walletId) {
        log.info("Fetching balance for wallet ID: {}", walletId);
        
        // Verify wallet exists
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found with ID: " + walletId));
        
        // Get all currency accounts
        List<CurrencyAccount> accounts = currencyAccountRepository.findByWalletId(walletId);
        
        // Convert to map of currency -> balance
        Map<String, BigDecimal> balances = accounts.stream()
                .collect(Collectors.toMap(
                        CurrencyAccount::getCurrencyCode,
                        CurrencyAccount::getBalance
                ));
        
        log.info("Balance fetched for wallet {}: {}", walletId, balances);
        return balances;
    }
    
    /**
     * Get balance for a specific currency in a wallet
     */
    @Transactional(readOnly = true)
    public BigDecimal getBalanceByCurrency(Long walletId, String currencyCode) {
        log.info("Fetching {} balance for wallet ID: {}", currencyCode, walletId);
        
        // Verify wallet exists
        walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found with ID: " + walletId));
        
        // Validate currency code
        if (!SUPPORTED_CURRENCIES.contains(currencyCode)) {
            throw new RuntimeException("Unsupported currency: " + currencyCode);
        }
        
        // Get currency account
        CurrencyAccount account = currencyAccountRepository
                .findByWalletIdAndCurrencyCode(walletId, currencyCode)
                .orElseThrow(() -> new RuntimeException(
                        String.format("Currency account not found for wallet %d and currency %s", 
                                walletId, currencyCode)));
        
        log.info("Balance for wallet {} in {}: {}", walletId, currencyCode, account.getBalance());
        return account.getBalance();
    }
    
    /**
     * Get wallet by user ID
     */
    @Transactional(readOnly = true)
    public Wallet getWalletByUserId(Long userId) {
        log.info("Fetching wallet for user ID: {}", userId);
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for user ID: " + userId));
    }
}