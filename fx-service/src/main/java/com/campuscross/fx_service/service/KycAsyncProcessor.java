package com.campuscross.fx_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Lazy;

/**
 * Async processor for background KYC tasks
 * Ensures Tier 2 and Tier 3 don't block the user experience
 */
@Component
public class KycAsyncProcessor {

    private static final Logger log = LoggerFactory.getLogger(KycAsyncProcessor.class);

    private final KycService kycService;

    public KycAsyncProcessor(@Lazy KycService kycService) {
        this.kycService = kycService;
    }

    /**
     * Process Tier 2 in background thread
     * This allows Tier 1 to return immediately to the user
     * FIXED: Added executor name to use the correct thread pool
     */
    @Async("kycTaskExecutor")
    public void processTier2Async(Long kycId) {
        log.info("Background Tier 2 processing started for kycId: {}", kycId);

        try {
            // Small delay to ensure transaction is committed
            Thread.sleep(100);
            kycService.processTier2Internal(kycId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Tier 2 async processing interrupted", e);
        } catch (Exception e) {
            log.error("Error in Tier 2 async processing for kycId {}: {}", kycId, e.getMessage(), e);
        }
    }

    /**
     * Process Tier 3 in background thread
     * Called automatically after Tier 2 approval
     * FIXED: Added executor name to use the correct thread pool
     */
    @Async("kycTaskExecutor")
    public void processTier3Async(Long kycId) {
        log.info("Background Tier 3 processing started for kycId: {}", kycId);

        try {
            Thread.sleep(100);
            kycService.processTier3Internal(kycId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Tier 3 async processing interrupted", e);
        } catch (Exception e) {
            log.error("Error in Tier 3 async processing for kycId {}: {}", kycId, e.getMessage(), e);
        }
    }
}