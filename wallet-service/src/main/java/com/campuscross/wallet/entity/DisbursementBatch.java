package com.campuscross.wallet.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "disbursement_batches")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisbursementBatch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String batchId;
    
    @Column(nullable = false)
    private Long createdBy;
    
    @Column(nullable = false)
    private Integer totalCount;
    
    @Column(nullable = false)
    private Integer successCount = 0;
    
    @Column(nullable = false)
    private Integer failedCount = 0;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(nullable = false, length = 3)
    private String currency;
    
    @Column(nullable = false, length = 20)
    private String status;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime completedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
        if (successCount == null) {
            successCount = 0;
        }
        if (failedCount == null) {
            failedCount = 0;
        }
    }
}