package com.campuscross.fx_service.controller;

import com.campuscross.fx_service.dto.kyc.*;
import com.campuscross.fx_service.service.KycService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * REST Controller for KYC operations
 * Handles all three tiers of KYC verification
 */

@RestController
@RequestMapping("/api/v1/kyc")
public class KycController {

    private final KycService kycService;

    public KycController(KycService kycService) {
        this.kycService = kycService;
    }

    /**
     * Submit Tier 1 KYC (Basic Information)
     * This is INSTANTANEOUS - completes in < 1 second
     * Automatically triggers Tier 2 in background
     */
    @PostMapping("/tier1")
    public ResponseEntity<KycSubmissionResponse> submitTier1(
            @Valid @RequestBody Tier1KycRequest request) {

        KycSubmissionResponse response = kycService.processTier1(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get KYC status for a user
     * Shows progress across all tiers
     */
    @GetMapping("/status/{userId}")
    public ResponseEntity<KycStatusResponse> getKycStatus(@PathVariable Long userId) {
        KycStatusResponse response = kycService.getKycStatus(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Webhook endpoint for Sumsub callbacks (Tier 2)
     * Called by Sumsub when document verification completes
     */
    @PostMapping("/webhook/sumsub")
    public ResponseEntity<Void> handleSumsubWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Payload-Digest") String signature) {

        kycService.handleSumsubWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }

    /**
     * Manual retry endpoint (for testing or failed cases)
     */
    @PostMapping("/retry/{userId}")
    public ResponseEntity<KycSubmissionResponse> retryKycProcess(@PathVariable Long userId) {
        KycSubmissionResponse response = kycService.retryKycProcess(userId);
        return ResponseEntity.ok(response);
    }
}