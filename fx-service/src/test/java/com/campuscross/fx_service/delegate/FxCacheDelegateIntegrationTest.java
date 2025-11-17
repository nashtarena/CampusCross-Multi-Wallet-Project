package com.campuscross.fx_service.delegate;

import com.campuscross.fx_service.client.SumsubClient;
import com.campuscross.fx_service.controller.KycController;
import com.campuscross.fx_service.controller.RateAlertController;
import com.campuscross.fx_service.repository.RateAlertRepository;
import com.campuscross.fx_service.repository.UserKycRepository;
import com.campuscross.fx_service.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.task.scheduling.enabled=false",
        "spring.task.execution.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
        "airwallex.enabled=false",
        "airwallex.api-url=http://localhost",
        "airwallex.client-id=test",
        "airwallex.api-key=test"
})
public class FxCacheDelegateIntegrationTest {

    @Autowired
    private FxRateFetcher mockFxRateFetcher;

    @Autowired
    private FxCacheDelegate cacheDelegate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    // Mock ALL JPA repositories
    @MockBean
    private UserKycRepository userKycRepository;

    @MockBean
    private RateAlertRepository rateAlertRepository;

    // Mock ALL services that depend on JPA
    @MockBean
    private KycService kycService;

    @MockBean
    private KycAsyncProcessor kycAsyncProcessor;

    @MockBean
    private RateAlertService rateAlertService;

    @MockBean
    private SumsubWebhookHandler sumsubWebhookHandler;

    @MockBean
    private OpenSanctionsService openSanctionsService;

    // Mock ALL controllers
    @MockBean
    private KycController kycController;

    @MockBean
    private RateAlertController rateAlertController;

    // Mock ALL external clients
    @MockBean
    private SumsubClient sumsubClient;

    @BeforeEach
    void setUp() {
        reset(mockFxRateFetcher);

        redisTemplate.execute((org.springframework.data.redis.connection.RedisConnection connection) -> {
            connection.serverCommands().flushDb();
            return "OK";
        });
    }

    @Test
    void shouldCallFetcherOnlyOnceWhenCacheIsHit() {
        String from = "USD";
        String to = "CAD";
        BigDecimal realRate = new BigDecimal("1.350000");

        when(mockFxRateFetcher.fetchRealRateFromApi(from, to))
                .thenReturn(Optional.of(realRate));

        cacheDelegate.getRateWithCache(from, to);
        cacheDelegate.getRateWithCache(from, to);

        verify(mockFxRateFetcher, times(1))
                .fetchRealRateFromApi(from, to);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public FxRateFetcher fxRateFetcher() {
            return Mockito.mock(FxRateFetcher.class);
        }
    }
}