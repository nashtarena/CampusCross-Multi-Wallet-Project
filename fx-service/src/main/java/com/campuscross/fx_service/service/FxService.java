package com.campuscross.fx_service.service;

import com.campuscross.fx_service.delegate.FxCacheDelegate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
public class FxService {

    private static final Logger log = LoggerFactory.getLogger(FxService.class);
    // private final FxCacheDelegate cacheDelegate;

    @Autowired
    private final FxCacheDelegate cacheDelegate;
    private final FxRateFetcher rateFetcher;

    private static final BigDecimal SPREAD = new BigDecimal("0.99"); // Your 1% profit multiplier

    public FxService(
            FxCacheDelegate cacheDelegate,
            FxRateFetcher rateFetcher) {

        // 1. Caching Delegate
        this.cacheDelegate = cacheDelegate;

        // 2. Rate Fetcher
        this.rateFetcher = rateFetcher;
    }

    /**
     * Public method called by the Controller. Returns the customer-facing rate
     * (with spread).
     */

    public Optional<BigDecimal> getCustomerQuote(String from, String to) {
        Optional<BigDecimal> realRate = cacheDelegate.getRateWithCache(from, to);

        return realRate.map(rate -> {
            BigDecimal correctedRate = rate;

            // ✅ FIX 1: Invert USD → EUR and USD → GBP
            if ("USD".equals(from) && ("EUR".equals(to) || "GBP".equals(to))) {
                correctedRate = BigDecimal.ONE.divide(rate, 10, RoundingMode.HALF_UP);
                log.info("Inverted USD→{}: {} → {}", to, rate, correctedRate);
            }

            // ✅ FIX 2: Invert all JPY → XXX pairs
            if ("JPY".equals(from)) {
                correctedRate = BigDecimal.ONE.divide(rate, 10, RoundingMode.HALF_UP);
                log.info("Inverted JPY→{}: {} → {}", to, rate, correctedRate);
            }

            // Apply the spread (0.99 for 1% profit)
            return correctedRate.multiply(SPREAD).setScale(6, RoundingMode.HALF_UP);
        });
    }

}