package com.campuscross.fx_service.client;

import com.campuscross.fx_service.dto.sumsub.SumsubApplicantResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class SumsubClient {

    private static final Logger log = LoggerFactory.getLogger(SumsubClient.class);

    private final String apiUrl;
    private final String appToken;
    private final String appSecret;
    private final String levelName;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // FIXED: Constructor injection instead of field injection
    public SumsubClient(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${sumsub.api-url:https://api.sumsub.com}") String apiUrl,
            @Value("${sumsub.app-token}") String appToken,
            @Value("${sumsub.secret-key}") String appSecret,
            @Value("${sumsub.level-name:basic-kyc-level}") String levelName) {

        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.apiUrl = apiUrl;
        this.appToken = appToken;
        this.appSecret = appSecret;
        this.levelName = levelName;

        // Debug log to verify configuration
        log.info("=== SUMSUB CLIENT INITIALIZED ===");
        log.info("API URL: {}", this.apiUrl);
        log.info("App Token: {}...", this.appToken != null ? this.appToken.substring(0, 10) : "NULL");
        log.info("Level Name: {}", this.levelName);
        log.info("=================================");
    }

    /**
     * Creates a Sumsub applicant with proper authentication.
     */
    public SumsubApplicantResponse createApplicant(String externalUserId, String firstName,
            String lastName, String email, String levelName) {
        validateCredentials();

        String endpoint = "/resources/applicants";
        String url = apiUrl + endpoint + "?levelName=" + URLEncoder.encode(levelName, StandardCharsets.UTF_8);
        long timestamp = System.currentTimeMillis() / 1000;

        // CRITICAL DEBUG LOG
        log.info("Creating Sumsub applicant - externalUserId: {}, levelName: {}",
                externalUserId, levelName);

        try {
            // Build request body as JSON string for signature calculation
            String requestBody = buildRequestBody(externalUserId, firstName, lastName, email);

            // Log the actual JSON being sent (for debugging)
            log.debug("Sumsub request body: {}", requestBody);

            // Create headers with proper authentication
            String endpointWithQuery = endpoint + "?levelName=" + URLEncoder.encode(levelName, StandardCharsets.UTF_8);
            HttpHeaders headers = createAuthHeaders("POST", endpointWithQuery, timestamp, requestBody);

            // HttpHeaders headers = createAuthHeaders("POST", endpoint, timestamp,
            // requestBody);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            // Make the API call
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class);

            if (response.getStatusCode() == HttpStatus.OK ||
                    response.getStatusCode() == HttpStatus.CREATED) {

                String responseBody = response.getBody();
                log.info("Sumsub applicant created successfully");
                return objectMapper.readValue(responseBody, SumsubApplicantResponse.class);
            } else {
                throw new RuntimeException("Unexpected response: " + response.getStatusCode());
            }

        } catch (HttpClientErrorException e) {
            String errorMsg = String.format(
                    "Sumsub API error: %s. Check API credentials in application.properties",
                    e.getMessage());
            log.error("Sumsub API Error Details: {}", e.getResponseBodyAsString());
            throw new RuntimeException(errorMsg, e);
        } catch (Exception e) {
            log.error("Failed to create Sumsub applicant", e);
            throw new RuntimeException("Failed to create Sumsub applicant: " + e.getMessage(), e);
        }
    }

    /**
     * Builds the JSON request body
     */
    private String buildRequestBody(String externalUserId, String firstName,
            String lastName, String email) {
        return String.format(
                "{\"externalUserId\":\"%s\",\"levelName\":\"%s\",\"info\":{\"firstName\":\"%s\",\"lastName\":\"%s\",\"email\":\"%s\"}}",
                externalUserId, levelName, firstName, lastName, email);
    }

    /**
     * Creates authentication headers required by Sumsub API.
     */
    private HttpHeaders createAuthHeaders(String method, String endpoint,
            long timestamp, String body) {
        HttpHeaders headers = new HttpHeaders();

        headers.add("X-App-Token", appToken);
        String timestampStr = String.valueOf(timestamp);
        headers.add("X-App-Access-Ts", timestampStr);

        String signature = calculateSignature(method, endpoint, timestampStr, body);
        headers.add("X-App-Access-Sig", signature);

        return headers;
    }

    /**
     * Calculates HMAC-SHA256 signature for Sumsub authentication.
     */
    private String calculateSignature(String method, String endpoint,
            String timestamp, String body) {
        try {
            StringBuilder signatureData = new StringBuilder();
            signatureData.append(timestamp);
            signatureData.append(method.toUpperCase());
            signatureData.append(endpoint);

            if (body != null && !body.isEmpty()) {
                signatureData.append(body);
            }

            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    appSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256");
            sha256Hmac.init(secretKey);

            byte[] hash = sha256Hmac.doFinal(
                    signatureData.toString().getBytes(StandardCharsets.UTF_8));

            return bytesToHex(hash);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to calculate HMAC signature", e);
        }
    }

    /**
     * Converts byte array to hex string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * Validates that required credentials are configured
     */
    private void validateCredentials() {
        if (appToken == null || appToken.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Sumsub API token not configured. Set sumsub.app-token in application.properties");
        }

        if (appSecret == null || appSecret.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Sumsub API secret not configured. Set sumsub.secret-key in application.properties");
        }

        if (levelName == null || levelName.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Sumsub level name not configured. Set sumsub.level-name in application.properties");
        }
    }
}