package com.campuscross.wallet.controller;

import com.campuscross.wallet.entity.RiskScore;
import com.campuscross.wallet.service.FraudDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fraud")
@RequiredArgsConstructor
@Slf4j
public class FraudController {
    
    private final FraudDetectionService fraudDetectionService;
    
    /**
     * Get pending reviews (for admin dashboard)
     * GET /api/v1/fraud/reviews/pending
     */
    @GetMapping("/reviews/pending")
    public ResponseEntity<List<RiskScore>> getPendingReviews() {
        log.info("Fetching pending fraud reviews");
        List<RiskScore> pendingReviews = fraudDetectionService.getPendingReviews();
        return ResponseEntity.ok(pendingReviews);
    }
    
    /**
     * Approve a flagged transaction
     * POST /api/v1/fraud/reviews/{riskScoreId}/approve
     */
    @PostMapping("/reviews/{riskScoreId}/approve")
    public ResponseEntity<Void> approveTransaction(
            @PathVariable Long riskScoreId,
            @RequestParam Long reviewerId) {
        log.info("Approving transaction with risk score ID: {} by reviewer: {}", riskScoreId, reviewerId);
        fraudDetectionService.approveTransaction(riskScoreId, reviewerId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Reject a flagged transaction
     * POST /api/v1/fraud/reviews/{riskScoreId}/reject
     */
    @PostMapping("/reviews/{riskScoreId}/reject")
    public ResponseEntity<Void> rejectTransaction(
            @PathVariable Long riskScoreId,
            @RequestParam Long reviewerId) {
        log.info("Rejecting transaction with risk score ID: {} by reviewer: {}", riskScoreId, reviewerId);
        fraudDetectionService.rejectTransaction(riskScoreId, reviewerId);
        return ResponseEntity.ok().build();
    }
}