package com.campuscross.fx_service.service;

import com.campuscross.fx_service.dto.FxRateDto;
import com.campuscross.fx_service.delegate.FxCacheDelegate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
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
        private KafkaTemplate<String, FxRateDto> mockKafkaTemplate;

        @Mock
        private com.campuscross.fx_service.service.FxRateFetcher mockRateFetcher;

        private FxService fxService;

        @BeforeEach
        void setUp() {
                // Default topic used in tests
                fxService = new FxService(mockCacheDelegate, mockRateFetcher, mockKafkaTemplate, "fx.rates.realtime");
        }

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

                // ARRANGE: construct service with explicit topic so constructor injection works
                fxService = new FxService(mockCacheDelegate, mockRateFetcher, mockKafkaTemplate, "fx.rates.realtime");

                // ACT: Manually trigger the publishing logic
                fxService.scheduleAndPublishRates();

                // Verify the send method was used with our configured topic and the DTO type
                verify(mockKafkaTemplate, atLeastOnce())
                                .send(
                                                eq("fx.rates.realtime"),
                                                anyString(),
                                                any(FxRateDto.class));
        }
}