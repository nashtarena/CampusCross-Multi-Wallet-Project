package com.campuscross.wallet.controller;

import com.campuscross.wallet.entity.Transaction;
import com.campuscross.wallet.service.BankingService;
import com.campuscross.wallet.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Controller for simulated banking operations
 */
@RestController
@RequestMapping("/api/banking")
@RequiredArgsConstructor
@Slf4j
public class BankingController {

    private final BankingService bankingService;
    private final JwtUtil jwtUtil;

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestBody DepositRequest request, HttpServletRequest httpRequest) {
        try {
            // Extract userId from JWT (if present) and log both values for debugging
            String bearer = httpRequest.getHeader("Authorization");
            String tokenUserId = null;
            if (bearer != null && bearer.startsWith("Bearer ")) {
                String token = bearer.substring(7);
                if (jwtUtil.validateToken(token)) {
                    tokenUserId = jwtUtil.getUserIdFromToken(token);
                }
            }

            log.info("Deposit requested: body.userId={} token.userId={}", request.userId(), tokenUserId);

            // Prefer the userId from the validated JWT when present; fall back to
            // body.userId
            String effectiveUserId = tokenUserId != null ? tokenUserId : request.userId();
            log.info("Effective userId for deposit: {}", effectiveUserId);

            Transaction transaction = bankingService.depositFromBank(
                    effectiveUserId,
                    request.amount(),
                    request.currency());

            return ResponseEntity.ok(new DepositResponseWithUser(
                    true,
                    "Deposit successful",
                    transaction.getTransactionId(),
                    transaction.getAmount(),
                    transaction.getCurrencyCode(),
                    "COMPLETED",
                    request.userId()));
        } catch (Exception e) {
            log.error("Deposit failed", e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody WithdrawalRequest request) {
        try {
            Transaction transaction = bankingService.withdrawToBank(
                    request.userId(),
                    request.amount(),
                    request.currency(),
                    request.bankAccountNumber(),
                    request.bankName());

            return ResponseEntity.ok(new WithdrawalResponse(
                    true,
                    "Withdrawal successful",
                    transaction.getTransactionId(),
                    transaction.getAmount(),
                    transaction.getCurrencyCode(),
                    "COMPLETED",
                    request.bankAccountNumber()));
        } catch (Exception e) {
            log.error("Withdrawal failed", e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/deposit-instructions/{userId}")
    public ResponseEntity<?> getDepositInstructions(@PathVariable String userId) {
        try {
            Map<String, Object> instructions = bankingService.getDepositInstructions(userId);
            return ResponseEntity.ok(instructions);
        } catch (Exception e) {
            log.error("Failed to get deposit instructions", e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/withdrawal-instructions/{userId}")
    public ResponseEntity<?> getWithdrawalInstructions(@PathVariable String userId) {
        try {
            Map<String, Object> instructions = bankingService.getWithdrawalInstructions(userId);
            return ResponseEntity.ok(instructions);
        } catch (Exception e) {
            log.error("Failed to get withdrawal instructions", e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // DTOs
    public record DepositRequest(
            String userId,
            BigDecimal amount,
            String currency) {
    }

    public record WithdrawalRequest(
            String userId,
            BigDecimal amount,
            String currency,
            String bankAccountNumber,
            String bankName) {
    }

    public record DepositResponse(
            boolean success,
            String message,
            String transactionId,
            BigDecimal amount,
            String currency,
            String status) {
    }

    // Added userId to response for debugging/confirmation
    public record DepositResponseWithUser(
            boolean success,
            String message,
            String transactionId,
            BigDecimal amount,
            String currency,
            String status,
            String userId) {
    }

    public record WithdrawalResponse(
            boolean success,
            String message,
            String transactionId,
            BigDecimal amount,
            String currency,
            String status,
            String bankAccountNumber) {
    }

    public record ErrorResponse(
            String error) {
    }
}
