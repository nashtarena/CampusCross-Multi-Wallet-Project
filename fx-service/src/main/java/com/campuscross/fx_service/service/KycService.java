package com.campuscross.fx_service.service;

import com.campuscross.fx_service.client.SumsubClient;
import com.campuscross.fx_service.dto.kyc.*;
import com.campuscross.fx_service.dto.sumsub.SumsubApplicantResponse;
import com.campuscross.fx_service.model.UserKyc;
import com.campuscross.fx_service.repository.UserKycRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * KYC Service - Orchestrates the three-tier KYC process
 * 
 * Flow:
 * 1. Tier 1: Instant validation + sanctions check (< 1 second)
 * 2. Tier 2: Background Sumsub document verification (30s - 5min)
 * 3. Tier 3: Automatic AML/PEP screening after Tier 2 completes
 */
@Service
public class KycService {

    private static final Logger log = LoggerFactory.getLogger(KycService.class);

    private final UserKycRepository kycRepository;
    private final SumsubClient sumsubClient;
    private final OpenSanctionsService sanctionsService;
    private final KycAsyncProcessor asyncProcessor;
    private final SumsubWebhookHandler webhookHandler;

    public KycService(
            UserKycRepository kycRepository,
            SumsubClient sumsubClient,
            OpenSanctionsService sanctionsService,
            KycAsyncProcessor asyncProcessor,
            SumsubWebhookHandler webhookHandler) {
        this.kycRepository = kycRepository;
        this.sumsubClient = sumsubClient;
        this.sanctionsService = sanctionsService;
        this.asyncProcessor = asyncProcessor;
        this.webhookHandler = webhookHandler;
    }

    /**
     * Process Tier 1 KYC - INSTANTANEOUS
     * 
     * Steps:
     * 1. Save basic user information
     * 2. Run instant sanctions check (country-level)
     * 3. Mark Tier 1 as complete
     * 4. Trigger Tier 2 in background (non-blocking)
     * 
     * Returns immediately with success response
     */
    @Transactional
    public KycSubmissionResponse processTier1(Tier1KycRequest request) {
        log.info("Processing Tier 1 KYC for userId: {}", request.getUserId());

        try {
            // Check if user already has KYC record
            UserKyc userKyc = kycRepository.findByUserId(request.getUserId())
                    .orElse(new UserKyc(request.getUserId()));

            // Step 1: Save basic information
            userKyc.setFirstName(request.getFirstName());
            userKyc.setLastName(request.getLastName());
            userKyc.setDateOfBirth(request.getDateOfBirth());
            userKyc.setPhoneNumber(request.getPhoneNumber());
            userKyc.setEmail(request.getEmail());
            userKyc.setCountryOfResidence(request.getCountryOfResidence());
            userKyc.setAddressLine1(request.getAddressLine1());
            userKyc.setAddressLine2(request.getAddressLine2());
            userKyc.setCity(request.getCity());
            userKyc.setStateProvince(request.getStateProvince());
            userKyc.setPostalCode(request.getPostalCode());

            // Step 2: Instant country-level sanctions check
            boolean isCountrySanctioned = sanctionsService.isCountrySanctioned(
                    request.getCountryOfResidence());

            if (isCountrySanctioned) {
                userKyc.setKycStatus(UserKyc.KycStatus.REJECTED);
                userKyc.setRejectionReason("Country is under sanctions");
                userKyc.setSanctionsMatch(true);
                kycRepository.save(userKyc);

                return createResponse(false, "KYC rejected: Sanctioned country",
                        request.getUserId(), null, UserKyc.KycStatus.REJECTED);
            }

            // Step 3: Mark Tier 1 as complete
            userKyc.setKycTier(UserKyc.KycTier.TIER_1);
            userKyc.setKycStatus(UserKyc.KycStatus.PENDING);
            userKyc.setTier1CompletedAt(Instant.now());
            userKyc.setSubmittedAt(Instant.now());
            userKyc.setVerificationMessage("Tier 1 complete. Document verification in progress.");

            userKyc = kycRepository.save(userKyc);

            // Step 4: Trigger Tier 2 in background (non-blocking)
            asyncProcessor.processTier2Async(userKyc.getId());

            log.info("Tier 1 completed instantly for userId: {}", request.getUserId());

            // Return success immediately
            KycSubmissionResponse response = createResponse(
                    true,
                    "Tier 1 KYC completed successfully. Document verification initiated.",
                    request.getUserId(),
                    UserKyc.KycTier.TIER_1,
                    UserKyc.KycStatus.PENDING);

            response.setNextStep("Document verification in progress. You'll be notified when complete.");
            return response;

        } catch (Exception e) {
            log.error("Error processing Tier 1 KYC: {}", e.getMessage(), e);
            return createResponse(false, "KYC submission failed: " + e.getMessage(),
                    request.getUserId(), null, null);
        }
    }

    /**
     * Get KYC status for a user
     */
    public KycStatusResponse getKycStatus(Long userId) {
        UserKyc userKyc = kycRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("KYC record not found for user: " + userId));

        return KycStatusResponse.fromEntity(userKyc);
    }

    /**
     * Handle Sumsub webhook callbacks
     * Called when document verification completes
     */
    @Transactional
    public void handleSumsubWebhook(String payload, String signature) {
        log.info("Received Sumsub webhook");

        try {
            // TODO: Verify webhook signature for security
            // Parse webhook payload to extract applicant ID and status

            // For now, this is a placeholder
            // In production, parse JSON payload and extract:
            // - applicantId
            // - reviewStatus (approved/rejected)
            // - reviewResult details

        } catch (Exception e) {
            log.error("Error processing Sumsub webhook: {}", e.getMessage(), e);
        }
    }

    /**
     * Manual retry endpoint
     */
    @Transactional
    public KycSubmissionResponse retryKycProcess(Long userId) {
        UserKyc userKyc = kycRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("KYC record not found"));

        // Determine which tier to retry based on current status
        if (userKyc.getKycTier() == UserKyc.KycTier.TIER_1) {
            asyncProcessor.processTier2Async(userKyc.getId());
        } else if (userKyc.getKycTier() == UserKyc.KycTier.TIER_2) {
            asyncProcessor.processTier3Async(userKyc.getId());
        }

        return createResponse(true, "KYC process restarted", userId,
                userKyc.getKycTier(), userKyc.getKycStatus());
    }

    /**
     * Internal method: Process Tier 2 (called by async processor)
     * This runs in background thread
     */
    @Transactional
    public void processTier2Internal(Long kycId) {
        log.info("Starting Tier 2 background processing for kycId: {}", kycId);

        try {
            UserKyc userKyc = kycRepository.findById(kycId)
                    .orElseThrow(() -> new RuntimeException("KYC record not found"));

            // Step 1: Create Sumsub applicant
            SumsubApplicantResponse sumsubResponse = sumsubClient.createApplicant(
                    String.valueOf(userKyc.getUserId()),
                    userKyc.getFirstName(),
                    userKyc.getLastName(),
                    userKyc.getEmail(),
                    "basic-kyc-level");

            // Step 2: Save Sumsub reference
            userKyc.setSumsubApplicantId(sumsubResponse.getId());
            userKyc.setSumsubInspectionId(sumsubResponse.getInspectionId());
            userKyc.setKycStatus(UserKyc.KycStatus.UNDER_REVIEW);
            userKyc.setVerificationMessage("Documents submitted to Sumsub for verification");

            kycRepository.save(userKyc);

            log.info("Tier 2 initiated for userId: {}. Sumsub applicant ID: {}",
                    userKyc.getUserId(), sumsubResponse.getId());

            // Note: Actual document verification happens via Sumsub SDK on frontend
            // Sumsub will call our webhook when verification completes

        } catch (Exception e) {
            log.error("Error in Tier 2 processing: {}", e.getMessage(), e);

            UserKyc userKyc = kycRepository.findById(kycId).orElse(null);
            if (userKyc != null) {
                userKyc.setKycStatus(UserKyc.KycStatus.REJECTED);
                userKyc.setRejectionReason("Tier 2 processing failed: " + e.getMessage());
                kycRepository.save(userKyc);
            }
        }
    }

    /**
     * Internal method: Process Tier 3 (called by async processor or webhook)
     * Runs AML/PEP screening automatically after Tier 2 approval
     */
    @Transactional
    public void processTier3Internal(Long kycId) {
        log.info("Starting Tier 3 AML/PEP screening for kycId: {}", kycId);

        try {
            UserKyc userKyc = kycRepository.findById(kycId)
                    .orElseThrow(() -> new RuntimeException("KYC record not found"));

            // Step 1: Run OpenSanctions AML/PEP check
            OpenSanctionsService.ScreeningResult screeningResult = sanctionsService.screenPerson(
                    userKyc.getFirstName(),
                    userKyc.getLastName(),
                    userKyc.getDateOfBirth(),
                    userKyc.getCountryOfResidence());

            // Step 2: Update KYC record with results
            userKyc.setAmlScreeningStatus(screeningResult.hasAmlHit()
                    ? UserKyc.AmlStatus.HIT
                    : UserKyc.AmlStatus.CLEAR);

            userKyc.setPepScreeningStatus(screeningResult.hasPepMatch()
                    ? UserKyc.PepStatus.MATCH
                    : UserKyc.PepStatus.CLEAR);

            userKyc.setSanctionsMatch(screeningResult.hasSanctionsMatch());
            userKyc.setPepMatch(screeningResult.hasPepMatch());
            userKyc.setScreeningNotes(screeningResult.getNotes());
            userKyc.setLastScreenedAt(Instant.now());
            userKyc.setRiskScore(screeningResult.getRiskScore());

            // Step 3: Determine final KYC status
            if (screeningResult.isClean()) {
                // ALL CLEAR - Full KYC approval
                userKyc.setKycTier(UserKyc.KycTier.TIER_3);
                userKyc.setKycStatus(UserKyc.KycStatus.APPROVED);
                userKyc.setTier3CompletedAt(Instant.now());
                userKyc.setReviewedAt(Instant.now());
                userKyc.setVerificationMessage("KYC fully approved. All compliance checks passed.");

                log.info("Tier 3 complete. User {} fully verified.", userKyc.getUserId());

            } else {
                // HITS FOUND - Requires manual review
                userKyc.setKycStatus(UserKyc.KycStatus.UNDER_REVIEW);
                userKyc.setVerificationMessage("AML/PEP hits detected. Manual review required.");

                log.warn("Tier 3 screening found hits for user {}. Manual review needed.",
                        userKyc.getUserId());
            }

            kycRepository.save(userKyc);

        } catch (Exception e) {
            log.error("Error in Tier 3 processing: {}", e.getMessage(), e);

            UserKyc userKyc = kycRepository.findById(kycId).orElse(null);
            if (userKyc != null) {
                userKyc.setKycStatus(UserKyc.KycStatus.UNDER_REVIEW);
                userKyc.setVerificationMessage("AML screening failed. Manual review required.");
                kycRepository.save(userKyc);
            }
        }
    }

    /**
     * Helper method to create response DTOs
     */
    private KycSubmissionResponse createResponse(
            boolean success,
            String message,
            Long userId,
            UserKyc.KycTier tier,
            UserKyc.KycStatus status) {

        KycSubmissionResponse response = new KycSubmissionResponse(success, message);
        response.setUserId(userId);
        response.setTierCompleted(tier);
        response.setStatus(status);
        return response;
    }
}