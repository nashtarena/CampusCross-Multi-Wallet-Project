package com.campuscross.fx_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Service for AML/PEP screening using OpenSanctions API
 * 
 * OpenSanctions provides:
 * - Sanctions lists (OFAC, UN, EU, etc.)
 * - PEP (Politically Exposed Persons) databases
 * - Adverse media screening
 */
@Service
public class OpenSanctionsService {

    private static final Logger log = LoggerFactory.getLogger(OpenSanctionsService.class);

    // High-risk sanctioned countries (as of 2025)
    private static final Set<String> SANCTIONED_COUNTRIES = new HashSet<>(Arrays.asList(
            "KP", // North Korea
            "IR", // Iran
            "SY", // Syria
            "CU", // Cuba
            "VE" // Venezuela (partial)
    ));

    private final RestTemplate restTemplate;

    public OpenSanctionsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Instant country-level sanctions check (Tier 1)
     * Returns true if country is under comprehensive sanctions
     */
    public boolean isCountrySanctioned(String countryCode) {
        boolean isSanctioned = SANCTIONED_COUNTRIES.contains(countryCode.toUpperCase());

        if (isSanctioned) {
            log.warn("Country {} is under sanctions", countryCode);
        }

        return isSanctioned;
    }

    /**
     * Full person-level AML/PEP screening (Tier 3)
     * 
     * In production, this would call OpenSanctions API:
     * https://api.opensanctions.org/match/default
     * 
     * For MVP, we implement a basic check with mock data
     */
    public ScreeningResult screenPerson(
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String countryCode) {

        log.info("Running AML/PEP screening for: {} {}", firstName, lastName);

        ScreeningResult result = new ScreeningResult();

        try {
            // TODO: Replace with actual OpenSanctions API call
            // Example API call structure:
            // POST https://api.opensanctions.org/match/default
            // Body: { "queries": { "entity": { "properties": { "name": ["John Doe"],
            // "birthDate": ["1990-01-01"] } } } }

            // For MVP demonstration:
            // 1. Check if name matches known test patterns
            boolean hasAmlHit = checkAmlMatch(firstName, lastName);
            boolean hasPepMatch = checkPepMatch(firstName, lastName);
            boolean hasSanctionsMatch = checkSanctionsMatch(firstName, lastName);

            // 2. Calculate risk score (0-100)
            int riskScore = calculateRiskScore(hasAmlHit, hasPepMatch, hasSanctionsMatch, countryCode);

            // 3. Build result
            result.setAmlHit(hasAmlHit);
            result.setPepMatch(hasPepMatch);
            result.setSanctionsMatch(hasSanctionsMatch);
            result.setRiskScore(riskScore);

            // 4. Add notes if any hits found
            if (hasAmlHit || hasPepMatch || hasSanctionsMatch) {
                result.setNotes(buildScreeningNotes(hasAmlHit, hasPepMatch, hasSanctionsMatch));
            } else {
                result.setNotes("No adverse findings. Clear for approval.");
            }

            log.info("Screening complete. Risk score: {}, Hits: AML={}, PEP={}, Sanctions={}",
                    riskScore, hasAmlHit, hasPepMatch, hasSanctionsMatch);

        } catch (Exception e) {
            log.error("Error during AML/PEP screening: {}", e.getMessage(), e);
            result.setNotes("Screening error: " + e.getMessage());
            result.setRiskScore(100); // Max risk on error
        }

        return result;
    }

    /**
     * Check for AML (Anti-Money Laundering) watchlist matches
     * In production, this queries OpenSanctions database
     */
    private boolean checkAmlMatch(String firstName, String lastName) {
        // Mock implementation - replace with actual API call
        // For demo: flag specific test names
        String fullName = (firstName + " " + lastName).toLowerCase();

        // Test cases for demonstration
        return fullName.contains("crime") || fullName.contains("fraud");
    }

    /**
     * Check for PEP (Politically Exposed Person) matches
     */
    private boolean checkPepMatch(String firstName, String lastName) {
        // Mock implementation
        String fullName = (firstName + " " + lastName).toLowerCase();

        // Test cases for demonstration
        return fullName.contains("president") ||
                fullName.contains("minister") ||
                fullName.contains("senator");
    }

    /**
     * Check for sanctions list matches
     */
    private boolean checkSanctionsMatch(String firstName, String lastName) {
        // Mock implementation
        String fullName = (firstName + " " + lastName).toLowerCase();

        // Test cases for demonstration
        return fullName.contains("sanction") || fullName.contains("embargo");
    }

    /**
     * Calculate risk score based on findings
     */
    private int calculateRiskScore(
            boolean hasAmlHit,
            boolean hasPepMatch,
            boolean hasSanctionsMatch,
            String countryCode) {

        int score = 0;

        // Base score from hits
        if (hasSanctionsMatch)
            score += 50; // Highest risk
        if (hasAmlHit)
            score += 30;
        if (hasPepMatch)
            score += 20;

        // Country risk adjustment
        if (SANCTIONED_COUNTRIES.contains(countryCode)) {
            score += 30;
        }

        // Cap at 100
        return Math.min(score, 100);
    }

    /**
     * Build detailed notes about screening findings
     */
    private String buildScreeningNotes(
            boolean hasAmlHit,
            boolean hasPepMatch,
            boolean hasSanctionsMatch) {

        StringBuilder notes = new StringBuilder("Screening findings: ");

        if (hasSanctionsMatch) {
            notes.append("SANCTIONS MATCH - Person appears on sanctions list. ");
        }
        if (hasAmlHit) {
            notes.append("AML HIT - Potential match in financial crime database. ");
        }
        if (hasPepMatch) {
            notes.append("PEP MATCH - Identified as Politically Exposed Person. ");
        }

        notes.append("Manual review required before approval.");

        return notes.toString();
    }

    /**
     * Result object for screening operations
     */
    public static class ScreeningResult {
        private boolean amlHit;
        private boolean pepMatch;
        private boolean sanctionsMatch;
        private int riskScore;
        private String notes;

        public boolean hasAmlHit() {
            return amlHit;
        }

        public void setAmlHit(boolean amlHit) {
            this.amlHit = amlHit;
        }

        public boolean hasPepMatch() {
            return pepMatch;
        }

        public void setPepMatch(boolean pepMatch) {
            this.pepMatch = pepMatch;
        }

        public boolean hasSanctionsMatch() {
            return sanctionsMatch;
        }

        public void setSanctionsMatch(boolean sanctionsMatch) {
            this.sanctionsMatch = sanctionsMatch;
        }

        public int getRiskScore() {
            return riskScore;
        }

        public void setRiskScore(int riskScore) {
            this.riskScore = riskScore;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public boolean isClean() {
            return !amlHit && !pepMatch && !sanctionsMatch;
        }
    }
}