package com.campuscross.fx_service.service;

import com.campuscross.fx_service.config.SumsubConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Service to generate Sumsub SDK access tokens
 */
@Service
public class SumsubTokenService {

    private static final Logger log = LoggerFactory.getLogger(SumsubTokenService.class);

    private final SumsubConfig sumsubConfig;
    private final RestTemplate restTemplate;

    public SumsubTokenService(SumsubConfig sumsubConfig, RestTemplate restTemplate) {
        this.sumsubConfig = sumsubConfig;
        this.restTemplate = restTemplate;
    }

    /**
     * Generate access token for Sumsub SDK
     * This token allows mobile/web app to launch Sumsub widget
     */
    public String generateAccessToken(String externalUserId, String levelName) {
        try {
            String endpoint = String.format("/resources/accessTokens?userId=%s&levelName=%s",
                    externalUserId, levelName);
            String url = sumsubConfig.getApi().getUrl() + endpoint;

            long timestamp = System.currentTimeMillis() / 1000;

            HttpHeaders headers = createAuthHeaders("POST", endpoint, timestamp);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(null, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    Map.class);

            if (response.getBody() != null && response.getBody().containsKey("token")) {
                String token = (String) response.getBody().get("token");
                log.info("Generated access token for user: {}", externalUserId);
                return token;
            }

            throw new RuntimeException("No token in response");

        } catch (Exception e) {
            log.error("Failed to generate access token: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate Sumsub access token", e);
        }
    }

    /**
     * Create authentication headers for Sumsub API
     */
    private HttpHeaders createAuthHeaders(String method, String endpoint, long timestamp) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-App-Token", sumsubConfig.getAppToken());

        String timestampStr = String.valueOf(timestamp);
        headers.add("X-App-Access-Ts", timestampStr);

        String signature = calculateSignature(method, endpoint, timestampStr, null);
        headers.add("X-App-Access-Sig", signature);

        return headers;
    }

    /**
     * Calculate HMAC-SHA256 signature
     */
    private String calculateSignature(String method, String endpoint, String timestamp, String body) {
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
                    sumsubConfig.getSecretKey().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256");
            sha256Hmac.init(secretKey);

            byte[] hash = sha256Hmac.doFinal(signatureData.toString().getBytes(StandardCharsets.UTF_8));

            return bytesToHex(hash);

        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate signature", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}