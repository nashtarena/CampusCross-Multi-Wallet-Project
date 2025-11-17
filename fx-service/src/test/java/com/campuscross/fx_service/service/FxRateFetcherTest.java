package com.campuscross.fx_service.service;

import com.campuscross.fx_service.client.AirwallexClient;
import com.campuscross.fx_service.config.AirwallexConfig;
import com.campuscross.fx_service.service.FxApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = { FxRateFetcher.class, FxRateFetcherTest.TestConfig.class })
@TestPropertySource(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "spring.task.scheduling.enabled=false",
        "spring.task.execution.enabled=false",
        "fx.api.key=test-api-key"
})
class FxRateFetcherTest {

    @Autowired
    private FxRateFetcher fxRateFetcher;

    @Autowired
    private RestTemplate mockRestTemplate;

    @MockBean
    private AirwallexClient airwallexClient;

    @MockBean
    private AirwallexConfig airwallexConfig;

    @MockBean
    private FxApiResponse fxApiResponse;

    @BeforeEach
    void setUp() {
        Mockito.reset(mockRestTemplate, airwallexClient, airwallexConfig);

        // Configure Airwallex to be disabled (use ExchangeRate API fallback)
        when(airwallexConfig.isEnabled()).thenReturn(false);
    }

    @Test
    void shouldReturnRateOnSuccessfulApiCall() {
        // Arrange
        String fromCurrency = "USD";
        String toCurrency = "EUR";
        Double expectedRate = 0.85;

        FxApiResponse mockResponse = new FxApiResponse();
        Map<String, Double> rates = new HashMap<>();
        rates.put(toCurrency, expectedRate);
        mockResponse.setConversion_rates(rates);

        when(mockRestTemplate.getForObject(anyString(), eq(FxApiResponse.class)))
                .thenReturn(mockResponse);

        // Act
        Optional<BigDecimal> result = fxRateFetcher.fetchRealRateFromApi(fromCurrency, toCurrency);

        // Assert
        assertTrue(result.isPresent(), "Rate should be present");
        assertEquals(new BigDecimal("0.85"), result.get(), "Rate should match expected value");
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public RestTemplate restTemplate() {
            return Mockito.mock(RestTemplate.class);
        }
    }
}