package com.campuscross.fx_service.dto.kyc;

import com.campuscross.fx_service.model.UserKyc;
import java.time.Instant;

/**
 * Response DTO for KYC status queries
 */
public class KycStatusResponse {

    private Long userId;
    private UserKyc.KycTier currentTier;
    private UserKyc.KycStatus status;
    private String message;

    // Tier completion flags
    private boolean tier1Complete;
    private boolean tier2Complete;
    private boolean tier3Complete;

    // Risk indicators
    private Boolean sanctionsMatch;
    private Boolean pepMatch;
    private Integer riskScore;

    // Timestamps
    private Instant tier1CompletedAt;
    private Instant tier2CompletedAt;
    private Instant tier3CompletedAt;
    private Instant lastUpdated;

    // Constructors
    public KycStatusResponse() {
    }

    // Static factory method to create from UserKyc entity
    public static KycStatusResponse fromEntity(UserKyc userKyc) {
        KycStatusResponse response = new KycStatusResponse();
        response.setUserId(userKyc.getUserId());
        response.setCurrentTier(userKyc.getKycTier());
        response.setStatus(userKyc.getKycStatus());
        response.setMessage(userKyc.getVerificationMessage());

        response.setTier1Complete(userKyc.getTier1CompletedAt() != null);
        response.setTier2Complete(userKyc.getTier2CompletedAt() != null);
        response.setTier3Complete(userKyc.getTier3CompletedAt() != null);

        response.setSanctionsMatch(userKyc.getSanctionsMatch());
        response.setPepMatch(userKyc.getPepMatch());
        response.setRiskScore(userKyc.getRiskScore());

        response.setTier1CompletedAt(userKyc.getTier1CompletedAt());
        response.setTier2CompletedAt(userKyc.getTier2CompletedAt());
        response.setTier3CompletedAt(userKyc.getTier3CompletedAt());
        response.setLastUpdated(userKyc.getUpdatedAt());

        return response;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public UserKyc.KycTier getCurrentTier() {
        return currentTier;
    }

    public void setCurrentTier(UserKyc.KycTier currentTier) {
        this.currentTier = currentTier;
    }

    public UserKyc.KycStatus getStatus() {
        return status;
    }

    public void setStatus(UserKyc.KycStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isTier1Complete() {
        return tier1Complete;
    }

    public void setTier1Complete(boolean tier1Complete) {
        this.tier1Complete = tier1Complete;
    }

    public boolean isTier2Complete() {
        return tier2Complete;
    }

    public void setTier2Complete(boolean tier2Complete) {
        this.tier2Complete = tier2Complete;
    }

    public boolean isTier3Complete() {
        return tier3Complete;
    }

    public void setTier3Complete(boolean tier3Complete) {
        this.tier3Complete = tier3Complete;
    }

    public Boolean getSanctionsMatch() {
        return sanctionsMatch;
    }

    public void setSanctionsMatch(Boolean sanctionsMatch) {
        this.sanctionsMatch = sanctionsMatch;
    }

    public Boolean getPepMatch() {
        return pepMatch;
    }

    public void setPepMatch(Boolean pepMatch) {
        this.pepMatch = pepMatch;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public Instant getTier1CompletedAt() {
        return tier1CompletedAt;
    }

    public void setTier1CompletedAt(Instant tier1CompletedAt) {
        this.tier1CompletedAt = tier1CompletedAt;
    }

    public Instant getTier2CompletedAt() {
        return tier2CompletedAt;
    }

    public void setTier2CompletedAt(Instant tier2CompletedAt) {
        this.tier2CompletedAt = tier2CompletedAt;
    }

    public Instant getTier3CompletedAt() {
        return tier3CompletedAt;
    }

    public void setTier3CompletedAt(Instant tier3CompletedAt) {
        this.tier3CompletedAt = tier3CompletedAt;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
