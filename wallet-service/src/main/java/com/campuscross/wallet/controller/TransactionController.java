package com.campuscross.wallet.controller;

import com.campuscross.wallet.entity.Transaction;
import com.campuscross.wallet.entity.User;
import com.campuscross.wallet.entity.Wallet;
import com.campuscross.wallet.service.TransactionService;
import com.campuscross.wallet.service.WalletService;
import com.campuscross.wallet.repository.UserRepository;
import com.campuscross.wallet.repository.WalletRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final WalletService walletService;

    // ---------------------------------------------------------
    // P2P TRANSFER
    // ---------------------------------------------------------
    @PostMapping("/p2p-transfer")
    public ResponseEntity<?> createP2PTransfer(
            @RequestBody P2PTransferRequest request,
            HttpServletRequest httpRequest) {
        try {
            String ipAddress = getClientIpAddress(httpRequest);

            // Extract and normalize identifier
            String identifier = request.recipientIdentifier();
            if (identifier != null)
                identifier = identifier.trim();

            log.info("P2P Transfer request for identifier: {}", identifier);

            if (identifier == null || identifier.isEmpty()) {
                throw new RuntimeException("Recipient identifier cannot be empty");
            }

                // Find recipient user (studentId -> phoneNumber fallback)
                User recipient = findRecipient(identifier);

                // Get source wallet to determine currency
                Wallet sourceWallet = walletRepository.findById(request.sourceWalletId())
                    .orElseThrow(() -> new RuntimeException("Source wallet not found"));
                String currency = sourceWallet.getCurrencyCode();

                // Find recipient wallet with same currency or create one
                Wallet targetWallet = walletRepository
                    .findByUserIdAndCurrencyCode(recipient.getId(), currency)
                    .orElseGet(() -> walletService.createWallet(recipient,
                        String.format("%s wallet", currency), Wallet.WalletType.PERSONAL, currency, false));

                Transaction transaction = transactionService.createP2PTransfer(
                    request.sourceWalletId(),
                    targetWallet.getWalletAddress(),
                    request.amount(),
                    request.description(),
                    ipAddress);

            return ResponseEntity.ok(new TransactionResponse(
                    transaction.getTransactionId(),
                    transaction.getType().toString(),
                    transaction.getStatus().toString(),
                    transaction.getAmount(),
                    transaction.getCurrencyCode(),
                    transaction.getDescription(),
                    transaction.getCreatedAt(),
                    transaction.getCompletedAt(),
                    "P2P transfer initiated successfully"));

        } catch (Exception e) {
            log.error("P2P transfer failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // Helper method for finding user by studentId OR phone
    private User findRecipient(String identifier) {
        Optional<User> byStudentId = userRepository.findByStudentId(identifier);
        if (byStudentId.isPresent())
            return byStudentId.get();

        Optional<User> byPhone = userRepository.findByPhoneNumber(identifier);
        if (byPhone.isPresent())
            return byPhone.get();

        throw new RuntimeException("User not found with this student ID or phone number");
    }

    // ---------------------------------------------------------
    // CAMPUS PAYMENT
    // ---------------------------------------------------------
    @PostMapping("/campus-payment")
    public ResponseEntity<?> createCampusPayment(
            @RequestBody CampusPaymentRequest request,
            HttpServletRequest httpRequest) {
        try {
            String ipAddress = getClientIpAddress(httpRequest);

            Transaction transaction = transactionService.createCampusPayment(
                    request.walletId(),
                    request.amount(),
                    request.merchantId(),
                    request.campusLocation(),
                    request.description(),
                    ipAddress);

            return ResponseEntity.ok(new TransactionResponse(
                    transaction.getTransactionId(),
                    transaction.getType().toString(),
                    transaction.getStatus().toString(),
                    transaction.getAmount(),
                    transaction.getCurrencyCode(),
                    transaction.getDescription(),
                    transaction.getCreatedAt(),
                    transaction.getCompletedAt(),
                    "Campus payment processed successfully"));

        } catch (Exception e) {
            log.error("Campus payment failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // ---------------------------------------------------------
    // REMITTANCE
    // ---------------------------------------------------------
    @PostMapping("/remittance")
    public ResponseEntity<?> createRemittance(
            @RequestBody RemittanceRequest request,
            HttpServletRequest httpRequest) {
        try {
            String ipAddress = getClientIpAddress(httpRequest);

            Transaction transaction = transactionService.createRemittance(
                    request.sourceWalletId(),
                    request.targetWalletAddress(),
                    request.amount(),
                    request.targetCurrency(),
                    request.exchangeRate(),
                    request.description(),
                    ipAddress);

            return ResponseEntity.ok(new TransactionResponse(
                    transaction.getTransactionId(),
                    transaction.getType().toString(),
                    transaction.getStatus().toString(),
                    transaction.getAmount(),
                    transaction.getCurrencyCode(),
                    transaction.getDescription(),
                    transaction.getCreatedAt(),
                    transaction.getCompletedAt(),
                    "Remittance processed successfully"));

        } catch (Exception e) {
            log.error("Remittance failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // ---------------------------------------------------------
    // REFUND
    // ---------------------------------------------------------
    @PostMapping("/{transactionId}/refund")
    public ResponseEntity<?> refundTransaction(
            @PathVariable String transactionId,
            @RequestBody RefundRequest request) {
        try {
            transactionService.refundTransaction(transactionId, request.reason());
            return ResponseEntity.ok(new SuccessResponse("Transaction refunded successfully"));
        } catch (Exception e) {
            log.error("Refund failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // ---------------------------------------------------------
    // CANCEL
    // ---------------------------------------------------------
    @PostMapping("/{transactionId}/cancel")
    public ResponseEntity<?> cancelTransaction(@PathVariable String transactionId) {
        try {
            transactionService.cancelTransaction(transactionId);
            return ResponseEntity.ok(new SuccessResponse("Transaction cancelled successfully"));
        } catch (Exception e) {
            log.error("Cancellation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // ---------------------------------------------------------
    // GET USER TRANSACTIONS
    // ---------------------------------------------------------
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserTransactions(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Transaction> transactions = transactionService.getUserTransactions(userId, pageable);

            List<TransactionResponse> responses = transactions.getContent()
                    .stream()
                    .map(this::mapToTransactionResponse)
                    .toList();

            return ResponseEntity.ok(new PagedTransactionResponse(
                    responses,
                    transactions.getNumber(),
                    transactions.getSize(),
                    transactions.getTotalElements(),
                    transactions.getTotalPages()));

        } catch (Exception e) {
            log.error("Failed to get user transactions: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // ---------------------------------------------------------
    // GET BY DATE RANGE
    // ---------------------------------------------------------
    @GetMapping("/user/{userId}/date-range")
    public ResponseEntity<?> getUserTransactionsByDateRange(
            @PathVariable Long userId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);

            List<TransactionResponse> responses = transactionService
                    .getUserTransactionsByDateRange(userId, start, end)
                    .stream()
                    .map(this::mapToTransactionResponse)
                    .toList();

            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            log.error("Failed to get transactions by date range: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // ---------------------------------------------------------
    // GET SINGLE TRANSACTION
    // ---------------------------------------------------------
    @GetMapping("/{transactionId}")
    public ResponseEntity<?> getTransaction(@PathVariable String transactionId) {
        try {
            Transaction transaction = transactionService.getTransaction(transactionId);
            return ResponseEntity.ok(mapToTransactionResponse(transaction));
        } catch (Exception e) {
            log.error("Failed to get transaction: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // ---------------------------------------------------------
    // FLAGGED / FAILED
    // ---------------------------------------------------------
    @GetMapping("/flagged")
    public ResponseEntity<?> getFlaggedTransactions() {
        try {
            List<TransactionResponse> responses = transactionService.getFlaggedTransactions()
                    .stream()
                    .map(this::mapToTransactionResponse)
                    .toList();

            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            log.error("Failed to get flagged transactions: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/failed")
    public ResponseEntity<?> getFailedTransactions() {
        try {
            List<TransactionResponse> responses = transactionService.getFailedTransactions()
                    .stream()
                    .map(this::mapToTransactionResponse)
                    .toList();

            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            log.error("Failed to get failed transactions: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // ---------------------------------------------------------
    // PRIVATE HELPERS
    // ---------------------------------------------------------
    private TransactionResponse mapToTransactionResponse(Transaction t) {
        return new TransactionResponse(
                t.getTransactionId(),
                t.getType().toString(),
                t.getStatus().toString(),
                t.getAmount(),
                t.getCurrencyCode(),
                t.getDescription(),
                t.getCreatedAt(),
                t.getCompletedAt(),
                null);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String header = request.getHeader("X-Forwarded-For");
        if (header != null && !header.isEmpty()) {
            return header.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    // ---------------------------------------------------------
    // DTOs
    // ---------------------------------------------------------
    public record P2PTransferRequest(
            Long sourceWalletId,
            String recipientIdentifier,
            BigDecimal amount,
            String description) {
    }

    public record CampusPaymentRequest(
            Long walletId,
            BigDecimal amount,
            String merchantId,
            String campusLocation,
            String description) {
    }

    public record RemittanceRequest(
            Long sourceWalletId,
            String targetWalletAddress,
            BigDecimal amount,
            String targetCurrency,
            BigDecimal exchangeRate,
            String description) {
    }

    public record RefundRequest(String reason) {
    }

    public record TransactionResponse(
            String transactionId,
            String type,
            String status,
            BigDecimal amount,
            String currencyCode,
            String description,
            LocalDateTime createdAt,
            LocalDateTime completedAt,
            String message) {
    }

    public record PagedTransactionResponse(
            List<TransactionResponse> transactions,
            int currentPage,
            int pageSize,
            long totalElements,
            int totalPages) {
    }

    public record SuccessResponse(String message) {
    }

    public record ErrorResponse(String error) {
    }
}
