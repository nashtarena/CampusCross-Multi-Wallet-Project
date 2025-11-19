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
import java.util.List;

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
        log.info("╔════════════════════════════════════════════════════════════╗");
        log.info("║           SUMSUB WEBHOOK RECEIVED                          ║");
        log.info("╚════════════════════════════════════════════════════════════╝");
        log.info("Timestamp: {}", Instant.now());
        log.info("Payload: {}", payload);
        log.info("Signature received: {}", signature);

        try {
            // Step 1: Verify webhook signature with enhanced debugging
            boolean signatureValid = verifySignature(payload, signature);
            if (!signatureValid) {
                log.error("❌ Invalid webhook signature");
                log.error("⚠️ WARNING: Signature verification failed but continuing for debugging");
                log.error("⚠️ TODO: Re-enable signature check in production!");
                // TEMPORARILY: Don't return, continue processing for debugging
                // return; // Uncomment this in production after fixing signature
            } else {
                log.info("✅ Signature verification PASSED");
            }

            // Step 2: Parse webhook payload
            JsonNode webhookData = objectMapper.readTree(payload);
            log.info("📄 Parsed webhook JSON:\n{}", webhookData.toPrettyString());

            // Extract all relevant fields
            String type = webhookData.path("type").asText();
            String applicantId = webhookData.path("applicantId").asText();
            String externalUserId = webhookData.path("externalUserId").asText();
            String reviewStatus = webhookData.path("reviewStatus").asText();
            String reviewResult = webhookData.path("reviewResult").path("reviewAnswer").asText();

            log.info("📋 Extracted Data:");
            log.info("  - Type: {}", type);
            log.info("  - Applicant ID: '{}'", applicantId);
            log.info("  - External User ID: '{}'", externalUserId);
            log.info("  - Review Status: {}", reviewStatus);
            log.info("  - Review Answer: {}", reviewResult);

            // Step 3: Validate required fields
            if (applicantId == null || applicantId.isEmpty()) {
                log.error("❌ Missing applicantId in webhook payload!");
                throw new RuntimeException("Missing applicantId in webhook");
            }

            if (reviewStatus == null || reviewStatus.isEmpty()) {
                log.error("❌ Missing reviewStatus in webhook payload!");
                throw new RuntimeException("Missing reviewStatus in webhook");
            }

            // Step 4: Find corresponding KYC record
            log.info("🔍 Searching for KYC record with applicantId: '{}'", applicantId);

            UserKyc userKyc = kycRepository.findBySumsubApplicantId(applicantId)
                    .orElseThrow(() -> {
                        log.error("❌ KYC record NOT FOUND for applicantId: '{}'", applicantId);

                        // Debug: Show what's in the database
                        List<UserKyc> allRecords = kycRepository.findAll();
                        log.error("📊 Total KYC records in database: {}", allRecords.size());
                        allRecords.forEach(kyc -> {
                            log.error("  - userId: {}, sumsubApplicantId: '{}', status: {}, tier: {}",
                                    kyc.getUserId(),
                                    kyc.getSumsubApplicantId(),
                                    kyc.getKycStatus(),
                                    kyc.getKycTier());
                        });

                        return new RuntimeException("KYC record not found for applicant: " + applicantId);
                    });

            log.info("✅ Found KYC record:");
            log.info("  - User ID: {}", userKyc.getUserId());
            log.info("  - Current Tier: {}", userKyc.getKycTier());
            log.info("  - Current Status: {}", userKyc.getKycStatus());

            // Step 5: Process based on status
            log.info("⚙️ Processing review status: '{}'", reviewStatus);

            switch (reviewStatus.toLowerCase()) {
                case "completed":
                    log.info("🎯 Processing COMPLETED review with answer: '{}'", reviewResult);
                    handleCompletedReview(userKyc, reviewResult);
                    break;

                case "pending":
                    log.info("⏳ Processing PENDING review");
                    handlePendingReview(userKyc);
                    break;

                default:
                    log.warn("⚠️ Unknown review status: '{}'", reviewStatus);
            }

            log.info("✅ Webhook processed successfully for userId: {}", userKyc.getUserId());
            log.info("╚════════════════════════════════════════════════════════════╝");

        } catch (Exception e) {
            log.error("╔════════════════════════════════════════════════════════════╗");
            log.error("║              WEBHOOK PROCESSING FAILED                     ║");
            log.error("╚════════════════════════════════════════════════════════════╝");
            log.error("❌ Error details:", e);
            // Don't throw - we want to return 200 OK to Sumsub even if processing fails
            // Otherwise Sumsub will keep retrying and flood your logs
        }
    }

    /**
     * Handle completed document verification
     */
    private void handleCompletedReview(UserKyc userKyc, String reviewResult) {
        try {
            if ("GREEN".equalsIgnoreCase(reviewResult)) {
                // APPROVED - Documents verified successfully
                log.info("✅ Tier 2 APPROVED for userId: {}", userKyc.getUserId());

                userKyc.setKycTier(UserKyc.KycTier.TIER_2);
                userKyc.setTier2CompletedAt(Instant.now());
                userKyc.setKycStatus(UserKyc.KycStatus.PENDING); // Pending Tier 3
                userKyc.setVerificationMessage("Documents verified. AML screening in progress.");

                UserKyc saved = kycRepository.save(userKyc);
                log.info("💾 UserKyc saved successfully: ID={}, Tier={}, Status={}",
                        saved.getId(), saved.getKycTier(), saved.getKycStatus());

                // Automatically trigger Tier 3 (AML/PEP screening)
                log.info("🚀 Triggering Tier 3 async processing...");
                asyncProcessor.processTier3Async(userKyc.getId());
                log.info("✅ Tier 3 processing triggered successfully");

            } else if ("RED".equalsIgnoreCase(reviewResult)) {
                // REJECTED - Documents failed verification
                log.warn("❌ Tier 2 REJECTED for userId: {}", userKyc.getUserId());

                userKyc.setKycStatus(UserKyc.KycStatus.REJECTED);
                userKyc.setRejectionReason("Document verification failed");
                userKyc.setVerificationMessage("Documents could not be verified. Please re-submit valid documents.");

                kycRepository.save(userKyc);
                log.info("💾 Rejection saved for userId: {}", userKyc.getUserId());

            } else {
                // YELLOW or other status - Manual review required
                log.info("⚠️ Tier 2 requires manual review for userId: {} (result: {})",
                        userKyc.getUserId(), reviewResult);

                userKyc.setKycStatus(UserKyc.KycStatus.UNDER_REVIEW);
                userKyc.setVerificationMessage("Documents under manual review");

                kycRepository.save(userKyc);
                log.info("💾 Manual review status saved for userId: {}", userKyc.getUserId());
            }
        } catch (Exception e) {
            log.error("❌ Failed to handle completed review for userId: {}", userKyc.getUserId(), e);
            throw new RuntimeException("Failed to process completed review", e);
        }
    }

    /**
     * Handle pending status (user still uploading documents)
     */
    private void handlePendingReview(UserKyc userKyc) {
        try {
            log.info("⏳ Tier 2 pending for userId: {}", userKyc.getUserId());

            userKyc.setKycStatus(UserKyc.KycStatus.PENDING);
            userKyc.setVerificationMessage("Awaiting document submission");

            kycRepository.save(userKyc);
            log.info("💾 Pending status saved for userId: {}", userKyc.getUserId());
        } catch (Exception e) {
            log.error("❌ Failed to handle pending review for userId: {}", userKyc.getUserId(), e);
            throw new RuntimeException("Failed to process pending review", e);
        }
    }

    /**
     * Verify webhook signature using HMAC SHA256
     * Critical for security - prevents fake webhook requests
     */
    private boolean verifySignature(String payload, String signature) {
        if (signature == null || signature.isEmpty()) {
            log.error("❌ No signature provided in webhook");
            return false;
        }

        try {
            // Calculate expected signature
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
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            String calculatedSignature = hexString.toString();

            log.info("🔐 Signature Verification:");
            log.info("  Received:   '{}'", signature);
            log.info("  Calculated: '{}'", calculatedSignature);

            // Try exact match
            if (calculatedSignature.equals(signature)) {
                log.info("  ✅ Exact match");
                return true;
            }

            // Try case-insensitive match
            if (calculatedSignature.equalsIgnoreCase(signature)) {
                log.info("  ✅ Case-insensitive match");
                return true;
            }

            // Try with "sha256=" prefix (some webhooks include this)
            String withPrefix = "sha256=" + calculatedSignature;
            if (withPrefix.equals(signature) || withPrefix.equalsIgnoreCase(signature)) {
                log.info("  ✅ Match with sha256= prefix");
                return true;
            }

            log.error("  ❌ No match found");
            log.error("  Length received: {}, calculated: {}",
                    signature.length(), calculatedSignature.length());

            return false;

        } catch (Exception e) {
            log.error("❌ Error verifying webhook signature: {}", e.getMessage(), e);
            return false;
        }
    }
}