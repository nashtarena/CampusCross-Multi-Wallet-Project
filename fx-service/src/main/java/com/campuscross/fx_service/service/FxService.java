package com.campuscross.fx_service.service;

import com.campuscross.fx_service.delegate.FxCacheDelegate;
import com.campuscross.fx_service.dto.FxRateDto;

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
    private final KafkaTemplate<String, FxRateDto> kafkaTemplate; // NEW FIELD
    // use topic from properties via @Value
    private final String fxRateTopic;
    private static final BigDecimal SPREAD = new BigDecimal("0.99"); // Your 1% profit multiplier

    // Constructor injection for both the client (e.g.) and the delegate

    public FxService(
            FxCacheDelegate cacheDelegate,
            FxRateFetcher rateFetcher,
            KafkaTemplate<String, FxRateDto> kafkaTemplate,
            @org.springframework.beans.factory.annotation.Value("${fx.kafka.rate-topic:fx.rates.realtime}") String fxRateTopic) {

        // 1. Caching Delegate
        this.cacheDelegate = cacheDelegate;

        // 2. Rate Fetcher
        this.rateFetcher = rateFetcher;

        // 4. Messaging Client
        this.kafkaTemplate = kafkaTemplate;
        this.fxRateTopic = fxRateTopic;
    }

    // MODIFIED CONSTRUCTOR: Now injects the RestTemplate AND KafkaTemplate

    /**
     * Public method called by the Controller. Returns the customer-facing rate
     * (with spread).
     */

    public Optional<BigDecimal> getCustomerQuote(String from, String to) {
        Optional<BigDecimal> realRate = cacheDelegate.getRateWithCache(from, to);

        return realRate.map(rate -> {
            BigDecimal correctedRate = rate;

            // ✅ FIX 1: Invert USD → EUR and USD → GBP
            if ("USD".equals(from) && ("EUR".equals(to) || "GBP".equals(to))) {
                correctedRate = BigDecimal.ONE.divide(rate, 10, RoundingMode.HALF_UP);
                log.info("Inverted USD→{}: {} → {}", to, rate, correctedRate);
            }

            // ✅ FIX 2: Invert all JPY → XXX pairs
            if ("JPY".equals(from)) {
                correctedRate = BigDecimal.ONE.divide(rate, 10, RoundingMode.HALF_UP);
                log.info("Inverted JPY→{}: {} → {}", to, rate, correctedRate);
            }

            // Apply the spread (0.99 for 1% profit)
            return correctedRate.multiply(SPREAD).setScale(6, RoundingMode.HALF_UP);
        });
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
                        // Create the DTO payload matching consumer expectations
                        FxRateDto dto = new FxRateDto();
                        dto.setCurrencyPair(from + "/" + to);
                        dto.setRateValue(customerRate);
                        dto.setTimestamp(java.time.Instant.now());

                        String key = from + "/" + to;

                        // Publish to configured Kafka topic
                        kafkaTemplate.send(fxRateTopic, key, dto);
                        log.debug("Published rate update: {} to {}", key, fxRateTopic);
                    } catch (Exception e) {
                        log.error("Error publishing rate for {}-{}: {}", from, to, e.getMessage());
                    }
                });
            }
        }
    }
}