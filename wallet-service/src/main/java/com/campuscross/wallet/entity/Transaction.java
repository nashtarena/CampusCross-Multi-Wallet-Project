package com.campuscross.wallet.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_wallet_id")
    private Wallet sourceWallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_wallet_id")
    private Wallet targetWallet;

    @Column(precision = 19, scale = 8, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency_code", nullable = false)
    private String currencyCode;

    @Column(name = "exchange_rate", precision = 19, scale = 8)
    private BigDecimal exchangeRate;

    @Column(name = "original_amount", precision = 19, scale = 8)
    private BigDecimal originalAmount;

    @Column(name = "original_currency")
    private String originalCurrency;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(nullable = false)
    private String description;

    @Column(name = "reference_id")
    private String referenceId;

    @Column(name = "merchant_id")
    private String merchantId;

    @Column(name = "campus_location")
    private String campusLocation;

    @Column(name = "external_transaction_id")
    private String externalTransactionId;

    @Column(name = "fee_amount", precision = 19, scale = 8)
    private BigDecimal feeAmount = BigDecimal.ZERO;

    @Column(name = "fee_currency")
    private String feeCurrency = "USD";

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "device_fingerprint")
    private String deviceFingerprint;

    @Column(name = "is_flagged")
    private Boolean flagged = false;

    @Column(name = "flag_reason")
    private String flagReason;

    @Column(name = "sender_student_id")
    private String senderStudentId;

    @Column(name = "recipient_student_id")
    private String recipientStudentId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public enum TransactionType {
        P2P_TRANSFER, CAMPUS_PAYMENT, REMITTANCE_OUTBOUND, REMITTANCE_INBOUND,
        CURRENCY_EXCHANGE, FEE_CHARGE, REFUND, DEPOSIT, WITHDRAWAL
    }

    public enum TransactionStatus {
        PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED, FLAGGED
    }

    public boolean isCompleted() {
        return status == TransactionStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == TransactionStatus.FAILED || status == TransactionStatus.CANCELLED;
    }

    public boolean canBeCancelled() {
        return status == TransactionStatus.PENDING || status == TransactionStatus.PROCESSING;
    }

    public void markCompleted() {
        this.status = TransactionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void markFailed(String reason) {
        this.status = TransactionStatus.FAILED;
        this.failureReason = reason;
        this.completedAt = LocalDateTime.now();
    }

    public void markFlagged(String reason) {
        this.status = TransactionStatus.FLAGGED;
        this.flagged = true;
        this.flagReason = reason;
    }
}
