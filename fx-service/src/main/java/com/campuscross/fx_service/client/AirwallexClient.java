package com.campuscross.fx_service.client;

import com.campuscross.fx_service.config.AirwallexConfig;
import com.campuscross.fx_service.dto.airwallex.AirwallexAuthResponse;
import com.campuscross.fx_service.dto.airwallex.AirwallexRateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.campuscross.fx_service.dto.airwallex.AirwallexPayoutRequest;
import com.campuscross.fx_service.dto.airwallex.AirwallexPayoutResponse;

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

        String url = String.format(
                "%s/api/v1/fx/rates/current?buy_currency=%s&sell_currency=%s",
                config.getApiUrl(),
                toCurrency, // buy_currency
                fromCurrency); // sell_currency

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

        AirwallexRateResponse body = response.getBody();

        // ✅ USE rate_details[0].rate, NOT the top-level rate
        BigDecimal rate;
        if (body.getRateDetails() != null && !body.getRateDetails().isEmpty()) {
            rate = body.getRateDetails().get(0).getRate();
            log.info("Using rate_details[0].rate: {}", rate);
        } else {
            // Fallback to top-level rate if rate_details is empty
            rate = new BigDecimal(body.getRate());
            log.warn("rate_details was empty, falling back to top-level rate: {}", rate);
        }

        log.info("Airwallex rate {} → {} = {} ({} per 1 {})",
                fromCurrency, toCurrency, rate, toCurrency, fromCurrency);

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

    /**
     * Create a payout (bank transfer) via Airwallex
     */
    public AirwallexPayoutResponse createPayout(AirwallexPayoutRequest payoutRequest) {

        ensureAuthenticated();

        String url = config.getApiUrl() + "/api/v1/transfers";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AirwallexPayoutRequest> request = new HttpEntity<>(payoutRequest, headers);

        try {
            ResponseEntity<AirwallexPayoutResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    AirwallexPayoutResponse.class);

            if (response.getBody() == null) {
                throw new RuntimeException("Empty payout response from Airwallex");
            }

            log.info("Airwallex payout created: ID={}, Status={}",
                    response.getBody().getId(),
                    response.getBody().getStatus());

            return response.getBody();

        } catch (Exception e) {
            log.error("Airwallex payout creation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create payout via Airwallex", e);
        }
    }

    /**
     * Get payout status by Airwallex payout ID
     */
    public AirwallexPayoutResponse getPayoutStatus(String payoutId) {

        ensureAuthenticated();

        String url = String.format("%s/api/v1/payouts/%s", config.getApiUrl(), payoutId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<AirwallexPayoutResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    AirwallexPayoutResponse.class);

            if (response.getBody() == null) {
                throw new RuntimeException("Empty status response from Airwallex");
            }

            log.debug("Airwallex payout status: ID={}, Status={}",
                    payoutId,
                    response.getBody().getStatus());

            return response.getBody();

        } catch (Exception e) {
            log.error("Failed to get payout status: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve payout status", e);
        }
    }

    /**
     * Cancel a pending payout
     */
    public boolean cancelPayout(String payoutId) {

        ensureAuthenticated();

        String url = String.format("%s/api/v1/payouts/%s/cancel", config.getApiUrl(), payoutId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    String.class);

            boolean success = response.getStatusCode().is2xxSuccessful();

            if (success) {
                log.info("Airwallex payout cancelled: ID={}", payoutId);
            }

            return success;

        } catch (Exception e) {
            log.error("Failed to cancel payout: {}", e.getMessage(), e);
            return false;
        }
    }
}
