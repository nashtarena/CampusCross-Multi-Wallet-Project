package com.campuscross.fx_service.service;

import com.campuscross.fx_service.client.SumsubClient;
import com.campuscross.fx_service.dto.kyc.*;
import com.campuscross.fx_service.dto.sumsub.SumsubApplicantResponse;
import com.campuscross.fx_service.model.UserKyc;
import com.campuscross.fx_service.repository.UserKycRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * KYC Service - Orchestrates the three-tier KYC process
 * 
 * Flow:
 * 1. Tier 1: Instant validation + sanctions check + Create Sumsub applicant (<
 * 2 seconds)
 * 2. User uploads documents via Sumsub SDK in mobile/web app
 * 3. Tier 2: Sumsub webhook triggers when documents verified
 * 4. Tier 3: Automatic AML/PEP screening after Tier 2 completes
 */
@Service
public class KycService {

    private static final Logger log = LoggerFactory.getLogger(KycService.class);

    private final UserKycRepository kycRepository;
    private final SumsubClient sumsubClient;
    private final OpenSanctionsService sanctionsService;
    private final KycAsyncProcessor asyncProcessor;
    private final SumsubWebhookHandler webhookHandler;
    private final SumsubTokenService tokenService;

    public KycService(
            UserKycRepository kycRepository,
            SumsubClient sumsubClient,
            OpenSanctionsService sanctionsService,
            KycAsyncProcessor asyncProcessor,
            SumsubWebhookHandler webhookHandler,
            SumsubTokenService tokenService) {
        this.kycRepository = kycRepository;
        this.sumsubClient = sumsubClient;
        this.sanctionsService = sanctionsService;
        this.asyncProcessor = asyncProcessor;
        this.webhookHandler = webhookHandler;
        this.tokenService = tokenService;
    }

    /**
     * Process Tier 1 KYC - SYNCHRONOUS (2-3 seconds)
     * 
     * Steps:
     * 1. Save basic user information
     * 2. Run instant sanctions check (country-level)
     * 3. Create Sumsub applicant IMMEDIATELY (not async)
     * 4. Generate SDK access token
     * 5. Return token so mobile/web app can launch Sumsub SDK
     * 
     * Returns immediately with access token for SDK
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

            userKyc = kycRepository.save(userKyc);

            // CRITICAL CHANGE: Create Sumsub applicant SYNCHRONOUSLY
            // This happens NOW, not in background
            String levelName = request.getLevelName() != null
                    ? request.getLevelName()
                    : "basic-kyc-level";

            log.info("Creating Sumsub applicant for userId: {}", request.getUserId());

            SumsubApplicantResponse sumsubResponse = sumsubClient.createApplicant(
                    String.valueOf(userKyc.getUserId()),
                    userKyc.getFirstName(),
                    userKyc.getLastName(),
                    userKyc.getEmail(),
                    levelName);

            // Save Sumsub references
            userKyc.setSumsubApplicantId(sumsubResponse.getId());
            userKyc.setSumsubInspectionId(sumsubResponse.getInspectionId());
            userKyc.setVerificationMessage("Ready for document upload");
            kycRepository.save(userKyc);

            log.info("Sumsub applicant created: {}", sumsubResponse.getId());

            // Step 4: Generate access token for SDK
            String accessToken = tokenService.generateAccessToken(
                    String.valueOf(userKyc.getUserId()),
                    levelName);

            log.info("Tier 1 completed for userId: {}. Ready for document upload.",
                    request.getUserId());

            // Step 5: Return response with access token
            KycSubmissionResponse response = createResponse(
                    true,
                    "Tier 1 KYC completed. Please upload your documents.",
                    request.getUserId(),
                    UserKyc.KycTier.TIER_1,
                    UserKyc.KycStatus.PENDING);

            // CRITICAL: Include access token and applicant ID
            response.setSumsubAccessToken(accessToken);
            response.setSumsubApplicantId(sumsubResponse.getId());
            response.setNextStep("Upload your identity documents using the verification screen");

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
            // Delegate to webhook handler
            webhookHandler.processWebhook(payload, signature);

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

        // Only retry Tier 3 if Tier 2 is complete
        if (userKyc.getKycTier() == UserKyc.KycTier.TIER_2) {
            asyncProcessor.processTier3Async(userKyc.getId());
        }

        return createResponse(true, "KYC process restarted", userId,
                userKyc.getKycTier(), userKyc.getKycStatus());
    }

    /**
     * Internal method: Process Tier 3 (called by webhook after Tier 2 approval)
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