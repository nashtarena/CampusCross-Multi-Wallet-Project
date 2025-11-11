package com.campuscross.fx_service.controller;

import com.campuscross.fx_service.service.FxService;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Optional;

import java.math.BigDecimal;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/fx") // This sets the base URL for this class
public class FxController {

    private final FxService fxService;

    // Spring injects your service
    public FxController(FxService fxService) {
        this.fxService = fxService;
    }

    /**
     * This method handles: GET /api/v1/fx/quote?from=USD&to=EUR
     */
    @GetMapping("/quote")
    public QuoteResponse getQuote(@RequestParam String from, @RequestParam String to) {

        // 3. Call your service's business logic
        Optional<BigDecimal> customerRate = fxService.getCustomerQuote(from, to);

        // 4. Return a clean JSON response
        return new QuoteResponse(from, to, customerRate.orElse(null));
    }
}

// --- Create this simple class (e.g., QuoteResponse.java) ---
// This defines the clean JSON you send back to the user
