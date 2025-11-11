package com.campuscross.wallet.service;

import com.campuscross.wallet.entity.BlockchainAuditChain;
import com.campuscross.wallet.entity.BlockchainVerificationLog;
import com.campuscross.wallet.repository.BlockchainAuditChainRepository;
import com.campuscross.wallet.repository.BlockchainVerificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlockchainVerificationService {
    
    private final BlockchainAuditChainRepository auditChainRepository;
    private final BlockchainVerificationLogRepository verificationLogRepository;
    private final HashingService hashingService;
    
    /**
     * Verify the entire blockchain from genesis to latest block
     */
    @Transactional
    public BlockchainVerificationLog verifyCompleteChain() {
        log.info("Starting complete blockchain verification");
        
        long startTime = System.currentTimeMillis();
        
        Long maxBlockNumber = auditChainRepository.findMaxBlockNumber().orElse(0L);
        
        BlockchainVerificationLog result = verifyChainRange(0L, maxBlockNumber);
        
        long duration = System.currentTimeMillis() - startTime;
        result.setVerificationTimeMs(duration);
        
        log.info("Complete chain verification finished in {}ms. Status: {}", duration, result.getStatus());
        
        return verificationLogRepository.save(result);
    }
    
    /**
     * Verify a specific range of blocks
     */
    @Transactional
    public BlockchainVerificationLog verifyChainRange(Long startBlock, Long endBlock) {
        log.info("Verifying blockchain from block {} to {}", startBlock, endBlock);
        
        long startTime = System.currentTimeMillis();
        
        List<BlockchainAuditChain> blocks = auditChainRepository.findBlocksInRange(startBlock, endBlock);
        
        long verifiedCount = 0;
        long tamperedCount = 0;
        Map<String, Object> details = new HashMap<>();
        
        for (int i = 0; i < blocks.size(); i++) {
            BlockchainAuditChain currentBlock = blocks.get(i);
            
            // Skip genesis block (block 0)
            if (currentBlock.getBlockNumber() == 0) {
                verifiedCount++;
                continue;
            }
            
            // Find previous block
            BlockchainAuditChain previousBlock = null;
            if (i > 0) {
                previousBlock = blocks.get(i - 1);
            } else {
                // If not in current range, fetch from DB
                previousBlock = auditChainRepository.findByBlockNumber(currentBlock.getBlockNumber() - 1)
                        .orElse(null);
            }
            
            if (previousBlock == null) {
                log.error("Previous block not found for block {}", currentBlock.getBlockNumber());
                tamperedCount++;
                continue;
            }
            
            // Verify hash linkage
            if (!currentBlock.getPreviousHash().equals(previousBlock.getCurrentHash())) {
                log.error("Hash chain broken at block {}. Expected previous hash: {}, Actual: {}",
                        currentBlock.getBlockNumber(),
                        previousBlock.getCurrentHash(),
                        currentBlock.getPreviousHash());
                
                currentBlock.setTamperDetected(true);
                auditChainRepository.save(currentBlock);
                tamperedCount++;
                continue;
            }
            
            // Verify current block's hash
            boolean isHashValid = hashingService.verifyBlockHash(
                    currentBlock.getCurrentHash(),
                    currentBlock.getBlockNumber(),
                    currentBlock.getPreviousHash(),
                    currentBlock.getEventData(),
                    currentBlock.getCreatedAt(),
                    currentBlock.getNonce()
            );
            
            if (!isHashValid) {
                log.error("Invalid hash for block {}. Hash verification failed", currentBlock.getBlockNumber());
                currentBlock.setTamperDetected(true);
                auditChainRepository.save(currentBlock);
                tamperedCount++;
                continue;
            }
            
            // Mark as verified
            currentBlock.setIsVerified(true);
            currentBlock.setVerifiedAt(java.time.LocalDateTime.now());
            auditChainRepository.save(currentBlock);
            
            verifiedCount++;
        }
        
        long duration = System.currentTimeMillis() - startTime;
        
        String status = tamperedCount == 0 ? "SUCCESS" : "TAMPERED";
        
        details.put("blocksVerified", verifiedCount);
        details.put("blocksWithTampering", tamperedCount);
        details.put("verificationDuration", duration + "ms");
        
        BlockchainVerificationLog log = BlockchainVerificationLog.builder()
                .verificationId(UUID.randomUUID().toString())
                .startBlock(startBlock)
                .endBlock(endBlock)
                .totalBlocks((long) blocks.size())
                .verifiedBlocks(verifiedCount)
                .tamperedBlocks(tamperedCount)
                .status(status)
                .verificationTimeMs(duration)
                .details(details)
                .build();
        
        return verificationLogRepository.save(log);
    }
    
    /**
     * Verify a single block
     */
    @Transactional
    public boolean verifySingleBlock(Long blockNumber) {
        log.info("Verifying single block: {}", blockNumber);
        
        BlockchainAuditChain block = auditChainRepository.findByBlockNumber(blockNumber)
                .orElseThrow(() -> new RuntimeException("Block not found: " + blockNumber));
        
        // Genesis block is always valid
        if (blockNumber == 0) {
            return true;
        }
        
        // Get previous block
        BlockchainAuditChain previousBlock = auditChainRepository.findByBlockNumber(blockNumber - 1)
                .orElseThrow(() -> new RuntimeException("Previous block not found"));
        
        // Check hash linkage
        if (!block.getPreviousHash().equals(previousBlock.getCurrentHash())) {
            log.error("Hash chain broken at block {}", blockNumber);
            return false;
        }
        
        // Verify current block hash
        boolean isValid = hashingService.verifyBlockHash(
                block.getCurrentHash(),
                block.getBlockNumber(),
                block.getPreviousHash(),
                block.getEventData(),
                block.getCreatedAt(),
                block.getNonce()
        );
        
        if (isValid) {
            block.setIsVerified(true);
            block.setVerifiedAt(java.time.LocalDateTime.now());
            auditChainRepository.save(block);
        }
        
        return isValid;
    }
    
    /**
     * Get verification history
     */
    @Transactional(readOnly = true)
    public List<BlockchainVerificationLog> getVerificationHistory() {
        return verificationLogRepository.findTop10ByOrderByCreatedAtDesc();
    }
    
    /**
     * Get tampered blocks
     */
    @Transactional(readOnly = true)
    public List<BlockchainAuditChain> getTamperedBlocks() {
        return auditChainRepository.findAll().stream()
                .filter(BlockchainAuditChain::getTamperDetected)
                .toList();
    }
}