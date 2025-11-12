package com.campuscross.fx_service.dto;

import java.math.BigDecimal;
import java.time.Instant;

// This DTO must match the structure of the JSON published to the Kafka topic
public class FxRateDto {
    private String currencyPair;
    private BigDecimal rateValue;
    private Instant timestamp;

    // --- CONSTRUCTORS ---

    public FxRateDto() {
    }

    // --- GETTERS AND SETTERS ---

    public String getCurrencyPair() {
        return currencyPair;
    }

    public void setCurrencyPair(String currencyPair) {
        this.currencyPair = currencyPair;
    }

    public BigDecimal getRateValue() {
        return rateValue;
    }

    public void setRateValue(BigDecimal rateValue) {
        this.rateValue = rateValue;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}