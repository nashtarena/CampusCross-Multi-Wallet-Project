package com.campuscross.fx_service.controller;

import com.campuscross.fx_service.service.FxService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.util.Optional;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

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
    @GetMapping("/quote/{from}/{to}")
    public ResponseEntity<QuoteResponse> getQuote(@PathVariable String from, @PathVariable String to) {

        // 3. Call your service's business logic
        Optional<BigDecimal> customerRate = fxService.getCustomerQuote(from, to);

        if (customerRate.isPresent()) {
            QuoteResponse response = new QuoteResponse(from, to, customerRate.get());
            return ResponseEntity.ok(response); // Returns 200 OK
        } else {
            // Returns 404 NOT FOUND
            return ResponseEntity.notFound().build();
        }
    }
}

// --- Create this simple class (e.g., QuoteResponse.java) ---
// This defines the clean JSON you send back to the user
