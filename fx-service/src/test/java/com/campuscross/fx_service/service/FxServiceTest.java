package com.campuscross.fx_service.service;

import com.campuscross.fx_service.controller.QuoteResponse;
import com.campuscross.fx_service.delegate.FxCacheDelegate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// Pure Mockito Unit Test
@ExtendWith(MockitoExtension.class)
class FxServiceTest {

    private static final BigDecimal SPREAD = new BigDecimal("0.99");

    @Mock
    private FxCacheDelegate mockCacheDelegate;

    @Mock
    private KafkaTemplate<String, QuoteResponse> mockKafkaTemplate;

    @InjectMocks
    private FxService fxService;

    @Test
    void shouldApplySpreadCorrectlyToCustomerQuote() {
        // ARRANGE
        String from = "USD";
        String to = "EUR";
        BigDecimal realRate = new BigDecimal("1.000000");

        BigDecimal expectedCustomerRate = realRate
                .multiply(SPREAD)
                .setScale(6, RoundingMode.HALF_UP);

        when(mockCacheDelegate.getRateWithCache(from, to))
                .thenReturn(Optional.of(realRate));

        // ACT
        Optional<BigDecimal> result = fxService.getCustomerQuote(from, to);

        // ASSERT
        assertTrue(result.isPresent(), "Customer quote should be present");

        // FIX: Use compareTo(BigDecimal) which returns 0 on equality, ensuring
        // precision
        assertEquals(0, expectedCustomerRate.compareTo(result.get()),
                () -> "Spread not applied correctly. Expected: " + expectedCustomerRate
                        + ", got: " + result.get());

        verify(mockCacheDelegate, times(1)).getRateWithCache(from, to);
    }

    @Test
    void scheduledJobShouldCallKafkaTemplate() {
        // ARRANGE
        when(mockCacheDelegate.getRateWithCache(anyString(), anyString()))
                .thenReturn(Optional.of(new BigDecimal("0.50")));

        // ACT: Manually trigger the publishing logic
        fxService.scheduleAndPublishRates();

        // FIX: Verify the 3-argument send method used by your service
        verify(mockKafkaTemplate, atLeastOnce())
                .send(
                        eq("fx-rate-updates"),
                        anyString(),
                        any(QuoteResponse.class));
    }
}