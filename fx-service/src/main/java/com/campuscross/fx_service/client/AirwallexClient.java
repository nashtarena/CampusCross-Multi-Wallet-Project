package com.campuscross.fx_service.client;

import com.campuscross.fx_service.config.AirwallexConfig;
import com.campuscross.fx_service.dto.airwallex.AirwallexAuthResponse;
import com.campuscross.fx_service.dto.airwallex.AirwallexRateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;

@Component
public class AirwallexClient {

    private static final Logger log = LoggerFactory.getLogger(AirwallexClient.class);

    private final AirwallexConfig config;
    private final RestTemplate restTemplate;

    private String accessToken;
    private Instant tokenExpiresAt;

    public AirwallexClient(AirwallexConfig config, RestTemplate restTemplate) {
        this.config = config;
        this.restTemplate = restTemplate;
    }

    /**
     * Fetch live FX rate from Airwallex
     */
    public BigDecimal getCurrentRate(String fromCurrency, String toCurrency) {

        ensureAuthenticated();

        // Required endpoint (exact path)
        String url = String.format(
                "%s/api/v1/fx/rates/current?buy_currency=%s&sell_currency=%s&sell_amount=1",
                config.getApiUrl(),
                toCurrency,
                fromCurrency);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<AirwallexRateResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                AirwallexRateResponse.class);

        if (response.getBody() == null) {
            throw new RuntimeException("Empty rate response from Airwallex");
        }

        BigDecimal rate = new BigDecimal(response.getBody().getRate());

        log.info("Airwallex rate {} → {} = {}", fromCurrency, toCurrency, rate);

        return rate;
    }

    /**
     * Ensure we have a valid Bearer token
     */
    private void ensureAuthenticated() {
        if (accessToken != null && tokenExpiresAt != null &&
                Instant.now().plusSeconds(300).isBefore(tokenExpiresAt)) {
            return; // still valid
        }
        authenticate();
    }

    /**
     * Authenticate with Airwallex using:
     * x-client-id
     * x-api-key
     * And receive:
     * token (Bearer)
     * expires_at
     */
    private void authenticate() {
        try {
            String url = config.getApiUrl() + "/api/v1/authentication/login";

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-client-id", config.getClientId());
            headers.set("x-api-key", config.getApiKey());
            headers.set("x-api-version", "2023-03-01");
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>("{}", headers);

            ResponseEntity<AirwallexAuthResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    AirwallexAuthResponse.class);

            if (response.getBody() == null) {
                throw new RuntimeException("Null authentication response");
            }

            AirwallexAuthResponse auth = response.getBody();

            this.accessToken = auth.getToken();

            // Parse expires_at (format: "2025-11-14T08:08:30+0000")
            String raw = auth.getExpiresAt();
            Instant expires;

            try {
                String normalized = raw.replaceAll("(\\+|\\-)(\\d{2})(\\d{2})$", "$1$2:$3");
                expires = Instant.parse(normalized);
            } catch (Exception ex) {
                log.warn("Unable to parse expires_at. Using fallback 1hr.");
                expires = Instant.now().plusSeconds(3600);
            }

            this.tokenExpiresAt = expires;

            log.info("Authenticated with Airwallex. Token expires at {}", expires);

        } catch (Exception e) {
            log.error("Authentication failed: {}", e.getMessage());
            throw new RuntimeException("Airwallex authentication failed", e);
        }
    }

    public boolean isConfigured() {
        return config.getClientId() != null && !config.getClientId().isEmpty()
                && config.getApiKey() != null && !config.getApiKey().isEmpty();
    }
}
