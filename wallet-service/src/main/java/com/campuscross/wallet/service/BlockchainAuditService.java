package com.campuscross.wallet.service;

import com.campuscross.wallet.entity.BlockchainAuditChain;
import com.campuscross.wallet.repository.BlockchainAuditChainRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlockchainAuditService {
    
    private final BlockchainAuditChainRepository auditChainRepository;
    private final HashingService hashingService;
    
    // Event types
    public static final String EVENT_WALLET_CREATED = "WALLET_CREATED";
    public static final String EVENT_TRANSACTION_CREATED = "TRANSACTION_CREATED";
    public static final String EVENT_TRANSACTION_COMPLETED = "TRANSACTION_COMPLETED";
    public static final String EVENT_BALANCE_UPDATED = "BALANCE_UPDATED";
    public static final String EVENT_CONVERSION_EXECUTED = "CONVERSION_EXECUTED";
    public static final String EVENT_DISBURSEMENT_CREATED = "DISBURSEMENT_CREATED";
    public static final String EVENT_DISBURSEMENT_PROCESSED = "DISBURSEMENT_PROCESSED";
    public static final String EVENT_RISK_SCORE_CREATED = "RISK_SCORE_CREATED";
    public static final String EVENT_RISK_REVIEW_APPROVED = "RISK_REVIEW_APPROVED";
    public static final String EVENT_RISK_REVIEW_REJECTED = "RISK_REVIEW_REJECTED";
    
    /**
     * Create a new audit block in the blockchain
     */
    @Transactional
    public BlockchainAuditChain createAuditBlock(
            String eventType,
            String entityType,
            Long entityId,
            Long userId,
            Map<String, Object> eventData
    ) {
        log.info("Creating audit block: eventType={}, entityType={}, entityId={}", 
                eventType, entityType, entityId);
        
        try {
            // Get the latest block to link to
            BlockchainAuditChain previousBlock = auditChainRepository.findLatestBlock()
                    .orElseThrow(() -> new RuntimeException("Genesis block not found! Database may not be initialized."));
            
            // Calculate next block number
            Long nextBlockNumber = previousBlock.getBlockNumber() + 1;
            
            // Generate unique audit ID
            String auditId = generateAuditId(eventType, nextBlockNumber);
            
            // Prepare timestamp
            LocalDateTime timestamp = LocalDateTime.now();
            
            // Calculate merkle root from event data
            String merkleRoot = hashingService.calculateMerkleRoot(eventData);
            
            // Calculate block hash (links to previous block)
            Long nonce = 0L;
            String currentHash = hashingService.calculateBlockHash(
                    nextBlockNumber,
                    previousBlock.getCurrentHash(),
                    eventData,
                    timestamp,
                    nonce
            );
            
            // Create metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("previousBlockNumber", previousBlock.getBlockNumber());
            metadata.put("chainLength", nextBlockNumber);
            metadata.put("timestamp", timestamp.toString());
            
            // Build the audit block
            BlockchainAuditChain auditBlock = BlockchainAuditChain.builder()
                    .blockNumber(nextBlockNumber)
                    .auditId(auditId)
                    .eventType(eventType)
                    .entityType(entityType)
                    .entityId(entityId)
                    .userId(userId)
                    .currentHash(currentHash)
                    .previousHash(previousBlock.getCurrentHash())
                    .merkleRoot(merkleRoot)
                    .nonce(nonce)
                    .eventData(eventData)
                    .metadata(metadata)
                    .createdAt(timestamp)
                    .isVerified(false)
                    .tamperDetected(false)
                    .build();
            
            // Save to database
            auditBlock = auditChainRepository.save(auditBlock);
            
            log.info("Audit block created successfully: blockNumber={}, hash={}", 
                    nextBlockNumber, currentHash);
            
            return auditBlock;
            
        } catch (Exception e) {
            log.error("Failed to create audit block", e);
            throw new RuntimeException("Failed to create audit block: " + e.getMessage(), e);
        }
    }
    
    /**
     * Create audit block with proof-of-work (optional, for enhanced security)
     */
    @Transactional
    public BlockchainAuditChain createAuditBlockWithProofOfWork(
            String eventType,
            String entityType,
            Long entityId,
            Long userId,
            Map<String, Object> eventData,
            int difficulty
    ) {
        log.info("Creating audit block with PoW: difficulty={}", difficulty);
        
        try {
            BlockchainAuditChain previousBlock = auditChainRepository.findLatestBlock()
                    .orElseThrow(() -> new RuntimeException("Genesis block not found!"));
            
            Long nextBlockNumber = previousBlock.getBlockNumber() + 1;
            String auditId = generateAuditId(eventType, nextBlockNumber);
            LocalDateTime timestamp = LocalDateTime.now();
            String merkleRoot = hashingService.calculateMerkleRoot(eventData);
            
            // Calculate hash with proof-of-work
            HashingService.NonceResult powResult = hashingService.calculateHashWithProofOfWork(
                    nextBlockNumber,
                    previousBlock.getCurrentHash(),
                    eventData,
                    timestamp,
                    difficulty
            );
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("previousBlockNumber", previousBlock.getBlockNumber());
            metadata.put("chainLength", nextBlockNumber);
            metadata.put("timestamp", timestamp.toString());
            metadata.put("proofOfWork", true);
            metadata.put("difficulty", difficulty);
            metadata.put("miningTimeMs", powResult.timeTakenMs);
            
            BlockchainAuditChain auditBlock = BlockchainAuditChain.builder()
                    .blockNumber(nextBlockNumber)
                    .auditId(auditId)
                    .eventType(eventType)
                    .entityType(entityType)
                    .entityId(entityId)
                    .userId(userId)
                    .currentHash(powResult.hash)
                    .previousHash(previousBlock.getCurrentHash())
                    .merkleRoot(merkleRoot)
                    .nonce(powResult.nonce)
                    .eventData(eventData)
                    .metadata(metadata)
                    .createdAt(timestamp)
                    .isVerified(false)
                    .tamperDetected(false)
                    .build();
            
            auditBlock = auditChainRepository.save(auditBlock);
            
            log.info("Audit block created with PoW: blockNumber={}, nonce={}, time={}ms", 
                    nextBlockNumber, powResult.nonce, powResult.timeTakenMs);
            
            return auditBlock;
            
        } catch (Exception e) {
            log.error("Failed to create audit block with PoW", e);
            throw new RuntimeException("Failed to create audit block with PoW: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get audit trail for a specific entity
     */
    @Transactional(readOnly = true)
    public java.util.List<BlockchainAuditChain> getAuditTrail(String entityType, Long entityId) {
        log.info("Fetching audit trail: entityType={}, entityId={}", entityType, entityId);
        return auditChainRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }
    
    /**
     * Get audit trail for a user
     */
    @Transactional(readOnly = true)
    public java.util.List<BlockchainAuditChain> getUserAuditTrail(Long userId) {
        log.info("Fetching user audit trail: userId={}", userId);
        return auditChainRepository.findByUserId(userId);
    }
    
    /**
     * Get latest block in the chain
     */
    @Transactional(readOnly = true)
    public BlockchainAuditChain getLatestBlock() {
        return auditChainRepository.findLatestBlock()
                .orElseThrow(() -> new RuntimeException("No blocks found in chain"));
    }
    
    /**
     * Get chain statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getChainStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        Long maxBlockNumber = auditChainRepository.findMaxBlockNumber().orElse(0L);
        Long totalBlocks = auditChainRepository.count();
        Long verifiedBlocks = auditChainRepository.countVerifiedBlocks();
        Long tamperedBlocks = auditChainRepository.countTamperedBlocks();
        
        stats.put("totalBlocks", totalBlocks);
        stats.put("latestBlockNumber", maxBlockNumber);
        stats.put("verifiedBlocks", verifiedBlocks);
        stats.put("tamperedBlocks", tamperedBlocks);
        stats.put("chainHealth", tamperedBlocks == 0 ? "HEALTHY" : "COMPROMISED");
        
        BlockchainAuditChain latestBlock = auditChainRepository.findLatestBlock().orElse(null);
        if (latestBlock != null) {
            stats.put("latestBlockHash", latestBlock.getCurrentHash());
            stats.put("latestBlockTime", latestBlock.getCreatedAt());
        }
        
        log.info("Chain statistics: {}", stats);
        return stats;
    }
    
    /**
     * Generate unique audit ID
     */
    private String generateAuditId(String eventType, Long blockNumber) {
        return String.format("%s-%d-%s", 
                eventType, 
                blockNumber, 
                UUID.randomUUID().toString().substring(0, 8));
    }
    
    /**
     * Quick helper methods for common audit events
     */
    
    public BlockchainAuditChain auditWalletCreation(Long walletId, Long userId, Map<String, Object> details) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("walletId", walletId);
        eventData.put("action", "WALLET_CREATED");
        eventData.putAll(details);
        
        return createAuditBlock(EVENT_WALLET_CREATED, "WALLET", walletId, userId, eventData);
    }
    
    public BlockchainAuditChain auditTransaction(
            Long transactionId, 
            Long userId, 
            String transactionType,
            String amount,
            String currency,
            String status
    ) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("transactionId", transactionId);
        eventData.put("transactionType", transactionType);
        eventData.put("amount", amount);
        eventData.put("currency", currency);
        eventData.put("status", status);
        eventData.put("timestamp", LocalDateTime.now().toString());
        
        return createAuditBlock(EVENT_TRANSACTION_CREATED, "TRANSACTION", transactionId, userId, eventData);
    }
    
    public BlockchainAuditChain auditBalanceUpdate(
            Long accountId,
            Long userId,
            String currency,
            String oldBalance,
            String newBalance,
            String reason
    ) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("accountId", accountId);
        eventData.put("currency", currency);
        eventData.put("oldBalance", oldBalance);
        eventData.put("newBalance", newBalance);
        eventData.put("reason", reason);
        eventData.put("timestamp", LocalDateTime.now().toString());
        
        return createAuditBlock(EVENT_BALANCE_UPDATED, "CURRENCY_ACCOUNT", accountId, userId, eventData);
    }
    
    public BlockchainAuditChain auditConversion(
            Long transactionId,
            Long userId,
            String fromCurrency,
            String toCurrency,
            String amount,
            String convertedAmount,
            String exchangeRate
    ) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("transactionId", transactionId);
        eventData.put("fromCurrency", fromCurrency);
        eventData.put("toCurrency", toCurrency);
        eventData.put("amount", amount);
        eventData.put("convertedAmount", convertedAmount);
        eventData.put("exchangeRate", exchangeRate);
        eventData.put("timestamp", LocalDateTime.now().toString());
        
        return createAuditBlock(EVENT_CONVERSION_EXECUTED, "TRANSACTION", transactionId, userId, eventData);
    }
    
    public BlockchainAuditChain auditDisbursement(
            Long batchId,
            Long adminId,
            String status,
            Integer totalCount,
            String totalAmount,
            String currency
    ) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("batchId", batchId);
        eventData.put("status", status);
        eventData.put("totalCount", totalCount);
        eventData.put("totalAmount", totalAmount);
        eventData.put("currency", currency);
        eventData.put("timestamp", LocalDateTime.now().toString());
        
        return createAuditBlock(EVENT_DISBURSEMENT_CREATED, "DISBURSEMENT_BATCH", batchId, adminId, eventData);
    }
    
    public BlockchainAuditChain auditRiskScore(
            Long riskScoreId,
            Long transactionId,
            Long userId,
            String riskLevel,
            String riskScore,
            String factors
    ) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("riskScoreId", riskScoreId);
        eventData.put("transactionId", transactionId);
        eventData.put("riskLevel", riskLevel);
        eventData.put("riskScore", riskScore);
        eventData.put("factors", factors);
        eventData.put("timestamp", LocalDateTime.now().toString());
        
        return createAuditBlock(EVENT_RISK_SCORE_CREATED, "RISK_SCORE", riskScoreId, userId, eventData);
    }
}