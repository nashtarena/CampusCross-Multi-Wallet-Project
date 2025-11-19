package com.campuscross.wallet.controller;

import com.campuscross.wallet.entity.Transaction;
<<<<<<< HEAD
import com.campuscross.wallet.entity.User;
import com.campuscross.wallet.entity.Wallet;
import com.campuscross.wallet.service.TransactionService;
import com.campuscross.wallet.repository.UserRepository;
import com.campuscross.wallet.repository.WalletRepository;
import java.util.Optional;
=======
import com.campuscross.wallet.service.TransactionService;
>>>>>>> 059d87042c298e8aa2a246bdee42e666e65fbcd7
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {
    
    private final TransactionService transactionService;
<<<<<<< HEAD
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
=======
>>>>>>> 059d87042c298e8aa2a246bdee42e666e65fbcd7
    
    @PostMapping("/p2p-transfer")
    public ResponseEntity<?> createP2PTransfer(@RequestBody P2PTransferRequest request, 
                                             HttpServletRequest httpRequest) {
        try {
            String ipAddress = getClientIpAddress(httpRequest);
            
<<<<<<< HEAD
            // Find recipient by student ID or phone number
            User recipient = null;
            String identifier = request.recipientIdentifier();
            log.info("=== DEBUG INFO ===");
            log.info("Raw recipient identifier: '{}'", identifier);
            log.info("Identifier length: {}", identifier != null ? identifier.length() : "null");
            log.info("Identifier trimmed: '{}'", identifier != null ? identifier.trim() : "null");
            log.info("Identifier uppercase: '{}'", identifier != null ? identifier.trim().toUpperCase() : "null");
            log.info("Starts with STU: {}", identifier != null && identifier.trim().toUpperCase().startsWith("STU"));
            log.info("Is digits only: {}", identifier != null && identifier.trim().matches("\\d+"));
            
            // Trim whitespace and check format
            if (identifier != null) {
                identifier = identifier.trim();
            }
            
            // Try to find user by student ID first, then by phone number
            if (identifier != null) {
                // First try as student ID (in case user registered with numeric student ID)
                log.info("Trying to find user by student ID: {}", identifier);
                Optional<User> userByStudentId = userRepository.findByStudentId(identifier);
                if (userByStudentId.isPresent()) {
                    recipient = userByStudentId.get();
                    log.info("Found user by student ID: {}", recipient.getStudentId());
                } else {
                    // If not found by student ID, try phone number
                    log.info("User not found by student ID, trying phone number: {}", identifier);
                    Optional<User> userByPhone = userRepository.findByPhoneNumber(identifier);
                    if (userByPhone.isPresent()) {
                        recipient = userByPhone.get();
                        log.info("Found user by phone number: {}", recipient.getPhoneNumber());
                    } else {
                        log.error("User not found by student ID '{}' or phone number '{}'", identifier, identifier);
                        throw new RuntimeException("User not found with this student ID or phone number");
                    }
                }
            } else {
                log.error("Recipient identifier is null");
                throw new RuntimeException("Recipient identifier cannot be null");
            }
            
            // Get recipient's default wallet
            Wallet targetWallet = walletRepository.findByUserIdAndIsDefaultTrue(recipient.getId())
                    .orElseThrow(() -> new RuntimeException("Recipient has no default wallet"));
            
            Transaction transaction = transactionService.createP2PTransfer(
                    request.sourceWalletId(),
                    targetWallet.getWalletAddress(),
=======
            Transaction transaction = transactionService.createP2PTransfer(
                    request.sourceWalletId(),
                    request.targetWalletAddress(),
>>>>>>> 059d87042c298e8aa2a246bdee42e666e65fbcd7
                    request.amount(),
                    request.description(),
                    ipAddress
            );
            
            return ResponseEntity.ok(new TransactionResponse(
                    transaction.getTransactionId(),
                    transaction.getType().toString(),
                    transaction.getStatus().toString(),
                    transaction.getAmount(),
                    transaction.getCurrencyCode(),
                    transaction.getDescription(),
                    transaction.getCreatedAt(),
                    transaction.getCompletedAt(),
                    "P2P transfer initiated successfully"
            ));
        } catch (Exception e) {
            log.error("P2P transfer failed", e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/campus-payment")
    public ResponseEntity<?> createCampusPayment(@RequestBody CampusPaymentRequest request,
                                               HttpServletRequest httpRequest) {
        try {
            String ipAddress = getClientIpAddress(httpRequest);
            
            Transaction transaction = transactionService.createCampusPayment(
                    request.walletId(),
                    request.amount(),
                    request.merchantId(),
                    request.campusLocation(),
                    request.description(),
                    ipAddress
            );
            
            return ResponseEntity.ok(new TransactionResponse(
                    transaction.getTransactionId(),
                    transaction.getType().toString(),
                    transaction.getStatus().toString(),
                    transaction.getAmount(),
                    transaction.getCurrencyCode(),
                    transaction.getDescription(),
                    transaction.getCreatedAt(),
                    transaction.getCompletedAt(),
                    "Campus payment processed successfully"
            ));
        } catch (Exception e) {
            log.error("Campus payment failed", e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/remittance")
    public ResponseEntity<?> createRemittance(@RequestBody RemittanceRequest request,
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
                    ipAddress
            );
            
            return ResponseEntity.ok(new TransactionResponse(
                    transaction.getTransactionId(),
                    transaction.getType().toString(),
                    transaction.getStatus().toString(),
                    transaction.getAmount(),
                    transaction.getCurrencyCode(),
                    transaction.getDescription(),
                    transaction.getCreatedAt(),
                    transaction.getCompletedAt(),
                    "Remittance processed successfully"
            ));
        } catch (Exception e) {
            log.error("Remittance failed", e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/{transactionId}/refund")
    public ResponseEntity<?> refundTransaction(@PathVariable String transactionId,
                                              @RequestBody RefundRequest request) {
        try {
            transactionService.refundTransaction(transactionId, request.reason());
            
            return ResponseEntity.ok(new SuccessResponse("Transaction refunded successfully"));
        } catch (Exception e) {
            log.error("Refund failed", e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/{transactionId}/cancel")
    public ResponseEntity<?> cancelTransaction(@PathVariable String transactionId) {
        try {
            transactionService.cancelTransaction(transactionId);
            
            return ResponseEntity.ok(new SuccessResponse("Transaction cancelled successfully"));
        } catch (Exception e) {
            log.error("Cancellation failed", e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserTransactions(@PathVariable Long userId,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Transaction> transactions = transactionService.getUserTransactions(userId, pageable);
            
            List<TransactionResponse> transactionResponses = transactions.getContent()
                    .stream()
                    .map(this::mapToTransactionResponse)
                    .toList();
            
            return ResponseEntity.ok(new PagedTransactionResponse(
                    transactionResponses,
                    transactions.getNumber(),
                    transactions.getSize(),
                    transactions.getTotalElements(),
                    transactions.getTotalPages()
            ));
        } catch (Exception e) {
            log.error("Failed to get user transactions", e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/user/{userId}/date-range")
    public ResponseEntity<?> getUserTransactionsByDateRange(
            @PathVariable Long userId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            
            List<Transaction> transactions = transactionService.getUserTransactionsByDateRange(userId, start, end);
            
            List<TransactionResponse> transactionResponses = transactions.stream()
                    .map(this::mapToTransactionResponse)
                    .toList();
            
            return ResponseEntity.ok(transactionResponses);
        } catch (Exception e) {
            log.error("Failed to get transactions by date range", e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/{transactionId}")
    public ResponseEntity<?> getTransaction(@PathVariable String transactionId) {
        try {
            Transaction transaction = transactionService.getTransaction(transactionId);
            
            return ResponseEntity.ok(mapToTransactionResponse(transaction));
        } catch (Exception e) {
            log.error("Failed to get transaction", e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/flagged")
    public ResponseEntity<?> getFlaggedTransactions() {
        try {
            List<Transaction> transactions = transactionService.getFlaggedTransactions();
            
            List<TransactionResponse> transactionResponses = transactions.stream()
                    .map(this::mapToTransactionResponse)
                    .toList();
            
            return ResponseEntity.ok(transactionResponses);
        } catch (Exception e) {
            log.error("Failed to get flagged transactions", e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/failed")
    public ResponseEntity<?> getFailedTransactions() {
        try {
            List<Transaction> transactions = transactionService.getFailedTransactions();
            
            List<TransactionResponse> transactionResponses = transactions.stream()
                    .map(this::mapToTransactionResponse)
                    .toList();
            
            return ResponseEntity.ok(transactionResponses);
        } catch (Exception e) {
            log.error("Failed to get failed transactions", e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getTransactionId(),
                transaction.getType().toString(),
                transaction.getStatus().toString(),
                transaction.getAmount(),
                transaction.getCurrencyCode(),
                transaction.getDescription(),
                transaction.getCreatedAt(),
                transaction.getCompletedAt(),
                null
        );
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
    
    // Request/Response DTOs
    public record P2PTransferRequest(
            Long sourceWalletId,
<<<<<<< HEAD
            String recipientIdentifier, // Can be student ID or phone number
=======
            String targetWalletAddress,
>>>>>>> 059d87042c298e8aa2a246bdee42e666e65fbcd7
            BigDecimal amount,
            String description
    ) {}
    
    public record CampusPaymentRequest(
            Long walletId,
            BigDecimal amount,
            String merchantId,
            String campusLocation,
            String description
    ) {}
    
    public record RemittanceRequest(
            Long sourceWalletId,
            String targetWalletAddress,
            BigDecimal amount,
            String targetCurrency,
            BigDecimal exchangeRate,
            String description
    ) {}
    
    public record RefundRequest(
            String reason
    ) {}
    
    public record TransactionResponse(
            String transactionId,
            String type,
            String status,
            BigDecimal amount,
            String currencyCode,
            String description,
            LocalDateTime createdAt,
            LocalDateTime completedAt,
            String message
    ) {}
    
    public record PagedTransactionResponse(
            List<TransactionResponse> transactions,
            int currentPage,
            int pageSize,
            long totalElements,
            int totalPages
    ) {}
    
    public record SuccessResponse(
            String message
    ) {}
    
    public record ErrorResponse(
            String error
    ) {}
}
