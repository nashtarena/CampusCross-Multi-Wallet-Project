package com.campuscross.fx_service.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Service responsible for publishing events to Kafka topics,
 * specifically used here for sending triggered FX alert notifications.
 */
@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Injects the KafkaTemplate configured by Spring Boot.
     * 
     * @param kafkaTemplate The template used to send messages.
     */
    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Sends a message to the specified Kafka topic.
     * 
     * @param topic   The target Kafka topic (e.g., "notification.fx.alerts").
     * @param key     The message key (typically the userId or alertId).
     * @param message The payload content (e.g., a JSON string detailing the alert).
     */
    public void sendMessage(String topic, String key, String message) {
        // Send the message asynchronously
        kafkaTemplate.send(topic, key, message);

        // Optional: Log successful sends for debugging
        System.out.println(String.format("Published Kafka Message: Topic=%s, Key=%s", topic, key));
    }
}