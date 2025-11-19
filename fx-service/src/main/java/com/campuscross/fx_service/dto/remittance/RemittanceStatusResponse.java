package com.campuscross.fx_service.dto.remittance;

import com.campuscross.fx_service.model.Remittance;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response DTO for remittance status inquiries
 */
public class RemittanceStatusResponse {

    private String referenceNumber;
    private Remittance.TransferStatus status;
    private String statusMessage;
    private BigDecimal sourceAmount;
    private String sourceCurrency;
    private BigDecimal destinationAmount;
    private String destinationCurrency;
    private String beneficiaryName;
    private Instant initiatedAt;
    private Instant completedAt;
    private String externalReferenceId;

    public static RemittanceStatusResponse fromEntity(Remittance remittance) {
        RemittanceStatusResponse response = new RemittanceStatusResponse();
        response.setReferenceNumber(remittance.getReferenceNumber());
        response.setStatus(remittance.getStatus());
        response.setStatusMessage(remittance.getStatusMessage());
        response.setSourceAmount(remittance.getSourceAmount());
        response.setSourceCurrency(remittance.getSourceCurrency());
        response.setDestinationAmount(remittance.getDestinationAmount());
        response.setDestinationCurrency(remittance.getDestinationCurrency());
        response.setBeneficiaryName(remittance.getBeneficiaryName());
        response.setInitiatedAt(remittance.getInitiatedAt());
        response.setCompletedAt(remittance.getCompletedAt());
        response.setExternalReferenceId(remittance.getExternalReferenceId());
        return response;
    }

    // Getters and Setters
    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public Remittance.TransferStatus getStatus() {
        return status;
    }

    public void setStatus(Remittance.TransferStatus status) {
        this.status = status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
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

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }

    public Instant getInitiatedAt() {
        return initiatedAt;
    }

    public void setInitiatedAt(Instant initiatedAt) {
        this.initiatedAt = initiatedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public String getExternalReferenceId() {
        return externalReferenceId;
    }

    public void setExternalReferenceId(String externalReferenceId) {
        this.externalReferenceId = externalReferenceId;
    }
}