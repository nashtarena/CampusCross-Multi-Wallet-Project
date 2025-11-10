package com.campuscross.wallet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatabasePerformanceService {
    
    private final JdbcTemplate jdbcTemplate;
    
    /**
     * Get table sizes to identify large tables
     */
    public List<Map<String, Object>> getTableSizes() {
        String sql = """
            SELECT 
                schemaname as schema,
                tablename as table_name,
                pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size,
                pg_total_relation_size(schemaname||'.'||tablename) as size_bytes
            FROM pg_tables
            WHERE schemaname = 'public'
            ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC
            """;
        
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
        log.info("Table sizes retrieved: {} tables", results.size());
        return results;
    }
    
    /**
     * Get index usage statistics
     */
    public List<Map<String, Object>> getIndexUsage() {
        String sql = """
            SELECT 
                schemaname,
                tablename,
                indexname,
                idx_scan as index_scans,
                idx_tup_read as tuples_read,
                idx_tup_fetch as tuples_fetched
            FROM pg_stat_user_indexes
            WHERE schemaname = 'public'
            ORDER BY idx_scan DESC
            """;
        
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
        log.info("Index usage statistics retrieved");
        return results;
    }
    
    /**
     * Get unused indexes (candidates for removal)
     */
    public List<Map<String, Object>> getUnusedIndexes() {
        String sql = """
            SELECT 
                schemaname,
                tablename,
                indexname,
                pg_size_pretty(pg_relation_size(indexrelid)) as index_size
            FROM pg_stat_user_indexes
            WHERE schemaname = 'public'
            AND idx_scan = 0
            AND indexname NOT LIKE '%_pkey'
            ORDER BY pg_relation_size(indexrelid) DESC
            """;
        
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
        log.info("Unused indexes found: {}", results.size());
        return results;
    }
    
    /**
     * Get slow query statistics (queries taking longer than expected)
     */
    public List<Map<String, Object>> getTableStats() {
        String sql = """
            SELECT 
                schemaname,
                relname as table_name,
                seq_scan as sequential_scans,
                seq_tup_read as seq_tuples_read,
                idx_scan as index_scans,
                idx_tup_fetch as idx_tuples_fetched,
                n_tup_ins as inserts,
                n_tup_upd as updates,
                n_tup_del as deletes
            FROM pg_stat_user_tables
            WHERE schemaname = 'public'
            ORDER BY seq_scan DESC
            """;
        
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
        log.info("Table statistics retrieved");
        return results;
    }
    
    /**
     * Get database connection statistics
     */
    public Map<String, Object> getConnectionStats() {
        String sql = """
            SELECT 
                max_conn,
                used,
                res_for_super,
                max_conn - used - res_for_super as available
            FROM 
                (SELECT count(*) used FROM pg_stat_activity) t1,
                (SELECT setting::int res_for_super FROM pg_settings WHERE name='superuser_reserved_connections') t2,
                (SELECT setting::int max_conn FROM pg_settings WHERE name='max_connections') t3
            """;
        
        Map<String, Object> result = jdbcTemplate.queryForMap(sql);
        log.info("Connection stats: {}", result);
        return result;
    }
    
    /**
     * Get cache hit ratio (should be > 95%)
     */
    public Map<String, Object> getCacheHitRatio() {
        String sql = """
            SELECT 
                sum(heap_blks_read) as heap_read,
                sum(heap_blks_hit) as heap_hit,
                sum(heap_blks_hit) / NULLIF(sum(heap_blks_hit) + sum(heap_blks_read), 0) * 100 as cache_hit_ratio
            FROM pg_statio_user_tables
            """;
        
        Map<String, Object> result = jdbcTemplate.queryForMap(sql);
        log.info("Cache hit ratio: {}%", result.get("cache_hit_ratio"));
        return result;
    }
    
    /**
     * Analyze a specific table (updates statistics)
     */
    public void analyzeTable(String tableName) {
        String sql = "ANALYZE " + tableName;
        jdbcTemplate.execute(sql);
        log.info("Table analyzed: {}", tableName);
    }
    
    /**
     * Vacuum a specific table (reclaim storage)
     */
    public void vacuumTable(String tableName) {
        String sql = "VACUUM " + tableName;
        jdbcTemplate.execute(sql);
        log.info("Table vacuumed: {}", tableName);
    }
    
    /**
     * Get comprehensive performance report
     */
    public String getPerformanceReport() {
        StringBuilder report = new StringBuilder();
        report.append("\n=== DATABASE PERFORMANCE REPORT ===\n\n");
        
        // Table sizes
        report.append("TABLE SIZES:\n");
        List<Map<String, Object>> tableSizes = getTableSizes();
        for (Map<String, Object> row : tableSizes) {
            report.append(String.format("  %s: %s\n", row.get("table_name"), row.get("size")));
        }
        
        // Cache hit ratio
        report.append("\nCACHE HIT RATIO:\n");
        Map<String, Object> cacheStats = getCacheHitRatio();
        report.append(String.format("  %.2f%% (should be > 95%%)\n", cacheStats.get("cache_hit_ratio")));
        
        // Connection stats
        report.append("\nCONNECTION STATS:\n");
        Map<String, Object> connStats = getConnectionStats();
        report.append(String.format("  Max: %s, Used: %s, Available: %s\n", 
            connStats.get("max_conn"), connStats.get("used"), connStats.get("available")));
        
        // Unused indexes
        report.append("\nUNUSED INDEXES:\n");
        List<Map<String, Object>> unusedIndexes = getUnusedIndexes();
        if (unusedIndexes.isEmpty()) {
            report.append("  All indexes are being used\n");
        } else {
            for (Map<String, Object> row : unusedIndexes) {
                report.append(String.format("  %s.%s (%s)\n", 
                    row.get("tablename"), row.get("indexname"), row.get("index_size")));
            }
        }
        
        report.append("\n=================================\n");
        
        log.info(report.toString());
        return report.toString();
    }
}