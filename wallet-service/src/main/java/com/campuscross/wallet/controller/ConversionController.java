package com.campuscross.wallet.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.campuscross.wallet.dto.ConversionRequest;
import com.campuscross.wallet.entity.Transaction;
import com.campuscross.wallet.service.CurrencyConversionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/conversions")
@RequiredArgsConstructor
@Slf4j
public class ConversionController {
    
    private final CurrencyConversionService conversionService;
    
    /**
     * Execute currency conversion
     * POST /api/v1/conversions
     */
    @PostMapping
    public ResponseEntity<Transaction> executeConversion(@Valid @RequestBody ConversionRequest request) {
        log.info("Executing conversion: {} {} to {} for user {}", 
                request.getAmount(), request.getFromCurrency(), 
                request.getToCurrency(), request.getUserId());
        
        Transaction transaction = conversionService.executeConversion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }
    
    /**
     * Preview conversion amount
     * GET /api/v1/conversions/preview?amount=100&rate=0.92
     */
    @GetMapping("/preview")
    public ResponseEntity<BigDecimal> previewConversion(
            @RequestParam BigDecimal amount,
            @RequestParam BigDecimal rate) {
        log.info("Previewing conversion: {} at rate {}", amount, rate);
        BigDecimal result = conversionService.previewConversion(amount, rate);
        return ResponseEntity.ok(result);
    }
    
    /**
     * Get conversion history for user
     * GET /api/v1/conversions/history/{userId}
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<Transaction>> getConversionHistory(@PathVariable Long userId) {
        log.info("Fetching conversion history for user {}", userId);
        List<Transaction> history = conversionService.getConversionHistory(userId);
        return ResponseEntity.ok(history);
    }
}