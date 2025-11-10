package com.campuscross.wallet.controller;

import com.campuscross.wallet.service.DatabasePerformanceService;
import com.campuscross.wallet.service.SystemHealthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
@Slf4j
public class SystemController {
    
    private final SystemHealthService systemHealthService;
    private final DatabasePerformanceService databasePerformanceService;
    
    /**
     * Get system health
     * GET /api/v1/system/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        log.info("Fetching system health");
        Map<String, Object> health = systemHealthService.getSystemHealth();
        return ResponseEntity.ok(health);
    }
    
    /**
     * Get system report
     * GET /api/v1/system/report
     */
    @GetMapping("/report")
    public ResponseEntity<String> getSystemReport() {
        log.info("Generating system report");
        String report = systemHealthService.getSystemReport();
        return ResponseEntity.ok(report);
    }
    
    /**
     * Get database performance report
     * GET /api/v1/system/performance
     */
    @GetMapping("/performance")
    public ResponseEntity<String> getPerformanceReport() {
        log.info("Generating database performance report");
        String report = databasePerformanceService.getPerformanceReport();
        return ResponseEntity.ok(report);
    }
    
    /**
     * Get table sizes
     * GET /api/v1/system/tables/sizes
     */
    @GetMapping("/tables/sizes")
    public ResponseEntity<List<Map<String, Object>>> getTableSizes() {
        log.info("Fetching table sizes");
        List<Map<String, Object>> sizes = databasePerformanceService.getTableSizes();
        return ResponseEntity.ok(sizes);
    }
    
    /**
     * Get cache hit ratio
     * GET /api/v1/system/cache/hit-ratio
     */
    @GetMapping("/cache/hit-ratio")
    public ResponseEntity<Map<String, Object>> getCacheHitRatio() {
        log.info("Fetching cache hit ratio");
        Map<String, Object> cacheStats = databasePerformanceService.getCacheHitRatio();
        return ResponseEntity.ok(cacheStats);
    }
}