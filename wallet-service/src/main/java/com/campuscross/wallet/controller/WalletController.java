package com.campuscross.wallet.controller;

import com.campuscross.wallet.entity.Wallet;
import com.campuscross.wallet.entity.User;
import com.campuscross.wallet.service.WalletService;
import com.campuscross.wallet.repository.UserRepository;
import com.campuscross.wallet.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@Slf4j
public class WalletController {
    
    private final WalletService walletService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    
    @PostMapping("/create")
    public ResponseEntity<?> createWallet(@RequestBody CreateWalletRequest request, HttpServletRequest httpRequest) {
        try {
            // Extract user from JWT token
            String token = extractTokenFromRequest(httpRequest);
            if (token == null || !jwtUtil.validateToken(token)) {
                return ResponseEntity.status(401).body(new ErrorResponse("Unauthorized"));
            }
            
            String userId = jwtUtil.getUserIdFromToken(token);
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("User not found"));
            }
            
            Wallet wallet = walletService.createWallet(
                    user,
                    request.walletName(),
                    request.type(),
                    request.currencyCode(),
                    request.isDefault()
            );
            
            return ResponseEntity.ok(new WalletResponse(
                    wallet.getId(),
                    wallet.getWalletAddress(),
                    wallet.getWalletName(),
                    wallet.getType().toString(),
                    wallet.getStatus().toString(),
                    wallet.getBalance(),
                    wallet.getCurrencyCode(),
                    wallet.getIsDefault(),
                    wallet.getDailyLimit(),
                    wallet.getMonthlyLimit()
            ));
        } catch (Exception e) {
            log.error("Failed to create wallet", e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/{walletId}/add-funds")
    public ResponseEntity<?> addFunds(@PathVariable Long walletId, @RequestBody AddFundsRequest request) {
        try {
            Wallet wallet = walletService.addFunds(walletId, request.amount());
            
            return ResponseEntity.ok(new WalletBalanceResponse(
                    wallet.getBalance(),
                    "Funds added successfully"
            ));
        } catch (Exception e) {
            log.error("Failed to add funds", e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/{walletId}/deduct-funds")
    public ResponseEntity<?> deductFunds(@PathVariable Long walletId, @RequestBody DeductFundsRequest request) {
        try {
            Wallet wallet = walletService.deductFunds(walletId, request.amount());
            
            return ResponseEntity.ok(new WalletBalanceResponse(
                    wallet.getBalance(),
                    "Funds deducted successfully"
            ));
        } catch (Exception e) {
            log.error("Failed to deduct funds", e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/{walletId}/freeze")
    public ResponseEntity<?> freezeWallet(@PathVariable Long walletId, @RequestBody FreezeWalletRequest request) {
        try {
            walletService.freezeWallet(walletId, request.reason());
            return ResponseEntity.ok(new SuccessResponse("Wallet frozen successfully"));
        } catch (Exception e) {
            log.error("Failed to freeze wallet", e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/{walletId}/unfreeze")
    public ResponseEntity<?> unfreezeWallet(@PathVariable Long walletId) {
        try {
            walletService.unfreezeWallet(walletId);
            return ResponseEntity.ok(new SuccessResponse("Wallet unfrozen successfully"));
        } catch (Exception e) {
            log.error("Failed to unfreeze wallet", e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/{walletId}/close")
    public ResponseEntity<?> closeWallet(@PathVariable Long walletId) {
        try {
            walletService.closeWallet(walletId);
            return ResponseEntity.ok(new SuccessResponse("Wallet closed successfully"));
        } catch (Exception e) {
            log.error("Failed to close wallet", e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/{walletId}/daily-limit")
    public ResponseEntity<?> setDailyLimit(@PathVariable Long walletId, @RequestBody SetLimitRequest request) {
        try {
            Wallet wallet = walletService.setDailyLimit(walletId, request.limit());
            
            return ResponseEntity.ok(new LimitResponse(
                    wallet.getDailyLimit(),
                    "Daily limit set successfully"
            ));
        } catch (Exception e) {
            log.error("Failed to set daily limit", e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/{walletId}/monthly-limit")
    public ResponseEntity<?> setMonthlyLimit(@PathVariable Long walletId, @RequestBody SetLimitRequest request) {
        try {
            Wallet wallet = walletService.setMonthlyLimit(walletId, request.limit());
            
            return ResponseEntity.ok(new LimitResponse(
                    wallet.getMonthlyLimit(),
                    "Monthly limit set successfully"
            ));
        } catch (Exception e) {
            log.error("Failed to set monthly limit", e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserWallets(@PathVariable String userId) {
        try {
            List<Wallet> wallets = walletService.getUserWallets(userId);
            
            List<WalletResponse> walletResponses = wallets.stream()
                    .map(wallet -> new WalletResponse(
                            wallet.getId(),
                            wallet.getWalletAddress(),
                            wallet.getWalletName(),
                            wallet.getType().toString(),
                            wallet.getStatus().toString(),
                            wallet.getBalance(),
                            wallet.getCurrencyCode(),
                            wallet.getIsDefault(),
                            wallet.getDailyLimit(),
                            wallet.getMonthlyLimit()
                    ))
                    .toList();
            
            return ResponseEntity.ok(walletResponses);
        } catch (Exception e) {
            log.error("Failed to get user wallets", e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/user/{userId}/default")
    public ResponseEntity<?> getDefaultWallet(@PathVariable String userId) {
        try {
            Wallet wallet = walletService.getDefaultWallet(userId);
            
            return ResponseEntity.ok(new WalletResponse(
                    wallet.getId(),
                    wallet.getWalletAddress(),
                    wallet.getWalletName(),
                    wallet.getType().toString(),
                    wallet.getStatus().toString(),
                    wallet.getBalance(),
                    wallet.getCurrencyCode(),
                    wallet.getIsDefault(),
                    wallet.getDailyLimit(),
                    wallet.getMonthlyLimit()
            ));
        } catch (Exception e) {
            log.error("Failed to get default wallet", e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/address/{walletAddress}")
    public ResponseEntity<?> getWalletByAddress(@PathVariable String walletAddress) {
        try {
            Wallet wallet = walletService.getWalletByAddress(walletAddress);
            
            return ResponseEntity.ok(new WalletResponse(
                    wallet.getId(),
                    wallet.getWalletAddress(),
                    wallet.getWalletName(),
                    wallet.getType().toString(),
                    wallet.getStatus().toString(),
                    wallet.getBalance(),
                    wallet.getCurrencyCode(),
                    wallet.getIsDefault(),
                    wallet.getDailyLimit(),
                    wallet.getMonthlyLimit()
            ));
        } catch (Exception e) {
            log.error("Failed to get wallet by address", e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/user/{userId}/total-balance")
    public ResponseEntity<?> getTotalBalance(@PathVariable String userId) {
        try {
            BigDecimal totalBalance = walletService.getTotalBalance(userId);
            
            return ResponseEntity.ok(new TotalBalanceResponse(
                    totalBalance,
                    "USD" // Assuming USD for now
            ));
        } catch (Exception e) {
            log.error("Failed to get total balance", e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{walletId}")
    public ResponseEntity<?> deleteWallet(@PathVariable Long walletId, HttpServletRequest httpRequest) {
        try {
            // Extract user from JWT token
            String token = extractTokenFromRequest(httpRequest);
            if (token == null || !jwtUtil.validateToken(token)) {
                return ResponseEntity.status(401).body(new ErrorResponse("Unauthorized"));
            }
            
            String userId = jwtUtil.getUserIdFromToken(token);
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("User not found"));
            }
            
            // Get wallet to verify ownership
            Wallet wallet = walletService.getWalletById(walletId);
            if (!wallet.getUser().getId().equals(userId)) {
                return ResponseEntity.status(403).body(new ErrorResponse("Access denied"));
            }
            
            walletService.deleteWallet(walletId);
            return ResponseEntity.ok(new SuccessResponse("Wallet deleted successfully"));
        } catch (Exception e) {
            log.error("Failed to delete wallet: {}", walletId, e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    // Request/Response DTOs
    public record CreateWalletRequest(
            String walletName,
            Wallet.WalletType type,
            String currencyCode,
            Boolean isDefault
    ) {}
    
    public record AddFundsRequest(
            BigDecimal amount
    ) {}
    
    public record DeductFundsRequest(
            BigDecimal amount
    ) {}
    
    public record FreezeWalletRequest(
            String reason
    ) {}
    
    public record SetLimitRequest(
            BigDecimal limit
    ) {}
    
    public record WalletResponse(
            Long id,
            String walletAddress,
            String walletName,
            String type,
            String status,
            BigDecimal balance,
            String currencyCode,
            Boolean isDefault,
            BigDecimal dailyLimit,
            BigDecimal monthlyLimit
    ) {}
    
    public record WalletBalanceResponse(
            BigDecimal balance,
            String message
    ) {}
    
    public record LimitResponse(
            BigDecimal limit,
            String message
    ) {}
    
    public record TotalBalanceResponse(
            BigDecimal totalBalance,
            String currencyCode
    ) {}
    
    public record SuccessResponse(
            String message
    ) {}
    
    public record ErrorResponse(
            String error
    ) {}
    
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
