package com.campuscross.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisbursementRequest {
    
    private Long adminUserId; // Who is creating the disbursement
    private String currency;
    private String description;
    private List<DisbursementRecipient> recipients;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DisbursementRecipient {
        private String studentId;
        private BigDecimal amount;
    }
}