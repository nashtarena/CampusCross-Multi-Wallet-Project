package com.campuscross.fx_service.controller; // Use your actual package name (with or without underscore)

import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class QuoteResponse {

    private final String fromCurrency;
    private final String toCurrency;
    private final BigDecimal rate;
    private final Instant timestamp;

    public QuoteResponse(String from, String to, BigDecimal rate) {
        this.fromCurrency = from;
        this.toCurrency = to;
        this.rate = rate;
        this.timestamp = Instant.now();
    }
}