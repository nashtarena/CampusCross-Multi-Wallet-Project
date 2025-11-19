package com.campuscross.fx_service.dto.kyc;

import com.campuscross.fx_service.model.UserKyc;
import java.time.Instant;

/**
 * Response DTO after KYC submission (any tier)
 */
public class KycSubmissionResponse {

    private boolean success;
    private String message;
    private Long userId;
    private UserKyc.KycTier tierCompleted;
    private UserKyc.KycStatus status;
    private Instant timestamp;
    private String sumsubAccessToken; // For launching SDK;

    // For Tier 2 - Sumsub reference IDs
    private String sumsubApplicantId;

    // For immediate actions
    private String nextStep; // e.g., "Upload documents", "Wait for verification"

    // Constructors
    public KycSubmissionResponse() {
        this.timestamp = Instant.now();
    }

    public KycSubmissionResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.timestamp = Instant.now();
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public UserKyc.KycTier getTierCompleted() {
        return tierCompleted;
    }

    public void setTierCompleted(UserKyc.KycTier tierCompleted) {
        this.tierCompleted = tierCompleted;
    }

    public UserKyc.KycStatus getStatus() {
        return status;
    }

    public void setStatus(UserKyc.KycStatus status) {
        this.status = status;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getSumsubApplicantId() {
        return sumsubApplicantId;
    }

    public void setSumsubApplicantId(String sumsubApplicantId) {
        this.sumsubApplicantId = sumsubApplicantId;
    }

    public String getNextStep() {
        return nextStep;
    }

    public void setNextStep(String nextStep) {
        this.nextStep = nextStep;
    }

    public String getSumsubAccessToken() {
        return sumsubAccessToken;
    }

    public void setSumsubAccessToken(String sumsubAccessToken) {
        this.sumsubAccessToken = sumsubAccessToken;
    }

}