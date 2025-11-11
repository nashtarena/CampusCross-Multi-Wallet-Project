package com.campuscross.wallet.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "blockchain_verification_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockchainVerificationLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String verificationId;
    
    @Column(nullable = false)
    private Long startBlock;
    
    @Column(nullable = false)
    private Long endBlock;
    
    @Column(nullable = false)
    private Long totalBlocks;
    
    @Column(nullable = false)
    private Long verifiedBlocks;
    
    @Column
    private Long tamperedBlocks = 0L;
    
    @Column(nullable = false, length = 20)
    private String status; // SUCCESS, FAILED, TAMPERED
    
    @Column
    private Long verificationTimeMs;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> details;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (tamperedBlocks == null) {
            tamperedBlocks = 0L;
        }
    }
}