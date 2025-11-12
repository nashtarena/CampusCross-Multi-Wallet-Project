package com.campuscross.fx_service.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "user_kyc")
public class UserKyc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KycTier kycTier = KycTier.NONE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KycStatus kycStatus = KycStatus.PENDING;

    // ==========================================
    // TIER 1: Personal Information (Basic Info)
    // ==========================================
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private String email;

    @Column(length = 2) // ISO 3166-1 alpha-2 country code
    private String countryOfResidence;

    // Address Information
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String stateProvince;
    private String postalCode;

    // ==========================================
    // TIER 2: Document Verification (Sumsub)
    // ==========================================
    private String sumsubApplicantId; // Sumsub's unique applicant ID
    private String sumsubInspectionId; // Sumsub's inspection/check ID

    private String documentType; // PASSPORT, ID_CARD, DRIVERS_LICENSE
    private String documentNumber; // Actual document number

    @Column(length = 2)
    private String documentCountry; // Issuing country (ISO code)

    // ==========================================
    // TIER 3: AML/PEP Screening (OpenSanctions)
    // ==========================================
    @Enumerated(EnumType.STRING)
    private AmlStatus amlScreeningStatus = AmlStatus.NOT_CHECKED;

    @Enumerated(EnumType.STRING)
    private PepStatus pepScreeningStatus = PepStatus.NOT_CHECKED;

    private Boolean sanctionsMatch = false; // Quick flag for sanctions hit
    private Boolean pepMatch = false; // Quick flag for PEP match

    @Column(columnDefinition = "TEXT")
    private String screeningNotes; // Details of any hits found

    private Instant lastScreenedAt; // Last AML/PEP check timestamp

    // ==========================================
    // Risk Assessment
    // ==========================================
    private Integer riskScore; // 0-100, calculated based on various factors

    // ==========================================
    // Status Management & Audit Trail
    // ==========================================
    private Instant submittedAt; // When user submitted KYC
    private Instant reviewedAt; // When manual review completed (if needed)

    @Column(columnDefinition = "TEXT")
    private String rejectionReason; // Reason if rejected

    @Column(columnDefinition = "TEXT")
    private String verificationMessage; // General status message

    // Tier Completion Timestamps (for tracking progression)
    private Instant tier1CompletedAt;
    private Instant tier2CompletedAt;
    private Instant tier3CompletedAt;

    // Record Metadata
    @Column(updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    // ==========================================
    // ENUMS
    // ==========================================
    public enum KycTier {
        NONE, // Not started
        TIER_1, // Basic info collected
        TIER_2, // Documents verified via Sumsub
        TIER_3 // Full compliance (AML/PEP cleared)
    }

    public enum KycStatus {
        PENDING, // Awaiting verification
        UNDER_REVIEW, // Manual review required
        APPROVED, // Fully verified
        REJECTED // Failed verification
    }

    public enum AmlStatus {
        NOT_CHECKED, // AML screening not yet performed
        CLEAR, // No sanctions/watchlist hits
        HIT, // Potential match found
        UNDER_REVIEW // Manual review of hit required
    }

    public enum PepStatus {
        NOT_CHECKED, // PEP screening not yet performed
        CLEAR, // Not a politically exposed person
        MATCH, // PEP match found
        UNDER_REVIEW // Manual review required
    }

    // ==========================================
    // LIFECYCLE CALLBACKS
    // ==========================================
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // ==========================================
    // CONSTRUCTORS
    // ==========================================
    public UserKyc() {
    }

    public UserKyc(Long userId) {
        this.userId = userId;
    }

    // ==========================================
    // GETTERS AND SETTERS
    // ==========================================
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public KycTier getKycTier() {
        return kycTier;
    }

    public void setKycTier(KycTier kycTier) {
        this.kycTier = kycTier;
    }

    public KycStatus getKycStatus() {
        return kycStatus;
    }

    public void setKycStatus(KycStatus kycStatus) {
        this.kycStatus = kycStatus;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCountryOfResidence() {
        return countryOfResidence;
    }

    public void setCountryOfResidence(String countryOfResidence) {
        this.countryOfResidence = countryOfResidence;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStateProvince() {
        return stateProvince;
    }

    public void setStateProvince(String stateProvince) {
        this.stateProvince = stateProvince;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getSumsubApplicantId() {
        return sumsubApplicantId;
    }

    public void setSumsubApplicantId(String sumsubApplicantId) {
        this.sumsubApplicantId = sumsubApplicantId;
    }

    public String getSumsubInspectionId() {
        return sumsubInspectionId;
    }

    public void setSumsubInspectionId(String sumsubInspectionId) {
        this.sumsubInspectionId = sumsubInspectionId;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getDocumentCountry() {
        return documentCountry;
    }

    public void setDocumentCountry(String documentCountry) {
        this.documentCountry = documentCountry;
    }

    public AmlStatus getAmlScreeningStatus() {
        return amlScreeningStatus;
    }

    public void setAmlScreeningStatus(AmlStatus amlScreeningStatus) {
        this.amlScreeningStatus = amlScreeningStatus;
    }

    public PepStatus getPepScreeningStatus() {
        return pepScreeningStatus;
    }

    public void setPepScreeningStatus(PepStatus pepScreeningStatus) {
        this.pepScreeningStatus = pepScreeningStatus;
    }

    public Boolean getSanctionsMatch() {
        return sanctionsMatch;
    }

    public void setSanctionsMatch(Boolean sanctionsMatch) {
        this.sanctionsMatch = sanctionsMatch;
    }

    public Boolean getPepMatch() {
        return pepMatch;
    }

    public void setPepMatch(Boolean pepMatch) {
        this.pepMatch = pepMatch;
    }

    public String getScreeningNotes() {
        return screeningNotes;
    }

    public void setScreeningNotes(String screeningNotes) {
        this.screeningNotes = screeningNotes;
    }

    public Instant getLastScreenedAt() {
        return lastScreenedAt;
    }

    public void setLastScreenedAt(Instant lastScreenedAt) {
        this.lastScreenedAt = lastScreenedAt;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Instant reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getVerificationMessage() {
        return verificationMessage;
    }

    public void setVerificationMessage(String verificationMessage) {
        this.verificationMessage = verificationMessage;
    }

    public Instant getTier1CompletedAt() {
        return tier1CompletedAt;
    }

    public void setTier1CompletedAt(Instant tier1CompletedAt) {
        this.tier1CompletedAt = tier1CompletedAt;
    }

    public Instant getTier2CompletedAt() {
        return tier2CompletedAt;
    }

    public void setTier2CompletedAt(Instant tier2CompletedAt) {
        this.tier2CompletedAt = tier2CompletedAt;
    }

    public Instant getTier3CompletedAt() {
        return tier3CompletedAt;
    }

    public void setTier3CompletedAt(Instant tier3CompletedAt) {
        this.tier3CompletedAt = tier3CompletedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}