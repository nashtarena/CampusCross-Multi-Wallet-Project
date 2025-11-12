package com.campuscross.fx_service.service;

import com.campuscross.fx_service.config.SumsubConfig;
import com.campuscross.fx_service.model.UserKyc;
import com.campuscross.fx_service.repository.UserKycRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * Service to handle Sumsub webhook callbacks
 * 
 * Webhook is triggered when:
 * - Document verification completes (approved/rejected)
 * - Manual review status changes
 * - User re-submits documents
 */
@Service
public class SumsubWebhookHandler {

    private static final Logger log = LoggerFactory.getLogger(SumsubWebhookHandler.class);

    private final UserKycRepository kycRepository;
    private final KycAsyncProcessor asyncProcessor;
    private final SumsubConfig sumsubConfig;
    private final ObjectMapper objectMapper;

    public SumsubWebhookHandler(
            UserKycRepository kycRepository,
            KycAsyncProcessor asyncProcessor,
            SumsubConfig sumsubConfig,
            ObjectMapper objectMapper) {
        this.kycRepository = kycRepository;
        this.asyncProcessor = asyncProcessor;
        this.sumsubConfig = sumsubConfig;
        this.objectMapper = objectMapper;
    }

    /**
     * Process incoming Sumsub webhook
     * 
     * @param payload   JSON payload from Sumsub
     * @param signature HMAC signature for verification
     */
    @Transactional
    public void processWebhook(String payload, String signature) {
        log.info("Processing Sumsub webhook");

        try {
            // Step 1: Verify webhook signature
            if (!verifySignature(payload, signature)) {
                log.error("Invalid webhook signature. Possible security breach!");
                return;
            }

            // Step 2: Parse webhook payload
            JsonNode webhookData = objectMapper.readTree(payload);

            String applicantId = webhookData.path("applicantId").asText();
            String reviewStatus = webhookData.path("reviewStatus").asText();
            String reviewResult = webhookData.path("reviewResult").path("reviewAnswer").asText();

            log.info("Webhook received - ApplicantID: {}, Status: {}, Result: {}",
                    applicantId, reviewStatus, reviewResult);

            // Step 3: Find corresponding KYC record
            UserKyc userKyc = kycRepository.findBySumsubApplicantId(applicantId)
                    .orElseThrow(() -> new RuntimeException("KYC record not found for applicant: " + applicantId));

            // Step 4: Process based on status
            switch (reviewStatus) {
                case "completed":
                    handleCompletedReview(userKyc, reviewResult);
                    break;

                case "pending":
                    handlePendingReview(userKyc);
                    break;

                default:
                    log.warn("Unknown review status: {}", reviewStatus);
            }

        } catch (Exception e) {
            log.error("Error processing Sumsub webhook: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle completed document verification
     */
    private void handleCompletedReview(UserKyc userKyc, String reviewResult) {
        if ("GREEN".equalsIgnoreCase(reviewResult)) {
            // APPROVED - Documents verified successfully
            log.info("Tier 2 APPROVED for userId: {}", userKyc.getUserId());

            userKyc.setKycTier(UserKyc.KycTier.TIER_2);
            userKyc.setTier2CompletedAt(Instant.now());
            userKyc.setKycStatus(UserKyc.KycStatus.PENDING); // Pending Tier 3
            userKyc.setVerificationMessage("Documents verified. AML screening in progress.");

            kycRepository.save(userKyc);

            // Automatically trigger Tier 3 (AML/PEP screening)
            asyncProcessor.processTier3Async(userKyc.getId());

        } else if ("RED".equalsIgnoreCase(reviewResult)) {
            // REJECTED - Documents failed verification
            log.warn("Tier 2 REJECTED for userId: {}", userKyc.getUserId());

            userKyc.setKycStatus(UserKyc.KycStatus.REJECTED);
            userKyc.setRejectionReason("Document verification failed");
            userKyc.setVerificationMessage("Documents could not be verified. Please re-submit valid documents.");

            kycRepository.save(userKyc);

        } else {
            // YELLOW or other status - Manual review required
            log.info("Tier 2 requires manual review for userId: {}", userKyc.getUserId());

            userKyc.setKycStatus(UserKyc.KycStatus.UNDER_REVIEW);
            userKyc.setVerificationMessage("Documents under manual review");

            kycRepository.save(userKyc);
        }
    }

    /**
     * Handle pending status (user still uploading documents)
     */
    private void handlePendingReview(UserKyc userKyc) {
        log.info("Tier 2 pending for userId: {}", userKyc.getUserId());

        userKyc.setKycStatus(UserKyc.KycStatus.PENDING);
        userKyc.setVerificationMessage("Awaiting document submission");

        kycRepository.save(userKyc);
    }

    /**
     * Verify webhook signature using HMAC SHA256
     * Critical for security - prevents fake webhook requests
     */
    private boolean verifySignature(String payload, String signature) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    sumsubConfig.getSecretKey().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256");
            hmac.init(secretKey);

            byte[] hash = hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }

            String calculatedSignature = hexString.toString();

            // Compare signatures
            return calculatedSignature.equals(signature);

        } catch (Exception e) {
            log.error("Error verifying webhook signature: {}", e.getMessage(), e);
            return false;
        }
    }
}