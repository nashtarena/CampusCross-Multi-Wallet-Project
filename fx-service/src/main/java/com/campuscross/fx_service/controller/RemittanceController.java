package com.campuscross.fx_service.controller;

import com.campuscross.fx_service.dto.remittance.RemittanceRequest;
import com.campuscross.fx_service.dto.remittance.RemittanceResponse;
import com.campuscross.fx_service.dto.remittance.RemittanceStatusResponse;
import com.campuscross.fx_service.service.RemittanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for bank remittance/transfer operations
 * Integrates with FX, KYC, and Airwallex systems
 */
@RestController
@RequestMapping("/api/v1/remittances")
@CrossOrigin(origins = "http://localhost:3000")
public class RemittanceController {

    private final RemittanceService remittanceService;

    public RemittanceController(RemittanceService remittanceService) {
        this.remittanceService = remittanceService;
    }

    /**
     * POST /api/v1/remittances
     * Create a new bank transfer
     */
    @PostMapping
    public ResponseEntity<RemittanceResponse> createRemittance(
            @Valid @RequestBody RemittanceRequest request) {

        RemittanceResponse response = remittanceService.createRemittance(request);

        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * GET /api/v1/remittances/{referenceNumber}
     * Track remittance status by reference number
     */
    @GetMapping("/{referenceNumber}")
    public ResponseEntity<RemittanceStatusResponse> getRemittanceStatus(
            @PathVariable String referenceNumber) {

        try {
            RemittanceStatusResponse response = remittanceService.getRemittanceStatus(referenceNumber);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/v1/remittances/user/{userId}
     * Get all remittances for a user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RemittanceStatusResponse>> getUserRemittances(
            @PathVariable Long userId) {

        List<RemittanceStatusResponse> remittances = remittanceService.getUserRemittances(userId);
        return ResponseEntity.ok(remittances);
    }

    /**
     * DELETE /api/v1/remittances/{referenceNumber}
     * Cancel a pending remittance
     */
    @DeleteMapping("/{referenceNumber}")
    public ResponseEntity<String> cancelRemittance(
            @PathVariable String referenceNumber,
            @RequestParam Long userId) {

        try {
            boolean cancelled = remittanceService.cancelRemittance(referenceNumber, userId);

            if (cancelled) {
                return ResponseEntity.ok("Remittance cancelled successfully");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}