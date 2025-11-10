package com.campuscross.wallet.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemHealthService {
    
    private final JdbcTemplate jdbcTemplate;
    private final WalletService walletService;
    private final P2PTransferService p2pTransferService;
    private final CurrencyConversionService conversionService;
    private final FraudDetectionService fraudDetectionService;
    private final BulkDisbursementService disbursementService;
    
    /**
     * Comprehensive system health check
     */
    public Map<String, Object> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Database connectivity
            health.put("database", checkDatabaseHealth());
            
            // Table counts
            health.put("statistics", getSystemStatistics());
            
            // Service availability
            health.put("services", checkServicesHealth());
            
            health.put("status", "HEALTHY");
            health.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            log.error("System health check failed", e);
            health.put("status", "UNHEALTHY");
            health.put("error", e.getMessage());
        }
        
        return health;
    }
    
    /**
     * Check database health
     */
    private Map<String, Object> checkDatabaseHealth() {
        Map<String, Object> dbHealth = new HashMap<>();
        
        try {
            // Test query
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            dbHealth.put("status", "UP");
            dbHealth.put("responseTime", measureDatabaseResponseTime());
            
        } catch (Exception e) {
            dbHealth.put("status", "DOWN");
            dbHealth.put("error", e.getMessage());
        }
        
        return dbHealth;
    }
    
    /**
     * Measure database response time
     */
    private long measureDatabaseResponseTime() {
        long start = System.currentTimeMillis();
        jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        return System.currentTimeMillis() - start;
    }
    
    /**
     * Get system statistics
     */
    private Map<String, Object> getSystemStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            stats.put("totalUsers", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users", Long.class));
            
            stats.put("totalWallets", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM wallets", Long.class));
            
            stats.put("activeWallets", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM wallets WHERE status = 'active'", Long.class));
            
            stats.put("totalTransactions", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM transactions", Long.class));
            
            stats.put("completedTransactions", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM transactions WHERE status = 'completed'", Long.class));
            
            stats.put("pendingTransactions", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM transactions WHERE status = 'pending'", Long.class));
            
            stats.put("highRiskTransactions", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM risk_scores WHERE risk_level IN ('HIGH', 'CRITICAL')", Long.class));
            
            stats.put("totalDisbursementBatches", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM disbursement_batches", Long.class));
            
        } catch (Exception e) {
            log.error("Failed to get system statistics", e);
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }
    
    /**
     * Check all services are operational
     */
    private Map<String, String> checkServicesHealth() {
        Map<String, String> services = new HashMap<>();
        
        services.put("walletService", walletService != null ? "UP" : "DOWN");
        services.put("p2pTransferService", p2pTransferService != null ? "UP" : "DOWN");
        services.put("conversionService", conversionService != null ? "UP" : "DOWN");
        services.put("fraudDetectionService", fraudDetectionService != null ? "UP" : "DOWN");
        services.put("disbursementService", disbursementService != null ? "UP" : "DOWN");
        
        return services;
    }
    
    /**
     * Get detailed system report
     */
    public String getSystemReport() {
        StringBuilder report = new StringBuilder();
        report.append("\n========================================\n");
        report.append("       CAMPUSCROSS WALLET SYSTEM        \n");
        report.append("            HEALTH REPORT               \n");
        report.append("========================================\n\n");
        
        Map<String, Object> health = getSystemHealth();
        
        report.append("STATUS: ").append(health.get("status")).append("\n\n");
        
        // Database
        @SuppressWarnings("unchecked")
        Map<String, Object> db = (Map<String, Object>) health.get("database");
        report.append("DATABASE:\n");
        report.append("  Status: ").append(db.get("status")).append("\n");
        report.append("  Response Time: ").append(db.get("responseTime")).append("ms\n\n");
        
        // Statistics
        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) health.get("statistics");
        report.append("STATISTICS:\n");
        report.append("  Total Users: ").append(stats.get("totalUsers")).append("\n");
        report.append("  Total Wallets: ").append(stats.get("totalWallets")).append("\n");
        report.append("  Active Wallets: ").append(stats.get("activeWallets")).append("\n");
        report.append("  Total Transactions: ").append(stats.get("totalTransactions")).append("\n");
        report.append("  Completed: ").append(stats.get("completedTransactions")).append("\n");
        report.append("  Pending: ").append(stats.get("pendingTransactions")).append("\n");
        report.append("  High Risk: ").append(stats.get("highRiskTransactions")).append("\n");
        report.append("  Disbursement Batches: ").append(stats.get("totalDisbursementBatches")).append("\n\n");
        
        // Services
        @SuppressWarnings("unchecked")
        Map<String, String> services = (Map<String, String>) health.get("services");
        report.append("SERVICES:\n");
        services.forEach((name, status) -> 
            report.append("  ").append(name).append(": ").append(status).append("\n"));
        
        report.append("\n========================================\n");
        
        return report.toString();
    }
}