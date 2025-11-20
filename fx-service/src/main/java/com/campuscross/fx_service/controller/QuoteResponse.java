package com.campuscross.fx_service.controller;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Simple DTO returned by FxController: { from, to, rate, timestamp }
 */
public class QuoteResponse {
    private String from;
    private String to;
    private BigDecimal rate;
    private Instant timestamp;

    public QuoteResponse() {
    }

    public QuoteResponse(String from, String to, BigDecimal rate) {
        this.from = from;
        this.to = to;
        this.rate = rate;
        this.timestamp = Instant.now();
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}