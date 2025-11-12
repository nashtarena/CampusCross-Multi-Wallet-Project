package com.campuscross.fx_service.repository;

import com.campuscross.fx_service.model.UserKyc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserKycRepository extends JpaRepository<UserKyc, Long> {

    /**
     * Find KYC record by user ID (most common query)
     */
    Optional<UserKyc> findByUserId(Long userId);

    /**
     * Check if user has already started KYC process
     */
    boolean existsByUserId(Long userId);

    /**
     * Find all users by KYC status (for admin dashboard)
     */
    List<UserKyc> findByKycStatus(UserKyc.KycStatus status);

    /**
     * Find all users at a specific KYC tier
     */
    List<UserKyc> findByKycTier(UserKyc.KycTier tier);

    /**
     * Find all users requiring manual review (AML hits or PEP matches)
     */
    @Query("SELECT k FROM UserKyc k WHERE k.kycStatus = 'UNDER_REVIEW' OR k.amlScreeningStatus = 'HIT' OR k.pepScreeningStatus = 'MATCH'")
    List<UserKyc> findAllRequiringReview();

    /**
     * Find users with sanctions hits (for compliance dashboard)
     */
    List<UserKyc> findBySanctionsMatchTrue();

    /**
     * Find users with PEP matches (for compliance dashboard)
     */
    List<UserKyc> findByPepMatchTrue();

    /**
     * Find by Sumsub applicant ID (for webhook handling)
     */
    Optional<UserKyc> findBySumsubApplicantId(String sumsubApplicantId);

    /**
     * Find all approved users at specific tier (for analytics)
     */
    List<UserKyc> findByKycTierAndKycStatus(UserKyc.KycTier tier, UserKyc.KycStatus status);

    /**
     * Count users by status (for dashboard metrics)
     */
    long countByKycStatus(UserKyc.KycStatus status);

    /**
     * Find users pending AML screening (Tier 2 complete but not Tier 3)
     */
    @Query("SELECT k FROM UserKyc k WHERE k.kycTier = 'TIER_2' AND k.amlScreeningStatus = 'NOT_CHECKED'")
    List<UserKyc> findPendingAmlScreening();
}