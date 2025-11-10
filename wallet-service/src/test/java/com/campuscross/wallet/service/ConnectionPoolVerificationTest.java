package com.campuscross.wallet.service;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.zaxxer.hikari.HikariDataSource;

@SpringBootTest
public class ConnectionPoolVerificationTest {
    
    @Autowired
    private DataSource dataSource;
    
    @Test
    public void verifyHikariConnectionPool() {
        System.out.println("\n=== CONNECTION POOL VERIFICATION ===");
        
        assertTrue(dataSource instanceof HikariDataSource, "DataSource should be HikariCP");
        
        HikariDataSource hikariDS = (HikariDataSource) dataSource;
        
        System.out.println("Pool Name: " + hikariDS.getPoolName());
        System.out.println("Maximum Pool Size: " + hikariDS.getMaximumPoolSize());
        System.out.println("Minimum Idle: " + hikariDS.getMinimumIdle());
        System.out.println("Connection Timeout: " + hikariDS.getConnectionTimeout() + "ms");
        System.out.println("Idle Timeout: " + hikariDS.getIdleTimeout() + "ms");
        System.out.println("Max Lifetime: " + hikariDS.getMaxLifetime() + "ms");
        
        // Verify our optimizations are applied
        assertEquals(20, hikariDS.getMaximumPoolSize(), "Max pool size should be 20");
        assertEquals(5, hikariDS.getMinimumIdle(), "Min idle should be 5");
        assertEquals("WalletServicePool", hikariDS.getPoolName(), "Pool name should be WalletServicePool");
        
        System.out.println("\n✅ Connection pool properly configured!");
    }
}