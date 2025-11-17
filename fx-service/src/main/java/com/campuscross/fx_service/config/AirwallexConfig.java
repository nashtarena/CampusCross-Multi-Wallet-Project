package com.campuscross.fx_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration properties for Airwallex API
 */

@ConfigurationProperties(prefix = "airwallex")
public class AirwallexConfig {

    private String apiUrl = "https://api-demo.airwallex.com"; // Sandbox by default
    private String clientId;
    private String apiKey;
    private boolean enabled = true; // Feature flag

    // Getters and Setters
    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}