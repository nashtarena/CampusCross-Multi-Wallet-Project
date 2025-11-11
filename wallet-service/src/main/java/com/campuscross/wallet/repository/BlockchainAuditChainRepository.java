package com.campuscross.wallet.repository;

import com.campuscross.wallet.entity.BlockchainAuditChain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlockchainAuditChainRepository extends JpaRepository<BlockchainAuditChain, Long> {
    
    Optional<BlockchainAuditChain> findByBlockNumber(Long blockNumber);
    
    Optional<BlockchainAuditChain> findByAuditId(String auditId);
    
    @Query("SELECT MAX(b.blockNumber) FROM BlockchainAuditChain b")
    Optional<Long> findMaxBlockNumber();
    
    @Query("SELECT b FROM BlockchainAuditChain b WHERE b.blockNumber = (SELECT MAX(b2.blockNumber) FROM BlockchainAuditChain b2)")
    Optional<BlockchainAuditChain> findLatestBlock();
    
    List<BlockchainAuditChain> findByEntityTypeAndEntityId(String entityType, Long entityId);
    
    List<BlockchainAuditChain> findByUserId(Long userId);
    
    List<BlockchainAuditChain> findByEventType(String eventType);
    
    @Query("SELECT b FROM BlockchainAuditChain b WHERE b.blockNumber BETWEEN ?1 AND ?2 ORDER BY b.blockNumber")
    List<BlockchainAuditChain> findBlocksInRange(Long startBlock, Long endBlock);
    
    @Query("SELECT COUNT(b) FROM BlockchainAuditChain b WHERE b.isVerified = true")
    Long countVerifiedBlocks();
    
    @Query("SELECT COUNT(b) FROM BlockchainAuditChain b WHERE b.tamperDetected = true")
    Long countTamperedBlocks();
}