package com.campuscross.wallet.service;

import com.campuscross.wallet.entity.Wallet;
import com.campuscross.wallet.entity.User;
import com.campuscross.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {
    
    private final WalletRepository walletRepository;
    
    @Transactional
    public Wallet createDefaultWallet(User user) {
        return createWallet(user, "Default Wallet", Wallet.WalletType.PERSONAL, "USD", true);
    }
    
    @Transactional
    public Wallet createWallet(User user, String walletName, Wallet.WalletType type, 
                            String currencyCode, boolean isDefault) {
        
        // Check if user already has a default wallet
        if (isDefault) {
            walletRepository.findByUserIdAndIsDefaultTrue(user.getId())
                    .ifPresent(wallet -> {
                        wallet.setIsDefault(false);
                        walletRepository.save(wallet);
                    });
        }
        
        Wallet wallet = Wallet.builder()
                .user(user)
                .walletAddress(generateWalletAddress())
                .walletName(walletName)
                .type(type)
                .status(Wallet.WalletStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .currencyCode(currencyCode)
                .isDefault(isDefault)
                .dailyLimit(type == Wallet.WalletType.PERSONAL ? new BigDecimal("1000") : null)
                .monthlyLimit(type == Wallet.WalletType.PERSONAL ? new BigDecimal("10000") : null)
                .build();
        
        wallet = walletRepository.save(wallet);
        
        log.info("Created wallet {} for user: {}", wallet.getWalletAddress(), user.getEmail());
        return wallet;
    }
    
    @Transactional
    public Wallet addFunds(Long walletId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be positive");
        }
        
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        
        if (wallet.getStatus() != Wallet.WalletStatus.ACTIVE) {
            throw new RuntimeException("Wallet is not active");
        }
        
        wallet.addBalance(amount);
        wallet = walletRepository.save(wallet);
        
        log.info("Added {} to wallet {}", amount, wallet.getWalletAddress());
        return wallet;
    }
    
    @Transactional
    public Wallet deductFunds(Long walletId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be positive");
        }
        
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        
        if (!wallet.canSpend(amount)) {
            throw new RuntimeException("Insufficient balance or limit exceeded");
        }
        
        wallet.deductBalance(amount);
        wallet = walletRepository.save(wallet);
        
        log.info("Deducted {} from wallet {}", amount, wallet.getWalletAddress());
        return wallet;
    }
    
    @Transactional
    public void freezeWallet(Long walletId, String reason) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        
        wallet.setStatus(Wallet.WalletStatus.FROZEN);
        walletRepository.save(wallet);
        
        log.warn("Wallet {} frozen. Reason: {}", wallet.getWalletAddress(), reason);
    }
    
    @Transactional
    public void unfreezeWallet(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        
        wallet.setStatus(Wallet.WalletStatus.ACTIVE);
        walletRepository.save(wallet);
        
        log.info("Wallet {} unfrozen", wallet.getWalletAddress());
    }
    
    @Transactional
    public void closeWallet(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        
        if (wallet.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException("Cannot close wallet with non-zero balance");
        }
        
        wallet.setStatus(Wallet.WalletStatus.CLOSED);
        walletRepository.save(wallet);
        
        log.info("Wallet {} closed", wallet.getWalletAddress());
    }
    
    @Transactional
    public Wallet setDailyLimit(Long walletId, BigDecimal limit) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        
        wallet.setDailyLimit(limit);
        walletRepository.save(wallet);
        
        log.info("Daily limit set to {} for wallet {}", limit, wallet.getWalletAddress());
        return wallet;
    }
    
    @Transactional
    public Wallet setMonthlyLimit(Long walletId, BigDecimal limit) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        
        wallet.setMonthlyLimit(limit);
        walletRepository.save(wallet);
        
        log.info("Monthly limit set to {} for wallet {}", limit, wallet.getWalletAddress());
        return wallet;
    }
    
    public List<Wallet> getUserWallets(String userId) {
        return walletRepository.findByUserId(userId);
    }
    
    public Wallet getDefaultWallet(String userId) {
        return walletRepository.findByUserIdAndIsDefaultTrue(userId)
                .orElseThrow(() -> new RuntimeException("Default wallet not found"));
    }
    
    public Wallet getWalletByAddress(String walletAddress) {
        return walletRepository.findByWalletAddress(walletAddress)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
    }
    
<<<<<<< HEAD
    public Wallet getWalletById(Long walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
    }
    
=======
>>>>>>> 059d87042c298e8aa2a246bdee42e666e65fbcd7
    public BigDecimal getTotalBalance(String userId) {
        return walletRepository.getTotalBalanceByUserId(userId);
    }
    
<<<<<<< HEAD
    public void deleteWallet(Long walletId) {
        Wallet wallet = getWalletById(walletId);
        
        // Check if wallet has balance
        if (wallet.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException("Cannot delete wallet with non-zero balance");
        }
        
        // Check if wallet is the default wallet
        if (wallet.getIsDefault()) {
            throw new RuntimeException("Cannot delete default wallet");
        }
        
        walletRepository.delete(wallet);
    }
    
=======
>>>>>>> 059d87042c298e8aa2a246bdee42e666e65fbcd7
    private String generateWalletAddress() {
        return "WLT-" + UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(0, 16);
    }
}
