package com.campuscross.fx_service.kafka;

import com.campuscross.fx_service.dto.FxRateDto;
import com.campuscross.fx_service.service.RateAlertService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka Consumer that listens for real-time FX rate updates
 * and triggers the alert checking logic.
 */
@Component
public class FxRateConsumer {

    private final RateAlertService alertService;

    public FxRateConsumer(RateAlertService alertService) {
        this.alertService = alertService;
    }

    /**
     * Listens to the real-time rate topic defined in application.properties.
     * The groupId "rate-alert-group" ensures this service is independent.
     * 
     * @param rateDto The deserialized FX rate data.
     */
    @KafkaListener(topics = "${fx.kafka.rate-topic}", groupId = "rate-alert-group")
    public void consumeRealTimeRate(FxRateDto rateDto) {
        System.out.println(String.format("Consumed new FX Rate: %s @ %s",
                rateDto.getCurrencyPair(), rateDto.getRateValue()));

        // Call the service layer to check alerts against the new rate
        alertService.checkAndTriggerAlerts(rateDto.getCurrencyPair(), rateDto.getRateValue());
    }
}