package com.campuscross.fx_service.repository;

import com.campuscross.fx_service.model.Remittance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RemittanceRepository extends JpaRepository<Remittance, Long> {

    /**
     * Find remittance by reference number
     */
    Optional<Remittance> findByReferenceNumber(String referenceNumber);

    /**
     * Find remittance by external provider reference
     */
    Optional<Remittance> findByExternalReferenceId(String externalReferenceId);

    /**
     * Get all remittances for a user
     */
    List<Remittance> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Get remittances by status for a user
     */
    List<Remittance> findByUserIdAndStatus(Long userId, Remittance.TransferStatus status);

    /**
     * Find pending remittances that need processing
     */
    List<Remittance> findByStatus(Remittance.TransferStatus status);

    /**
     * Count remittances by status for analytics
     */
    long countByStatus(Remittance.TransferStatus status);

    /**
     * Find remittances requiring KYC verification
     */
    @Query("SELECT r FROM Remittance r WHERE r.status = 'KYC_REQUIRED' OR r.kycVerified = false")
    List<Remittance> findRequiringKycVerification();
}