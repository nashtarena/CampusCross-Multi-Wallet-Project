package com.campuscross.wallet.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "risk_scores")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskScore {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long transactionId;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal riskScore;
    
    @Column(nullable = false, length = 20)
    private String riskLevel;
    
    @Column(columnDefinition = "TEXT")
    private String riskFactors;
    
    @Column(nullable = false, length = 20)
    private String status;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime reviewedAt;
    
    @Column
    private Long reviewedBy;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
    }
}