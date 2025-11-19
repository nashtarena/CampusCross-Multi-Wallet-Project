package com.campuscross.fx_service.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entity representing a bank remittance/transfer
 * Integrates with your existing FX and KYC systems
 */
@Entity
@Table(name = "remittances", indexes = {
        @Index(name = "idx_user_status", columnList = "userId,status"),
        @Index(name = "idx_reference", columnList = "referenceNumber")
})
public class Remittance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    // Transfer Details
    @Column(nullable = false, unique = true, length = 50)
    private String referenceNumber; // Internal reference (REM-20251117-123456)

    private String externalReferenceId; // Provider reference (Airwallex/SWIFT)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferType transferType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferStatus status = TransferStatus.PENDING;

    // Amount & Currency
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal sourceAmount;

    @Column(nullable = false, length = 3)
    private String sourceCurrency;

    @Column(precision = 19, scale = 4)
    private BigDecimal destinationAmount;

    @Column(length = 3)
    private String destinationCurrency;

    @Column(precision = 19, scale = 4)
    private BigDecimal fxRate; // Links to your FX service

    @Column(precision = 19, scale = 4)
    private BigDecimal transferFee;

    // Beneficiary Bank Details
    private String beneficiaryName;
    private String beneficiaryAccountNumber;
    private String beneficiaryBankName;
    private String beneficiaryBankCode; // SWIFT/IBAN/Routing Number
    private String beneficiaryAddress;

    @Column(length = 2)
    private String beneficiaryCountry; // ISO country code

    // Transfer Purpose & Notes
    private String transferPurpose; // e.g., "Family Support", "Education", "Business"

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Status Tracking
    private Instant initiatedAt;
    private Instant processingAt;
    private Instant completedAt;
    private Instant failedAt;

    @Column(columnDefinition = "TEXT")
    private String failureReason;

    @Column(columnDefinition = "TEXT")
    private String statusMessage;

    // Compliance - Links to your existing KYC system
    private Boolean kycVerified = false;
    private Boolean amlCleared = false;

    // Audit Trail
    @Column(updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    // Enums
    public enum TransferType {
        SWIFT, // International wire transfer
        SEPA, // European transfers
        FEDWIRE, // US domestic
        AIRWALLEX // Using Airwallex API (your existing integration)
    }

    public enum TransferStatus {
        PENDING, // Created, awaiting processing
        KYC_REQUIRED, // Needs KYC verification
        PROCESSING, // Being processed by provider
        COMPLETED, // Successfully transferred
        FAILED, // Transfer failed
        CANCELLED // User cancelled
    }

    // Lifecycle Callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        initiatedAt = Instant.now();
        if (referenceNumber == null) {
            referenceNumber = generateReferenceNumber();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    private String generateReferenceNumber() {
        return "REM-" + Instant.now().toEpochMilli();
    }

    // Constructors
    public Remittance() {
    }

    public Remittance(Long userId) {
        this.userId = userId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getExternalReferenceId() {
        return externalReferenceId;
    }

    public void setExternalReferenceId(String externalReferenceId) {
        this.externalReferenceId = externalReferenceId;
    }

    public TransferType getTransferType() {
        return transferType;
    }

    public void setTransferType(TransferType transferType) {
        this.transferType = transferType;
    }

    public TransferStatus getStatus() {
        return status;
    }

    public void setStatus(TransferStatus status) {
        this.status = status;
    }

    public BigDecimal getSourceAmount() {
        return sourceAmount;
    }

    public void setSourceAmount(BigDecimal sourceAmount) {
        this.sourceAmount = sourceAmount;
    }

    public String getSourceCurrency() {
        return sourceCurrency;
    }

    public void setSourceCurrency(String sourceCurrency) {
        this.sourceCurrency = sourceCurrency;
    }

    public BigDecimal getDestinationAmount() {
        return destinationAmount;
    }

    public void setDestinationAmount(BigDecimal destinationAmount) {
        this.destinationAmount = destinationAmount;
    }

    public String getDestinationCurrency() {
        return destinationCurrency;
    }

    public void setDestinationCurrency(String destinationCurrency) {
        this.destinationCurrency = destinationCurrency;
    }

    public BigDecimal getFxRate() {
        return fxRate;
    }

    public void setFxRate(BigDecimal fxRate) {
        this.fxRate = fxRate;
    }

    public BigDecimal getTransferFee() {
        return transferFee;
    }

    public void setTransferFee(BigDecimal transferFee) {
        this.transferFee = transferFee;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }

    public String getBeneficiaryAccountNumber() {
        return beneficiaryAccountNumber;
    }

    public void setBeneficiaryAccountNumber(String beneficiaryAccountNumber) {
        this.beneficiaryAccountNumber = beneficiaryAccountNumber;
    }

    public String getBeneficiaryBankName() {
        return beneficiaryBankName;
    }

    public void setBeneficiaryBankName(String beneficiaryBankName) {
        this.beneficiaryBankName = beneficiaryBankName;
    }

    public String getBeneficiaryBankCode() {
        return beneficiaryBankCode;
    }

    public void setBeneficiaryBankCode(String beneficiaryBankCode) {
        this.beneficiaryBankCode = beneficiaryBankCode;
    }

    public String getBeneficiaryAddress() {
        return beneficiaryAddress;
    }

    public void setBeneficiaryAddress(String beneficiaryAddress) {
        this.beneficiaryAddress = beneficiaryAddress;
    }

    public String getBeneficiaryCountry() {
        return beneficiaryCountry;
    }

    public void setBeneficiaryCountry(String beneficiaryCountry) {
        this.beneficiaryCountry = beneficiaryCountry;
    }

    public String getTransferPurpose() {
        return transferPurpose;
    }

    public void setTransferPurpose(String transferPurpose) {
        this.transferPurpose = transferPurpose;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getInitiatedAt() {
        return initiatedAt;
    }

    public void setInitiatedAt(Instant initiatedAt) {
        this.initiatedAt = initiatedAt;
    }

    public Instant getProcessingAt() {
        return processingAt;
    }

    public void setProcessingAt(Instant processingAt) {
        this.processingAt = processingAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Instant getFailedAt() {
        return failedAt;
    }

    public void setFailedAt(Instant failedAt) {
        this.failedAt = failedAt;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public Boolean getKycVerified() {
        return kycVerified;
    }

    public void setKycVerified(Boolean kycVerified) {
        this.kycVerified = kycVerified;
    }

    public Boolean getAmlCleared() {
        return amlCleared;
    }

    public void setAmlCleared(Boolean amlCleared) {
        this.amlCleared = amlCleared;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}