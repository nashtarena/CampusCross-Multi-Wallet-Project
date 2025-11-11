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
@Table(name = "blockchain_audit_chain")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockchainAuditChain {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private Long blockNumber;
    
    @Column(nullable = false, unique = true, length = 100)
    private String auditId;
    
    @Column(nullable = false, length = 50)
    private String eventType;
    
    @Column(nullable = false, length = 50)
    private String entityType;
    
    @Column(nullable = false)
    private Long entityId;
    
    @Column(name = "user_id")
    private Long userId;
    
    // Blockchain fields
    @Column(nullable = false, unique = true, length = 64)
    private String currentHash;
    
    @Column(nullable = false, length = 64)
    private String previousHash;
    
    @Column(length = 64)
    private String merkleRoot;
    
    @Column
    private Long nonce = 0L;
    
    // Data stored as JSONB
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> eventData;
    
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;
    
    // Timestamps
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime verifiedAt;
    
    // Integrity flags
    @Column
    private Boolean isVerified = false;
    
    @Column
    private Boolean tamperDetected = false;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (nonce == null) {
            nonce = 0L;
        }
        if (isVerified == null) {
            isVerified = false;
        }
        if (tamperDetected == null) {
            tamperDetected = false;
        }
    }
}