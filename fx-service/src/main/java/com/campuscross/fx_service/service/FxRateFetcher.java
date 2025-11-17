package com.campuscross.fx_service.service;

import com.campuscross.fx_service.client.AirwallexClient;
import com.campuscross.fx_service.config.AirwallexConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class FxRateFetcher {

    private static final Logger log = LoggerFactory.getLogger(FxRateFetcher.class);

    private final RestTemplate restTemplate;
    private final AirwallexClient airwallexClient;
    private final AirwallexConfig airwallexConfig;

    // Keep ExchangeRate API as fallback
    @Value("${fx.api.key}")
    private String exchangeRateApiKey;

    public FxRateFetcher(
            RestTemplate restTemplate,
            AirwallexClient airwallexClient,
            AirwallexConfig airwallexConfig) {
        this.restTemplate = restTemplate;
        this.airwallexClient = airwallexClient;
        this.airwallexConfig = airwallexConfig;
    }

    /**
     * Fetch FX rate with intelligent provider selection
     * * Priority:
     * 1. Airwallex (if configured and enabled)
     * 2. ExchangeRate API (fallback)
     */
    public Optional<BigDecimal> fetchRealRateFromApi(String from, String to) {

        // 🚨 CRITICAL CHECK LOG: Ensure API Key is loaded
        if (exchangeRateApiKey == null || exchangeRateApiKey.isEmpty()) {
            log.error("🔑 fx.api.key is MISSING or EMPTY in application.properties. Fallback will fail.");
        }

        // Try Airwallex first if configured
        if (airwallexConfig.isEnabled() && airwallexClient.isConfigured()) {
            try {
                log.info("🚀 Attempting Airwallex (Primary): {} → {}", from, to);
                BigDecimal rate = airwallexClient.getCurrentRate(from, to);
                log.info("✅ Airwallex Success. Rate: {}", rate);
                return Optional.of(rate);
            } catch (Exception e) {
                log.warn("❌ Airwallex API failed for {} → {}. Falling back. Error: {}",
                        from, to, e.getMessage());
            }
        } else {
            log.info("⚠️ Airwallex is disabled or not configured. Skipping primary attempt.");
        }

        // Fallback to ExchangeRate API
        return fetchFromExchangeRateApi(from, to);
    }

    /**
     * Fetch rate from ExchangeRate API (original implementation)
     * Kept as fallback for reliability
     */
    private Optional<BigDecimal> fetchFromExchangeRateApi(String from, String to) {

        // 🚨 CRITICAL CHECK LOG: Prevent API call if key is known to be missing
        if (exchangeRateApiKey == null || exchangeRateApiKey.isEmpty()) {
            log.error("🛑 ExchangeRate API skip: Cannot fetch because API key is unset.");
            return Optional.empty();
        }

        String apiUrl = String.format(
                "https://v6.exchangerate-api.com/v6/%s/latest/%s",
                exchangeRateApiKey, from);

        log.info("🛰️ Attempting ExchangeRate API (Fallback): {} → {}. URL: {}", from, to, apiUrl);

        try {
            FxApiResponse response = restTemplate.getForObject(apiUrl, FxApiResponse.class);

            // 🚨 DIAGNOSTIC LOG: Check if the top-level response or map is null
            if (response == null) {
                log.error("❌ Fallback API returned a NULL response object.");
                return Optional.empty();
            }
            if (response.getConversion_rates() == null) {
                log.error(
                        "❌ Fallback API response is missing 'conversion_rates' map. Check JSON structure or API key validity.");
                // Assuming FxApiResponse has a getResult() method from the original code
                // suggestion:
                // log.error("Response status/result was: {}", response.getResult());
                return Optional.empty();
            }

            Double rawRate = response.getConversion_rates().get(to);

            if (rawRate != null) {
                BigDecimal preciseRate = BigDecimal.valueOf(rawRate);
                log.info("✅ Fallback API Success. Rate: {}", preciseRate);
                return Optional.of(preciseRate);
            } else {
                log.error("❌ Fallback API response did not contain rate for target currency '{}'. Available keys: {}",
                        to, response.getConversion_rates().keySet());
            }

        } catch (RestClientException e) {
            // Catches connection issues, 4xx/5xx HTTP errors, and JSON parsing errors
            log.error("🔥 CRITICAL API FAILURE: RestTemplate call to ExchangeRate API failed for {} to {}. Error: {}",
                    from, to, e.getMessage());
        }

        return Optional.empty();
    }
}