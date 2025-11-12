package com.campuscross.fx_service.dto.kyc;

import jakarta.validation.constraints.*;

/**
 * Request DTO for Tier 2 KYC submission (Document Verification)
 */
public class Tier2KycRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Document type is required")
    @Pattern(regexp = "PASSPORT|ID_CARD|DRIVERS_LICENSE", message = "Invalid document type")
    private String documentType;

    @NotBlank(message = "Document number is required")
    private String documentNumber;

    @NotBlank(message = "Document issuing country is required")
    @Size(min = 2, max = 2, message = "Country must be 2-letter ISO code")
    private String documentCountry;

    // Note: Actual file upload will be handled separately via multipart/form-data
    // This DTO is for the metadata

    // Constructors
    public Tier2KycRequest() {
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getDocumentCountry() {
        return documentCountry;
    }

    public void setDocumentCountry(String documentCountry) {
        this.documentCountry = documentCountry;
    }
}