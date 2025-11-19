package com.campuscross.fx_service.dto.remittance;

import com.campuscross.fx_service.model.Remittance;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response DTO for remittance operations
 */
public class RemittanceResponse {

    private boolean success;
    private String message;
    private String referenceNumber;
    private Remittance.TransferStatus status;
    private BigDecimal sourceAmount;
    private String sourceCurrency;
    private BigDecimal destinationAmount;
    private String destinationCurrency;
    private BigDecimal fxRate;
    private BigDecimal transferFee;
    private BigDecimal totalCost;
    private Instant estimatedCompletionTime;
    private Instant timestamp;

    public RemittanceResponse() {
        this.timestamp = Instant.now();
    }

    public RemittanceResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.timestamp = Instant.now();
    }

    // Static factory method
    public static RemittanceResponse fromEntity(Remittance remittance) {
        RemittanceResponse response = new RemittanceResponse();
        response.setSuccess(true);
        response.setMessage("Remittance created successfully");
        response.setReferenceNumber(remittance.getReferenceNumber());
        response.setStatus(remittance.getStatus());
        response.setSourceAmount(remittance.getSourceAmount());
        response.setSourceCurrency(remittance.getSourceCurrency());
        response.setDestinationAmount(remittance.getDestinationAmount());
        response.setDestinationCurrency(remittance.getDestinationCurrency());
        response.setFxRate(remittance.getFxRate());
        response.setTransferFee(remittance.getTransferFee());

        // Calculate total cost
        BigDecimal total = remittance.getSourceAmount();
        if (remittance.getTransferFee() != null) {
            total = total.add(remittance.getTransferFee());
        }
        response.setTotalCost(total);

        return response;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

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

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public Instant getEstimatedCompletionTime() {
        return estimatedCompletionTime;
    }

    public void setEstimatedCompletionTime(Instant estimatedCompletionTime) {
        this.estimatedCompletionTime = estimatedCompletionTime;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
