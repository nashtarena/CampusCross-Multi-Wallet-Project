package com.campuscross.wallet.controller;

import com.campuscross.wallet.entity.BlockchainAuditChain;
import com.campuscross.wallet.entity.BlockchainVerificationLog;
import com.campuscross.wallet.service.BlockchainAuditService;
import com.campuscross.wallet.service.BlockchainVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/blockchain")
@RequiredArgsConstructor
@Slf4j
public class BlockchainController {
    
    private final BlockchainAuditService auditService;
    private final BlockchainVerificationService verificationService;
    
    /**
     * Get blockchain statistics
     * GET /api/v1/blockchain/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getChainStatistics() {
        log.info("Fetching blockchain statistics");
        Map<String, Object> stats = auditService.getChainStatistics();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Get latest block
     * GET /api/v1/blockchain/latest
     */
    @GetMapping("/latest")
    public ResponseEntity<BlockchainAuditChain> getLatestBlock() {
        log.info("Fetching latest block");
        BlockchainAuditChain latest = auditService.getLatestBlock();
        return ResponseEntity.ok(latest);
    }
    
    /**
     * Get audit trail for entity
     * GET /api/v1/blockchain/audit/{entityType}/{entityId}
     */
    @GetMapping("/audit/{entityType}/{entityId}")
    public ResponseEntity<List<BlockchainAuditChain>> getAuditTrail(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        log.info("Fetching audit trail: {}:{}", entityType, entityId);
        List<BlockchainAuditChain> trail = auditService.getAuditTrail(entityType, entityId);
        return ResponseEntity.ok(trail);
    }
    
    /**
     * Get user's audit trail
     * GET /api/v1/blockchain/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BlockchainAuditChain>> getUserAuditTrail(@PathVariable Long userId) {
        log.info("Fetching user audit trail: {}", userId);
        List<BlockchainAuditChain> trail = auditService.getUserAuditTrail(userId);
        return ResponseEntity.ok(trail);
    }
    
    /**
     * Verify entire blockchain
     * POST /api/v1/blockchain/verify
     */
    @PostMapping("/verify")
    public ResponseEntity<BlockchainVerificationLog> verifyBlockchain() {
        log.info("Starting blockchain verification");
        BlockchainVerificationLog result = verificationService.verifyCompleteChain();
        return ResponseEntity.ok(result);
    }
    
    /**
     * Verify specific block
     * POST /api/v1/blockchain/verify/{blockNumber}
     */
    @PostMapping("/verify/{blockNumber}")
    public ResponseEntity<Map<String, Object>> verifySingleBlock(@PathVariable Long blockNumber) {
        log.info("Verifying block: {}", blockNumber);
        boolean isValid = verificationService.verifySingleBlock(blockNumber);
        
        Map<String, Object> response = Map.of(
                "blockNumber", blockNumber,
                "isValid", isValid,
                "status", isValid ? "VERIFIED" : "INVALID"
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get verification history
     * GET /api/v1/blockchain/verifications
     */
    @GetMapping("/verifications")
    public ResponseEntity<List<BlockchainVerificationLog>> getVerificationHistory() {
        log.info("Fetching verification history");
        List<BlockchainVerificationLog> history = verificationService.getVerificationHistory();
        return ResponseEntity.ok(history);
    }
    
    /**
     * Get tampered blocks
     * GET /api/v1/blockchain/tampered
     */
    @GetMapping("/tampered")
    public ResponseEntity<List<BlockchainAuditChain>> getTamperedBlocks() {
        log.info("Fetching tampered blocks");
        List<BlockchainAuditChain> tamperedBlocks = verificationService.getTamperedBlocks();
        return ResponseEntity.ok(tamperedBlocks);
    }
}