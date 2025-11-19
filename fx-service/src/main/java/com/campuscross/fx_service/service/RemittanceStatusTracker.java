package com.campuscross.fx_service.service;

import com.campuscross.fx_service.model.Remittance;
import com.campuscross.fx_service.repository.RemittanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Background service to track remittance status
 * Polls Airwallex API for pending transfers
 */
@Service
public class RemittanceStatusTracker {

    private static final Logger log = LoggerFactory.getLogger(RemittanceStatusTracker.class);

    private final RemittanceRepository remittanceRepository;
    private final RemittanceService remittanceService;

    public RemittanceStatusTracker(
            RemittanceRepository remittanceRepository,
            RemittanceService remittanceService) {
        this.remittanceRepository = remittanceRepository;
        this.remittanceService = remittanceService;
    }

    /**
     * Check status of all processing remittances every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void trackPendingRemittances() {
        log.debug("Checking status of pending remittances...");

        try {
            // Get all processing remittances
            List<Remittance> processingRemittances = remittanceRepository
                    .findByStatus(Remittance.TransferStatus.PROCESSING);

            log.info("Found {} processing remittances to track", processingRemittances.size());

            for (Remittance remittance : processingRemittances) {
                try {
                    // Update status from Airwallex
                    remittanceService.updateRemittanceStatus(remittance.getReferenceNumber());
                } catch (Exception e) {
                    log.error("Error tracking remittance {}: {}",
                            remittance.getReferenceNumber(), e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("Error in remittance status tracking: {}", e.getMessage(), e);
        }
    }

    /**
     * Clean up old completed/failed remittances (optional)
     * Runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void archiveOldRemittances() {
        log.info("Archiving old remittances...");

        // Implementation for archiving old records
        // This is optional - you might want to keep all records

        // Example: Archive remittances older than 90 days
        // Instant ninetyDaysAgo = Instant.now().minus(90, ChronoUnit.DAYS);
        // List<Remittance> oldRemittances =
        // remittanceRepository.findOldCompletedRemittances(ninetyDaysAgo);
        // Archive to separate table or mark as archived
    }
}