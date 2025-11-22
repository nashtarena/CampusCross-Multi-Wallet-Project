package com.campuscross.wallet.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@CrossOrigin(origins = "*")  // Allow all origins for these endpoints
public class HealthController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        log.info("Root endpoint accessed");
        Map<String, Object> response = new HashMap<>();
        response.put("service", "CampusCross Wallet Service");
        response.put("status", "running");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("message", "Welcome to CampusCross Multi-Wallet API");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        log.info("Health endpoint accessed");
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "wallet-service");
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        log.info("Ping endpoint accessed");
        return ResponseEntity.ok("pong");
    }
}