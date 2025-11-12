package com.campuscross.fx_service.service; // Or .util/.api if you prefer

import java.math.BigDecimal;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service // Registers this as a Spring bean for dependency injection
public class FxRateFetcher {

    private static final Logger log = LoggerFactory.getLogger(FxRateFetcher.class);

    // Dependencies
    private final RestTemplate restTemplate;

    // Configuration
    @Value("${fx.api.key}")
    private String apiKey;

    // Constructor Injection (Recommended)
    public FxRateFetcher(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Calls the external FX API to retrieve the current real exchange rate.
     * This method is the core worker and is NEVER cached internally.
     * * @param from The base currency (e.g., USD)
     * 
     * @param to The target currency (e.g., EUR)
     * @return Optional containing the exchange rate, or empty if the API call fails
     *         or is missing data.
     */
    // Inside FxRateFetcher.java

    public Optional<BigDecimal> fetchRealRateFromApi(String from, String to) {

        String apiUrl = String.format(
                "https://v6.exchangerate-api.com/v6/%s/latest/%s", apiKey, from);

        log.warn("API Call Executed: Fetching new rate for {} to {}. This indicates a CACHE MISS or Expiration.", from,
                to);

        try {
            // FxApiResponse now correctly returns Map<String, Double>
            FxApiResponse response = restTemplate.getForObject(apiUrl, FxApiResponse.class);

            if (response != null && response.getConversion_rates() != null) {

                // 1. Get the value as a Double (raw API type)
                Double rawRate = response.getConversion_rates().get(to);

                // 2. Check if the rate was found
                if (rawRate != null) {
                    // 3. CRITICAL FINAL FIX: Convert the raw Double to a BigDecimal
                    // to match the method's return signature (Optional<BigDecimal>)
                    BigDecimal preciseRate = BigDecimal.valueOf(rawRate);
                    return Optional.of(preciseRate);
                }
            }

        } catch (RestClientException e) {
            log.error("Failed to fetch FX rate for {} to {}: {}", from, to, e.getMessage());
        }

        // Return empty if API call failed or rate was not found in the response map
        return Optional.empty();
    }
}