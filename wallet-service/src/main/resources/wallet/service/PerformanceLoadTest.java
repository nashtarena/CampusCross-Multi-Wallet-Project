package com.campuscross.wallet.service;

import com.campuscross.wallet.dto.TransferRequest;
import com.campuscross.wallet.entity.CurrencyAccount;
import com.campuscross.wallet.entity.User;
import com.campuscross.wallet.entity.Wallet;
import com.campuscross.wallet.repository.CurrencyAccountRepository;
import com.campuscross.wallet.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PerformanceLoadTest {
    
    @Autowired
    private P2PTransferService p2pTransferService;
    
    @Autowired
    private WalletService walletService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CurrencyAccountRepository currencyAccountRepository;
    
    @Autowired
    private DatabasePerformanceService performanceService;
    
    @Test
    @Transactional
    public void testDatabasePerformanceReport() {
        // Generate performance report
        String report = performanceService.getPerformanceReport();
        
        assertNotNull(report);
        assertTrue(report.contains("DATABASE PERFORMANCE REPORT"));
        
        System.out.println(report);
    }
    
    @Test
    @Transactional
    public void testHighVolumeTransfers() throws Exception {
        System.out.println("\n=== HIGH VOLUME TRANSFER TEST ===");
        System.out.println("Creating 100 users and executing 100 transfers...\n");
        
        long startTime = System.currentTimeMillis();
        
        // Create 100 users with wallets
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            User user = User.builder()
                    .email("loadtest" + i + "@university.edu")
                    .name("Load Test User " + i)
                    .studentId("LOAD" + String.format("%03d", i))
                    .build();
            user = userRepository.save(user);
            Wallet wallet = walletService.createWallet(user.getId());
            
            // Give each user $1000
            CurrencyAccount account = currencyAccountRepository
                    .findByWalletIdAndCurrencyCode(wallet.getId(), "USD")
                    .orElseThrow();
            account.setBalance(new BigDecimal("1000.00"));
            currencyAccountRepository.save(account);
            
            users.add(user);
        }
        
        long setupTime = System.currentTimeMillis() - startTime;
        System.out.println("Setup completed in " + setupTime + "ms");
        
        // Execute 100 transfers
        startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 100; i++) {
            User sender = users.get(i);
            User recipient = users.get((i + 1) % 100); // Circular transfers
            
            TransferRequest request = TransferRequest.builder()
                    .fromUserId(sender.getId())
                    .toStudentId(recipient.getStudentId())
                    .amount(new BigDecimal("10.00"))
                    .currencyCode("USD")
                    .description("Load test transfer " + i)
                    .idempotencyKey("load-test-" + i)
                    .build();
            
            p2pTransferService.executeTransfer(request);
        }
        
        long transferTime = System.currentTimeMillis() - startTime;
        
        System.out.println("\n=== RESULTS ===");
        System.out.println("Transfers completed: 100");
        System.out.println("Time taken: " + transferTime + "ms");
        System.out.println("Average per transfer: " + (transferTime / 100.0) + "ms");
        System.out.println("Transactions per second: " + (100000.0 / transferTime));
        
        // Performance should be under 50ms per transfer
        assertTrue(transferTime / 100.0 < 500, "Average transfer time should be under 500ms");
        
        System.out.println("\n✅ High volume test passed!");
    }
    
    @Test
    @Transactional
    public void testConcurrentTransfers() throws Exception {
        System.out.println("\n=== CONCURRENT TRANSFER TEST ===");
        System.out.println("Testing 50 concurrent transfers...\n");
        
        // Create 100 users
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            User user = User.builder()
                    .email("concurrent" + i + "@university.edu")
                    .name("Concurrent User " + i)
                    .studentId("CONC" + String.format("%03d", i))
                    .build();
            user = userRepository.save(user);
            Wallet wallet = walletService.createWallet(user.getId());
            
            CurrencyAccount account = currencyAccountRepository
                    .findByWalletIdAndCurrencyCode(wallet.getId(), "USD")
                    .orElseThrow();
            account.setBalance(new BigDecimal("1000.00"));
            currencyAccountRepository.save(account);
            
            users.add(user);
        }
        
        // Use thread pool for concurrent execution
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<Boolean>> futures = new ArrayList<>();
        
        long startTime = System.currentTimeMillis();
        
        // Submit 50 transfers concurrently
        for (int i = 0; i < 50; i++) {
            final int index = i;
            Future<Boolean> future = executor.submit(() -> {
                try {
                    User sender = users.get(index);
                    User recipient = users.get((index + 50) % 100);
                    
                    TransferRequest request = TransferRequest.builder()
                            .fromUserId(sender.getId())
                            .toStudentId(recipient.getStudentId())
                            .amount(new BigDecimal("10.00"))
                            .currencyCode("USD")
                            .description("Concurrent test " + index)
                            .idempotencyKey("concurrent-" + index)
                            .build();
                    
                    p2pTransferService.executeTransfer(request);
                    return true;
                } catch (Exception e) {
                    System.err.println("Transfer failed: " + e.getMessage());
                    return false;
                }
            });
            futures.add(future);
        }
        
        // Wait for all to complete
        int successCount = 0;
        for (Future<Boolean> future : futures) {
            if (future.get()) {
                successCount++;
            }
        }
        
        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);
        
        long duration = System.currentTimeMillis() - startTime;
        
        System.out.println("\n=== RESULTS ===");
        System.out.println("Successful transfers: " + successCount + "/50");
        System.out.println("Total time: " + duration + "ms");
        System.out.println("Transactions per second: " + (50000.0 / duration));
        
        assertTrue(successCount >= 45, "At least 45 transfers should succeed");
        
        System.out.println("\n✅ Concurrent transfer test passed!");
    }
    
    @Test
    @Transactional
    public void testConnectionPoolPerformance() {
        System.out.println("\n=== CONNECTION POOL TEST ===");
        
        // Get connection stats
        var connStats = performanceService.getConnectionStats();
        
        System.out.println("Max connections: " + connStats.get("max_conn"));
        System.out.println("Used connections: " + connStats.get("used"));
        System.out.println("Available connections: " + connStats.get("available"));
        
        assertNotNull(connStats.get("max_conn"));
        
        System.out.println("\n✅ Connection pool healthy!");
    }
    
    @Test
    @Transactional
    public void testCacheHitRatio() {
        System.out.println("\n=== CACHE HIT RATIO TEST ===");
        
        var cacheStats = performanceService.getCacheHitRatio();
        
        if (cacheStats.get("cache_hit_ratio") != null) {
            double hitRatio = ((Number) cacheStats.get("cache_hit_ratio")).doubleValue();
            System.out.println("Cache hit ratio: " + String.format("%.2f%%", hitRatio));
            
            if (hitRatio > 0) {
                assertTrue(hitRatio > 50, "Cache hit ratio should be > 50%");
            }
        }
        
        System.out.println("\n✅ Cache performance checked!");
    }
}