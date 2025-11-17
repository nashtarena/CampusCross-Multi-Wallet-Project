package com.campuscross.fx_service.controller;

import com.campuscross.fx_service.model.RateAlert;
import com.campuscross.fx_service.service.RateAlertService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1/alerts")
public class RateAlertController {

    private final RateAlertService alertService;

    public RateAlertController(RateAlertService alertService) {
        this.alertService = alertService;
    }

    /**
     * POST endpoint to allow a user to create a new rate alert.
     * Developer C will call this from the frontend.
     * 
     * @param alert The RateAlert object to be saved.
     * @return The saved RateAlert with the generated ID.
     */
    @PostMapping
    public ResponseEntity<RateAlert> createAlert(@RequestBody RateAlert alert) {
        // Validation (e.g., check for currencyPair, thresholdValue, and userId
        // presence)
        if (alert.getUserId() == null || alert.getCurrencyPair() == null || alert.getThresholdValue() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // The save method in RateAlertService sets the status to ACTIVE by default
        RateAlert newAlert = alertService.save(alert);
        return new ResponseEntity<>(newAlert, HttpStatus.CREATED);
    }

    /**
     * GET endpoint to retrieve all alerts set by a specific user.
     * 
     * @param userId The ID of the user whose alerts are requested.
     * @return A list of RateAlerts for the user.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RateAlert>> getAlertsByUserId(@PathVariable Long userId) {
        List<RateAlert> alerts = alertService.findByUserId(userId);
        return ResponseEntity.ok(alerts);
    }

    /**
     * DELETE endpoint to remove a specific alert by its ID.
     * 
     * @param id The ID of the alert to delete.
     * @return HTTP 204 No Content on success.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlert(@PathVariable Long id) {
        // Note: For production, you must verify the alert belongs to the authenticated
        // user.
        Optional<RateAlert> alert = alertService.findById(id);
        if (alert.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        alertService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}