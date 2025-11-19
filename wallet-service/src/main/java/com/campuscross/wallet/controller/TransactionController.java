package com.campuscross.wallet.controller;

import com.campuscross.wallet.entity.Transaction;
import com.campuscross.wallet.service.TransactionService;
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
    
    @PostMapping("/p2p-transfer")
    public ResponseEntity<?> createP2PTransfer(@RequestBody P2PTransferRequest request, 
                                             HttpServletRequest httpRequest) {
        try {
            String ipAddress = getClientIpAddress(httpRequest);
            
            Transaction transaction = transactionService.createP2PTransfer(
                    request.sourceWalletId(),
                    request.targetWalletAddress(),
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
            String targetWalletAddress,
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
