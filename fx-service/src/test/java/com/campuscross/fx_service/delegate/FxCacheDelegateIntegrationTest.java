package com.campuscross.fx_service.delegate;

import com.campuscross.fx_service.service.FxRateFetcher;
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

import com.campuscross.fx_service.repository.UserKycRepository;
import com.campuscross.fx_service.repository.RateAlertRepository;
import com.campuscross.fx_service.service.KycService;
import com.campuscross.fx_service.service.KycAsyncProcessor;
import com.campuscross.fx_service.service.RateAlertService;
import com.campuscross.fx_service.controller.KycController;
import com.campuscross.fx_service.controller.RateAlertController;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.task.scheduling.enabled=false",
        "spring.task.execution.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
public class FxCacheDelegateIntegrationTest {

    @Autowired
    private FxRateFetcher mockFxRateFetcher;

    @Autowired
    private FxCacheDelegate cacheDelegate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    // Mock all JPA repositories
    @MockBean
    private UserKycRepository userKycRepository;

    @MockBean
    private RateAlertRepository rateAlertRepository;

    // Mock all services that depend on repositories
    @MockBean
    private KycService kycService;

    @MockBean
    private KycAsyncProcessor kycAsyncProcessor;

    @MockBean
    private RateAlertService rateAlertService;

    // Mock all controllers that depend on those services
    @MockBean
    private KycController kycController;

    @MockBean
    private RateAlertController rateAlertController;

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