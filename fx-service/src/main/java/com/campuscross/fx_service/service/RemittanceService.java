package com.campuscross.fx_service.service;

import com.campuscross.fx_service.client.AirwallexClient;
import com.campuscross.fx_service.dto.airwallex.AirwallexPayoutRequest;
import com.campuscross.fx_service.dto.airwallex.AirwallexPayoutResponse;
import com.campuscross.fx_service.dto.remittance.RemittanceRequest;
import com.campuscross.fx_service.dto.remittance.RemittanceResponse;
import com.campuscross.fx_service.dto.remittance.RemittanceStatusResponse;
import com.campuscross.fx_service.model.Remittance;
import com.campuscross.fx_service.model.UserKyc;
import com.campuscross.fx_service.repository.RemittanceRepository;
import com.campuscross.fx_service.repository.UserKycRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Remittance Service - Handles bank transfers with FX conversion
 * Integrates with existing FX, KYC, and Airwallex systems
 */
@Service
public class RemittanceService {

    private static final Logger log = LoggerFactory.getLogger(RemittanceService.class);

    // Transfer fee configuration
    private static final BigDecimal TRANSFER_FEE_PERCENTAGE = new BigDecimal("0.01"); // 1%
    private static final BigDecimal MIN_TRANSFER_FEE = new BigDecimal("5.00");
    private static final BigDecimal MAX_TRANSFER_FEE = new BigDecimal("50.00");

    private final RemittanceRepository remittanceRepository;
    private final UserKycRepository kycRepository;
    private final FxService fxService;
    private final AirwallexClient airwallexClient;

    public RemittanceService(
            RemittanceRepository remittanceRepository,
            UserKycRepository kycRepository,
            FxService fxService,
            AirwallexClient airwallexClient) {
        this.remittanceRepository = remittanceRepository;
        this.kycRepository = kycRepository;
        this.fxService = fxService;
        this.airwallexClient = airwallexClient;
    }

    /**
     * Create a new remittance
     * Steps:
     * 1. Verify KYC status
     * 2. Get FX rate
     * 3. Calculate fees
     * 4. Create remittance record
     * 5. Initiate transfer (async)
     */
    @Transactional
    public RemittanceResponse createRemittance(RemittanceRequest request) {
        log.info("Creating remittance for userId: {}, amount: {} {}",
                request.getUserId(), request.getSourceAmount(), request.getSourceCurrency());

        try {
            // Step 1: Verify KYC - CRITICAL for compliance
            if (!verifyKycStatus(request.getUserId())) {
                return new RemittanceResponse(false,
                        "KYC verification required. Please complete your profile verification.");
            }

            // Step 2: Get current FX rate from your existing FX service
            Optional<BigDecimal> fxRateOpt = fxService.getCustomerQuote(
                    request.getSourceCurrency(),
                    request.getDestinationCurrency());

            if (fxRateOpt.isEmpty()) {
                return new RemittanceResponse(false,
                        "Unable to get exchange rate. Please try again later.");
            }

            BigDecimal fxRate = fxRateOpt.get();

            // Step 3: Calculate destination amount and fees
            BigDecimal destinationAmount = request.getSourceAmount()
                    .multiply(fxRate)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal transferFee = calculateTransferFee(request.getSourceAmount());

            // Step 4: Create remittance entity
            Remittance remittance = new Remittance(request.getUserId());
            remittance.setSourceAmount(request.getSourceAmount());
            remittance.setSourceCurrency(request.getSourceCurrency());
            remittance.setDestinationAmount(destinationAmount);
            remittance.setDestinationCurrency(request.getDestinationCurrency());
            remittance.setFxRate(fxRate);
            remittance.setTransferFee(transferFee);

            // Beneficiary details
            remittance.setBeneficiaryName(request.getBeneficiaryName());
            remittance.setBeneficiaryAccountNumber(request.getBeneficiaryAccountNumber());
            remittance.setBeneficiaryBankName(request.getBeneficiaryBankName());
            remittance.setBeneficiaryBankCode(request.getBeneficiaryBankCode());
            remittance.setBeneficiaryAddress(request.getBeneficiaryAddress());
            remittance.setBeneficiaryCountry(request.getBeneficiaryCountry());
            remittance.setTransferPurpose(request.getTransferPurpose());
            remittance.setNotes(request.getNotes());

            // Set transfer type
            remittance.setTransferType(determineTransferType(request.getBeneficiaryCountry()));

            // Mark as KYC verified
            remittance.setKycVerified(true);
            remittance.setAmlCleared(true);

            // Initial status
            remittance.setStatus(Remittance.TransferStatus.PENDING);
            remittance.setStatusMessage("Transfer initiated. Processing...");

            // Save to database
            remittance = remittanceRepository.save(remittance);

            log.info("Remittance created: {} with rate: {}",
                    remittance.getReferenceNumber(), fxRate);

            // Step 5: Process transfer asynchronously
            processTransferAsync(remittance.getId());

            // Return response
            RemittanceResponse response = RemittanceResponse.fromEntity(remittance);
            response.setMessage("Transfer initiated successfully. Reference: " +
                    remittance.getReferenceNumber());

            // Estimate completion time (1-3 business days)
            response.setEstimatedCompletionTime(
                    Instant.now().plusSeconds(3 * 24 * 60 * 60));

            return response;

        } catch (Exception e) {
            log.error("Error creating remittance: {}", e.getMessage(), e);
            return new RemittanceResponse(false,
                    "Failed to create remittance: " + e.getMessage());
        }
    }

    /**
     * Get remittance status by reference number
     */
    public RemittanceStatusResponse getRemittanceStatus(String referenceNumber) {
        Remittance remittance = remittanceRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new RuntimeException("Remittance not found: " + referenceNumber));

        return RemittanceStatusResponse.fromEntity(remittance);
    }

    /**
     * Get all remittances for a user
     */
    public List<RemittanceStatusResponse> getUserRemittances(Long userId) {
        return remittanceRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(RemittanceStatusResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Cancel a pending remittance
     */
    @Transactional
    public boolean cancelRemittance(String referenceNumber, Long userId) {
        Optional<Remittance> remittanceOpt = remittanceRepository.findByReferenceNumber(referenceNumber);

        if (remittanceOpt.isEmpty()) {
            return false;
        }

        Remittance remittance = remittanceOpt.get();

        // Verify ownership
        if (!remittance.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Remittance does not belong to user");
        }

        // Can only cancel pending transfers
        if (remittance.getStatus() != Remittance.TransferStatus.PENDING) {
            throw new RuntimeException("Cannot cancel transfer in status: " + remittance.getStatus());
        }

        remittance.setStatus(Remittance.TransferStatus.CANCELLED);
        remittance.setStatusMessage("Transfer cancelled by user");
        remittanceRepository.save(remittance);

        log.info("Transfer cancelled: {}", referenceNumber);
        return true;
    }

    /**
     * Process transfer via Airwallex (called internally or by scheduler)
     */
    @Transactional
    public void processTransfer(Long remittanceId) {
        log.info("Processing transfer for remittanceId: {}", remittanceId);

        try {
            Remittance remittance = remittanceRepository.findById(remittanceId)
                    .orElseThrow(() -> new RuntimeException("Remittance not found"));

            // Update status to processing
            remittance.setStatus(Remittance.TransferStatus.PROCESSING);
            remittance.setProcessingAt(Instant.now());
            remittance.setStatusMessage("Transfer in progress with Airwallex");
            remittanceRepository.save(remittance);

            // Build Airwallex payout request
            AirwallexPayoutRequest payoutRequest = buildAirwallexPayoutRequest(remittance);

            // Execute payout via Airwallex API
            AirwallexPayoutResponse payoutResponse = airwallexClient.createPayout(payoutRequest);

            // Save Airwallex reference
            remittance.setExternalReferenceId(payoutResponse.getId());
            remittance.setStatusMessage("Transfer submitted to Airwallex. ID: " + payoutResponse.getId());
            remittanceRepository.save(remittance);

            // Check initial status
            if ("COMPLETED".equalsIgnoreCase(payoutResponse.getStatus())) {
                completeTransfer(remittanceId, payoutResponse.getId());
            } else if ("FAILED".equalsIgnoreCase(payoutResponse.getStatus())) {
                failTransfer(remittanceId, payoutResponse.getErrorMessage());
            } else {
                log.info("Transfer pending with Airwallex: {}", payoutResponse.getId());
            }

        } catch (Exception e) {
            log.error("Transfer processing failed for remittanceId {}: {}",
                    remittanceId, e.getMessage());
            failTransfer(remittanceId, e.getMessage());
        }
    }

    /**
     * Update remittance status by polling Airwallex
     * Called by RemittanceStatusTracker
     */
    @Transactional
    public void updateRemittanceStatus(String referenceNumber) {
        Remittance remittance = remittanceRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new RuntimeException("Remittance not found"));

        if (remittance.getExternalReferenceId() == null) {
            log.warn("No external reference for remittance: {}", referenceNumber);
            return;
        }

        try {
            // Query Airwallex for current status
            AirwallexPayoutResponse status = airwallexClient.getPayoutStatus(
                    remittance.getExternalReferenceId());

            // Update based on Airwallex status
            switch (status.getStatus().toUpperCase()) {
                case "COMPLETED":
                    if (remittance.getStatus() != Remittance.TransferStatus.COMPLETED) {
                        completeTransfer(remittance.getId(), remittance.getExternalReferenceId());
                    }
                    break;

                case "FAILED":
                    if (remittance.getStatus() != Remittance.TransferStatus.FAILED) {
                        failTransfer(remittance.getId(), status.getErrorMessage());
                    }
                    break;

                case "PROCESSING":
                case "PENDING":
                    remittance.setStatusMessage("Transfer in progress: " + status.getStatus());
                    remittanceRepository.save(remittance);
                    break;

                default:
                    log.warn("Unknown Airwallex status: {}", status.getStatus());
            }

        } catch (Exception e) {
            log.error("Error updating remittance status: {}", e.getMessage());
        }
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Verify user's KYC status
     */
    private boolean verifyKycStatus(Long userId) {
        Optional<UserKyc> kycOpt = kycRepository.findByUserId(userId);

        if (kycOpt.isEmpty()) {
            log.warn("No KYC record found for userId: {}", userId);
            return false;
        }

        UserKyc kyc = kycOpt.get();

        // Require at least Tier 2 (document verification) for remittances
        boolean isVerified = kyc.getKycTier() != UserKyc.KycTier.NONE &&
                kyc.getKycTier() != UserKyc.KycTier.TIER_1 &&
                kyc.getKycStatus() == UserKyc.KycStatus.APPROVED;

        if (!isVerified) {
            log.warn("KYC not sufficient for userId: {}. Tier: {}, Status: {}",
                    userId, kyc.getKycTier(), kyc.getKycStatus());
        }

        return isVerified;
    }

    /**
     * Calculate transfer fee based on amount
     */
    private BigDecimal calculateTransferFee(BigDecimal amount) {
        BigDecimal fee = amount.multiply(TRANSFER_FEE_PERCENTAGE);

        // Apply min/max limits
        if (fee.compareTo(MIN_TRANSFER_FEE) < 0) {
            fee = MIN_TRANSFER_FEE;
        } else if (fee.compareTo(MAX_TRANSFER_FEE) > 0) {
            fee = MAX_TRANSFER_FEE;
        }

        return fee.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Determine transfer type based on beneficiary country
     */
    private Remittance.TransferType determineTransferType(String countryCode) {
        // Use Airwallex for all transfers
        // In production, you could check country and use:
        // - SEPA for EU countries
        // - FEDWIRE for US
        // - SWIFT for others
        return Remittance.TransferType.AIRWALLEX;
    }

    /**
     * Process transfer asynchronously
     */
    private void processTransferAsync(Long remittanceId) {
        // In production, use @Async or queue to Kafka
        // For now, simulate async with thread
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Simulate delay
                processTransfer(remittanceId);
            } catch (Exception e) {
                log.error("Error in async transfer processing: {}", e.getMessage());
            }
        }).start();
    }

    /**
     * Build Airwallex payout request from remittance entity
     */
    private AirwallexPayoutRequest buildAirwallexPayoutRequest(Remittance remittance) {
        AirwallexPayoutRequest request = new AirwallexPayoutRequest();

        request.setRequestId(remittance.getReferenceNumber());
        request.setSourceCurrency(remittance.getSourceCurrency());
        request.setSourceAmount(remittance.getSourceAmount().toString());
        request.setReason(remittance.getTransferPurpose());
        request.setReference(remittance.getReferenceNumber());

        // Build beneficiary
        AirwallexPayoutRequest.Beneficiary beneficiary = new AirwallexPayoutRequest.Beneficiary();
        beneficiary.setAccountName(remittance.getBeneficiaryName());
        beneficiary.setAccountNumber(remittance.getBeneficiaryAccountNumber());
        beneficiary.setBankName(remittance.getBeneficiaryBankName());
        beneficiary.setSwiftCode(remittance.getBeneficiaryBankCode());
        beneficiary.setAccountRoutingType1("swift_code");
        beneficiary.setAccountRoutingValue1(remittance.getBeneficiaryBankCode());

        // Build address
        AirwallexPayoutRequest.Beneficiary.Address address = new AirwallexPayoutRequest.Beneficiary.Address();
        address.setCountryCode(remittance.getBeneficiaryCountry());

        if (remittance.getBeneficiaryAddress() != null) {
            String[] addressParts = remittance.getBeneficiaryAddress().split(",");
            if (addressParts.length > 0) {
                address.setStreetAddress(addressParts[0].trim());
            }
            if (addressParts.length > 1) {
                address.setCity(addressParts[1].trim());
            }
        }

        beneficiary.setAddress(address);
        request.setBeneficiary(beneficiary);

        return request;
    }

    /**
     * Mark transfer as completed
     */
    @Transactional
    public void completeTransfer(Long remittanceId, String externalReference) {
        Remittance remittance = remittanceRepository.findById(remittanceId)
                .orElseThrow(() -> new RuntimeException("Remittance not found"));

        remittance.setStatus(Remittance.TransferStatus.COMPLETED);
        remittance.setCompletedAt(Instant.now());
        remittance.setExternalReferenceId(externalReference);
        remittance.setStatusMessage("Transfer completed successfully");

        remittanceRepository.save(remittance);

        log.info("Transfer completed: {} with external ref: {}",
                remittance.getReferenceNumber(), externalReference);
    }

    /**
     * Mark transfer as failed
     */
    @Transactional
    public void failTransfer(Long remittanceId, String reason) {
        Remittance remittance = remittanceRepository.findById(remittanceId)
                .orElseThrow(() -> new RuntimeException("Remittance not found"));

        remittance.setStatus(Remittance.TransferStatus.FAILED);
        remittance.setFailedAt(Instant.now());
        remittance.setFailureReason(reason);
        remittance.setStatusMessage("Transfer failed: " + reason);

        remittanceRepository.save(remittance);

        log.error("Transfer failed: {} - Reason: {}",
                remittance.getReferenceNumber(), reason);
    }
}