package com.campuscross.fx_service.service;

import com.campuscross.fx_service.controller.QuoteResponse; // Assumed location of your response DTO
import com.campuscross.fx_service.delegate.FxCacheDelegate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.kafka.core.KafkaTemplate; // NEW IMPORT
import org.springframework.scheduling.annotation.Scheduled; // NEW IMPORT
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
public class FxService {

    private static final Logger log = LoggerFactory.getLogger(FxService.class);
    // private final FxCacheDelegate cacheDelegate;

    @Autowired
    private final FxCacheDelegate cacheDelegate;
    private final FxRateFetcher rateFetcher;

    // private final RestTemplate restTemplate;
    private final KafkaTemplate<String, QuoteResponse> kafkaTemplate; // NEW FIELD
    private static final String FX_RATE_TOPIC = "fx-rate-updates"; // Topic name
    private static final BigDecimal SPREAD = new BigDecimal("0.99"); // Your 1% profit multiplier

    // Constructor injection for both the client (e.g.) and the delegate
    public FxService(
            FxCacheDelegate cacheDelegate,
            FxRateFetcher rateFetcher,
            KafkaTemplate<String, QuoteResponse> kafkaTemplate) {

        // 1. Caching Delegate
        this.cacheDelegate = cacheDelegate;

        // 2. Rate Fetcher
        this.rateFetcher = rateFetcher;

        // 4. Messaging Client
        this.kafkaTemplate = kafkaTemplate;
    }

    // MODIFIED CONSTRUCTOR: Now injects the RestTemplate AND KafkaTemplate

    /**
     * Public method called by the Controller. Returns the customer-facing rate
     * (with spread).
     */

    public Optional<BigDecimal> getCustomerQuote(String from, String to) {
        Optional<BigDecimal> realRate = cacheDelegate.getRateWithCache(from, to);

        // If the realRate is present, apply the spread (0.99 for 1% profit)
        return realRate.map(rate -> rate.multiply(SPREAD).setScale(6, RoundingMode.HALF_UP));
    }

    /**
     * NEW METHOD: Scheduled task to periodically refresh ALL MVP rates and publish
     * them to Kafka.
     * Runs every 2 hours (7200000 ms).
     */
    @Scheduled(fixedRate = 120000) // Every 2 hours
    public void scheduleAndPublishRates() {
        // Defined all four MVP currencies to ensure comprehensive coverage
        String[] currencies = { "USD", "EUR", "GBP", "JPY" };

        // Loop through all permutations to fetch rates (e.g., USD->EUR, EUR->USD)
        for (String from : currencies) {
            for (String to : currencies) {
                // Skip same-currency pairs
                if (from.equals(to)) {
                    continue;
                }

                // This call automatically hits the cache or calls the API
                Optional<BigDecimal> quoteOptional = getCustomerQuote(from, to);

                quoteOptional.ifPresent(customerRate -> {
                    try {
                        // Create the message payload
                        QuoteResponse message = new QuoteResponse(from, to, customerRate);
                        String key = from + "-" + to;

                        // Publish to Kafka
                        kafkaTemplate.send(FX_RATE_TOPIC, key, message);
                        log.debug("Published rate update: {} to {}", key, FX_RATE_TOPIC);
                    } catch (Exception e) {
                        log.error("Error publishing rate for {}-{}: {}", from, to, e.getMessage());
                    }
                });
            }
        }
    }
}