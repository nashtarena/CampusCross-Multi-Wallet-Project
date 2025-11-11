package com.campuscross.wallet.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.campuscross.wallet.entity.BlockchainVerificationLog;

@Repository
public interface BlockchainVerificationLogRepository extends JpaRepository<BlockchainVerificationLog, Long> {
    
    Optional<BlockchainVerificationLog> findByVerificationId(String verificationId);
    
    List<BlockchainVerificationLog> findByStatusOrderByCreatedAtDesc(String status);
    
    List<BlockchainVerificationLog> findTop10ByOrderByCreatedAtDesc();
}