package com.campuscross.fx_service.service;

import com.campuscross.fx_service.kafka.KafkaProducerService;
import com.campuscross.fx_service.model.RateAlert;
import com.campuscross.fx_service.repository.RateAlertRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class RateAlertService {

    // Define a cooldown period (e.g., 60 minutes) to prevent users from being
    // spammed
    private static final long COOLDOWN_MINUTES = 60;
    private static final String NOTIFICATION_TOPIC = "notification.fx.alerts"; // Topic for Developer C

    private final RateAlertRepository repository;
    private final KafkaProducerService kafkaProducer;

    public RateAlertService(RateAlertRepository repository, KafkaProducerService kafkaProducer) {
        this.repository = repository;
        this.kafkaProducer = kafkaProducer;
    }

    // --- CORE MONITORING LOGIC (Called by Kafka Consumer) ---

    @Transactional // Ensure the database update (cooldown) and fetch are managed atomically
    public void checkAndTriggerAlerts(String currencyPair, BigDecimal currentRate) {
        // 1. Fetch all ACTIVE alerts for the currency pair
        List<RateAlert> activeAlerts = repository.findAllByCurrencyPairAndStatus(
                currencyPair, RateAlert.AlertStatus.ACTIVE);

        for (RateAlert alert : activeAlerts) {
            // 2. Check the condition (threshold and cooldown)
            if (isTriggerConditionMet(alert, currentRate)) {
                // 3. Trigger alert and update status in DB
                triggerAlert(alert, currentRate);
            }
        }
    }

    private boolean isTriggerConditionMet(RateAlert alert, BigDecimal currentRate) {
        // Check if the alert is currently in a cooldown state
        if (alert.getLastTriggeredAt() != null &&
                alert.getLastTriggeredAt().isAfter(Instant.now().minus(COOLDOWN_MINUTES, ChronoUnit.MINUTES))) {
            return false; // Still in cooldown
        }

        // Compare the current rate with the alert's threshold
        int comparison = currentRate.compareTo(alert.getThresholdValue());

        if (alert.getDirection() == RateAlert.Direction.ABOVE) {
            // Condition met if currentRate is strictly GREATER than threshold
            return comparison > 0;
        } else if (alert.getDirection() == RateAlert.Direction.BELOW) {
            // Condition met if currentRate is strictly LESS than threshold
            return comparison < 0;
        }
        return false;
    }

    private void triggerAlert(RateAlert alert, BigDecimal currentRate) {
        // 1. Update Alert Status in the database to prevent immediate re-triggering
        alert.setStatus(RateAlert.AlertStatus.TRIGGERED); // You might switch this to ACTIVE later if it should
                                                          // immediately reset
        alert.setLastTriggeredAt(Instant.now());
        repository.save(alert);

        // 2. Publish to Kafka for Notification Service (Developer C)
        String alertMessage = String.format(
                "FX Alert triggered: %s rate %.4f is now %s %.4f. UserID: %d",
                alert.getCurrencyPair(), currentRate,
                alert.getDirection(), alert.getThresholdValue(),
                alert.getUserId());

        // Use the Kafka Producer service to send the event
        kafkaProducer.sendMessage(NOTIFICATION_TOPIC, String.valueOf(alert.getUserId()), alertMessage);
    }

    // --- CRUD METHODS (Required by Controller) ---

    @Transactional
    public RateAlert save(RateAlert alert) {
        // Set initial status if not provided and save the alert
        if (alert.getStatus() == null) {
            alert.setStatus(RateAlert.AlertStatus.ACTIVE);
        }
        return repository.save(alert);
    }

    public List<RateAlert> findByUserId(Long userId) {
        // Fetch all alerts for a specific user
        return repository.findAllByUserId(userId);
    }

    // ... Implement logic for update and delete as needed

    // Add these two methods inside the RateAlertService class

    // --- Additional CRUD Methods (Required by Controller) ---

    public Optional<RateAlert> findById(Long id) {
        // Required for checking if the alert exists before deletion in the controller
        return repository.findById(id);
    }

    @Transactional
    public void delete(Long id) {
        // Required for the DELETE endpoint in the controller
        repository.deleteById(id);
    }
}