package com.campuscross.fx_service.service;

import com.campuscross.fx_service.service.FxApiResponse; // Assuming this class is in model
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// 1. OPTIMIZED CONTEXT: Only load the class under test (FxRateFetcher) and the mock config.
//    If FxRateFetcher has dependencies (like FxService), those MUST also be included here,
//    or mocked with @MockitoBean. Assuming RestTemplate is the only external dependency for this method.
@SpringBootTest(classes = { FxRateFetcher.class, FxRateFetcherTest.TestConfig.class }, properties = {
        "spring.main.allow-bean-definition-overriding=true"
})
// 2. These properties are harmless, but largely unnecessary when only loading
// FxRateFetcher.
// We will keep them to maintain consistency.
@TestPropertySource(properties = {
        "spring.task.scheduling.enabled=false",
        "spring.task.execution.enabled=false"
})
// 3. Kafka exclusion is no longer strictly needed here, as we are not loading
// the full
// application context, but we will remove the @EnableAutoConfiguration
// annotation
// which was giving the "attribute undefined" error in older Spring versions.
class FxRateFetcherTest {

    // Removed @MockitoBean for KafkaTemplate, as it is unnecessary when
    // FxService/KafkaAutoConfiguration
    // are not loaded, simplifying the test.

    @Autowired
    private RestTemplate mockRestTemplate;

    @Autowired
    private FxRateFetcher fxRateFetcher;

    @BeforeEach
    void setUp() {
        // Good practice: reset mock state before each test
        Mockito.reset(mockRestTemplate);
    }

    @Test
    void shouldReturnRateOnSuccessfulApiCall() {
        // ARRANGE
        String from = "USD";
        String to = "EUR";
        BigDecimal expectedRate = new BigDecimal("1.2345");

        // Assuming FxApiResponse is structured to receive raw Double values from the
        // API
        FxApiResponse response = new FxApiResponse();
        Map<String, Double> rates = new HashMap<>();
        rates.put(to, expectedRate.doubleValue());
        // NOTE: Ensure your FxApiResponse class has a setConversion_rates method
        // that accepts Map<String, Double>
        // If not, use the correct setter method name.
        response.setConversion_rates(rates);

        when(mockRestTemplate.getForObject(anyString(), eq(FxApiResponse.class)))
                .thenReturn(response);

        // ACT - Correct method call as confirmed by your previous code
        Optional<BigDecimal> result = fxRateFetcher.fetchRealRateFromApi(from, to);

        // ASSERT
        assertTrue(result.isPresent(), "Rate fetch failed after mock response was processed.");

        // Assertions use BigDecimal.compareTo()
        assertEquals(0, expectedRate.compareTo(result.get()),
                "The converted BigDecimal rate did not match the expected value.");

        // Verify the external RestTemplate call was made exactly once
        verify(mockRestTemplate, times(1))
                .getForObject(anyString(), eq(FxApiResponse.class));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary // Ensures this mock is preferred over any existing RestTemplate bean
        public RestTemplate restTemplate() {
            // This creates the mock instance that is @Autowired into the test class
            return Mockito.mock(RestTemplate.class);
        }
    }

}