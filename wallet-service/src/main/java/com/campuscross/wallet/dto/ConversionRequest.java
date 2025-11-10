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
public class ConversionRequest {
    private Long userId;
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal amount;
    private BigDecimal exchangeRate;
    private String idempotencyKey;
}