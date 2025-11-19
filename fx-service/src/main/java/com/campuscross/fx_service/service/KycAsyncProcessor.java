package com.campuscross.fx_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Lazy;

/**
 * Async processor for background KYC tasks
 * Only handles Tier 3 (AML/PEP screening) in background
 * Tier 2 is now handled synchronously + Sumsub webhook
 */
@Component
public class KycAsyncProcessor {

    private static final Logger log = LoggerFactory.getLogger(KycAsyncProcessor.class);

    private final KycService kycService;

    public KycAsyncProcessor(@Lazy KycService kycService) {
        this.kycService = kycService;
    }

    /**
     * Process Tier 3 in background thread
     * Called automatically after Tier 2 approval (via webhook)
     */
    @Async("kycTaskExecutor")
    public void processTier3Async(Long kycId) {
        log.info("Background Tier 3 processing started for kycId: {}", kycId);

        try {
            Thread.sleep(100); // Small delay to ensure transaction is committed
            kycService.processTier3Internal(kycId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Tier 3 async processing interrupted", e);
        } catch (Exception e) {
            log.error("Error in Tier 3 async processing for kycId {}: {}", kycId, e.getMessage(), e);
        }
    }
}