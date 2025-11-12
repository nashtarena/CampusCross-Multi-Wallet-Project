package com.campuscross.fx_service.delegate;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.campuscross.fx_service.service.FxRateFetcher; // Import your main service

@Service // Important: This registers it as a Spring bean for proxying
public class FxCacheDelegate {

    private final FxRateFetcher fxRateFetcher;

    // Inject the original service
    public FxCacheDelegate(FxRateFetcher fxRateFetcher) {
        this.fxRateFetcher = fxRateFetcher;
    }

    // This method is now on a separate bean, guaranteeing AOP interception
    @Cacheable(value = "fx-rates", key = "#from + '_' + #to")
    public Optional<BigDecimal> getRateWithCache(String from, String to) {
        // If the cache hits, this method is skipped entirely.
        // If the cache misses, we execute the real logic from the main service.
        return fxRateFetcher.fetchRealRateFromApi(from, to);
    }
}