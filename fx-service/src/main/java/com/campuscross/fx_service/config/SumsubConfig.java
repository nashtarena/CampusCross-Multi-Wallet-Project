package com.campuscross.fx_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for Sumsub API
 */
@ConfigurationProperties(prefix = "sumsub")
public class SumsubConfig {

    private Api api = new Api();
    private String appToken;
    private String secretKey;
    private String levelName;

    public static class Api {
        private String url = "https://api.sumsub.com";

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    // Getters and Setters
    public Api getApi() {
        return api;
    }

    public void setApi(Api api) {
        this.api = api;
    }

    public String getAppToken() {
        return appToken;
    }

    public void setAppToken(String appToken) {
        this.appToken = appToken;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }
}