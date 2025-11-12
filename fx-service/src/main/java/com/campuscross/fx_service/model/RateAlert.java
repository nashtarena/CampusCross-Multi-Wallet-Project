package com.campuscross.fx_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "rate_alerts")
public class RateAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String currencyPair;

    @Column(precision = 19, scale = 8)
    private BigDecimal thresholdValue;

    @Enumerated(EnumType.STRING)
    private Direction direction;

    @Enumerated(EnumType.STRING)
    private AlertStatus status = AlertStatus.ACTIVE;

    private Instant lastTriggeredAt;

    // --- ENUMS ---

    public enum Direction {
        ABOVE,
        BELOW
    }

    public enum AlertStatus {
        ACTIVE,
        TRIGGERED,
        INACTIVE
    }

    // --- CONSTRUCTORS ---

    public RateAlert() {
    }

    // --- GETTERS AND SETTERS ---
    // ALL the methods required by your RateAlertService are defined below.

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        // Required for Line 80 and 83 in your error log
        return userId;
    }

    public void setUserId(Long userId) {
        // Required for Line 83 in your error log
        this.userId = userId;
    }

    public String getCurrencyPair() {
        // Required for Line 78 in your error log
        return currencyPair;
    }

    public void setCurrencyPair(String currencyPair) {
        this.currencyPair = currencyPair;
    }

    public BigDecimal getThresholdValue() {
        // Required for Line 56 and 79 in your error log
        return thresholdValue;
    }

    public void setThresholdValue(BigDecimal thresholdValue) {
        this.thresholdValue = thresholdValue;
    }

    public Direction getDirection() {
        // Required for Line 58, 61, and 79 in your error log
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public AlertStatus getStatus() {
        // Required for Line 91 in your error log
        return status;
    }

    public void setStatus(AlertStatus status) {
        // Required for Line 64 and 92 in your error log
        this.status = status;
    }

    public Instant getLastTriggeredAt() {
        // Required for Line 50 and 51 in your error log
        return lastTriggeredAt;
    }

    public void setLastTriggeredAt(Instant lastTriggeredAt) {
        // Required for Line 72 in your error log
        this.lastTriggeredAt = lastTriggeredAt;
    }
}