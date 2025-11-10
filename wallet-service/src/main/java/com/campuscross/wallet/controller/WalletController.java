package com.campuscross.wallet.controller;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.campuscross.wallet.entity.Wallet;
import com.campuscross.wallet.service.WalletService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
@Slf4j
public class WalletController {
    
    private final WalletService walletService;
    
    /**
     * Create a new wallet for a user
     * POST /api/v1/wallets
     */
    @PostMapping
    public ResponseEntity<Wallet> createWallet(@RequestParam Long userId) {
        log.info("Creating wallet for user ID: {}", userId);
        Wallet wallet = walletService.createWallet(userId);

        if (wallet.getUserId() != null) {
            return ResponseEntity.ok(wallet);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(wallet);
    }
    
    /**
     * Get wallet by user ID
     * GET /api/v1/wallets/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Wallet> getWalletByUserId(@PathVariable Long userId) {
        log.info("Fetching wallet for user ID: {}", userId);
        Wallet wallet = walletService.getWalletByUserId(userId);
        return ResponseEntity.ok(wallet);
    }
    
    /**
     * Get all balances for a wallet
     * GET /api/v1/wallets/{walletId}/balance
     */
    @GetMapping("/{walletId}/balance")
    public ResponseEntity<Map<String, BigDecimal>> getBalance(@PathVariable Long walletId) {
        log.info("Fetching balance for wallet ID: {}", walletId);
        Map<String, BigDecimal> balances = walletService.getBalance(walletId);
        return ResponseEntity.ok(balances);
    }
    
    /**
     * Get balance for specific currency
     * GET /api/v1/wallets/{walletId}/balance/{currency}
     */
    @GetMapping("/{walletId}/balance/{currency}")
    public ResponseEntity<BigDecimal> getBalanceByCurrency(
            @PathVariable Long walletId,
            @PathVariable String currency) {
        log.info("Fetching {} balance for wallet ID: {}", currency, walletId);
        BigDecimal balance = walletService.getBalanceByCurrency(walletId, currency);
        return ResponseEntity.ok(balance);
    }
}