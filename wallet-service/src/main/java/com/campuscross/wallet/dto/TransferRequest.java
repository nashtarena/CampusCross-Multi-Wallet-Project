package com.campuscross.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {
    private Long fromUserId;
    private String toStudentId;
    private String toMobileNumber;
    private BigDecimal amount;
    private String currencyCode;
    private String description;
    private String idempotencyKey;
}