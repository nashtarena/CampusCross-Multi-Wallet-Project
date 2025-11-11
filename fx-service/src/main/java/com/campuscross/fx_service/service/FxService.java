package com.campuscross.fx_service.service;

import com.campuscross.fx_service.controller.QuoteResponse; // Assumed location of your response DTO
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate; // NEW IMPORT
import org.springframework.scheduling.annotation.Scheduled; // NEW IMPORT
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
public class FxService {

    private static final Logger log = LoggerFactory.getLogger(FxService.class);

    private final RestTemplate restTemplate;
    private final KafkaTemplate<String, QuoteResponse> kafkaTemplate; // NEW FIELD
    private static final String FX_RATE_TOPIC = "fx-rate-updates"; // Topic name
    private static final BigDecimal SPREAD = new BigDecimal("0.99"); // Your 1% profit multiplier

    @Value("${fx.api.key}")
    private String apiKey;

    // MODIFIED CONSTRUCTOR: Now injects the RestTemplate AND KafkaTemplate
    public FxService(RestTemplate restTemplate, KafkaTemplate<String, QuoteResponse> kafkaTemplate) {
        this.restTemplate = restTemplate;
        this.kafkaTemplate = kafkaTemplate; // NEW ASSIGNMENT
    }

    /**
     * Public method called by the Controller. Returns the customer-facing rate
     * (with spread).
     */
    public Optional<BigDecimal> getCustomerQuote(String from, String to) {
        Optional<BigDecimal> realRate = fetchRealRate(from, to);

        // If the realRate is present, apply the spread (0.99 for 1% profit)
        return realRate.map(rate -> rate.multiply(SPREAD).setScale(6, RoundingMode.HALF_UP));
    }

    /**
     * Private method to fetch the REAL Mid-Market Rate (MMR).
     * Uses @Cacheable to store results in Redis.
     */
    @Cacheable(value = "fx-rates", key = "#from + '-' + #to")
    public Optional<BigDecimal> fetchRealRate(String from, String to) {
        String apiUrl = String.format(
                "https://v6.exchangerate-api.com/v6/%s/latest/%s", apiKey, from);

        // log.info("Attempting to fetch rate using URL: {}", apiUrl);
        log.warn("API Call Executed: Fetching new rate for {} to {}. This indicates a CACHE MISS or Expiration.", from,
                to);

        try {
            // Make the call to the external API
            // Note: FxApiResponse must be correctly defined to match the API response.
            FxApiResponse response = restTemplate.getForObject(apiUrl, FxApiResponse.class);
            // log.info("API Response received: {}", response);

            if (response != null && response.getConversion_rates() != null
                    && response.getConversion_rates().containsKey(to)) {

                return Optional.of(response.getConversion_rates().get(to));
            }

        } catch (RestClientException e) {
            log.error("Failed to fetch FX rate for {} to {}: {}", from, to, e.getMessage());
        }

        return Optional.empty();
    }

    /**
     * NEW METHOD: Scheduled task to periodically refresh ALL MVP rates and publish
     * them to Kafka.
     * Runs every 2 hours (7200000 ms).
     */
    @Scheduled(fixedRate = 7200000) // Every 2 hours
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