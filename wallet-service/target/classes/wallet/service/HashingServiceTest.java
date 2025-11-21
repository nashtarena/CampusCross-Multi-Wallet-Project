package com.campuscross.wallet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class HashingServiceTest {
    
    @Autowired
    private HashingService hashingService;
    
    @Test
    public void testCalculateBlockHash() {
        // Create test data
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("action", "TRANSFER");
        eventData.put("amount", "100.00");
        eventData.put("currency", "USD");
        
        LocalDateTime timestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        
        // Calculate hash
        String hash = hashingService.calculateBlockHash(
                1L,
                "0000000000000000000000000000000000000000000000000000000000000000",
                eventData,
                timestamp,
                0L
        );
        
        // Verify
        assertNotNull(hash);
        assertEquals(64, hash.length()); // SHA-256 = 64 hex characters
        
        System.out.println("✅ Block hash generated: " + hash);
    }
    
    @Test
    public void testHashConsistency() {
        // Same input should produce same hash
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("test", "data");
        
        LocalDateTime timestamp = LocalDateTime.now();
        
        String hash1 = hashingService.calculateBlockHash(1L, "prev", eventData, timestamp, 0L);
        String hash2 = hashingService.calculateBlockHash(1L, "prev", eventData, timestamp, 0L);
        
        assertEquals(hash1, hash2);
        
        System.out.println("✅ Hash consistency verified!");
    }
    
    @Test
    public void testHashUniqueness() {
        // Different input should produce different hash
        Map<String, Object> eventData1 = new HashMap<>();
        eventData1.put("amount", "100");
        
        Map<String, Object> eventData2 = new HashMap<>();
        eventData2.put("amount", "101"); // Just 1 different!
        
        LocalDateTime timestamp = LocalDateTime.now();
        
        String hash1 = hashingService.calculateBlockHash(1L, "prev", eventData1, timestamp, 0L);
        String hash2 = hashingService.calculateBlockHash(1L, "prev", eventData2, timestamp, 0L);
        
        assertNotEquals(hash1, hash2);
        
        System.out.println("✅ Hash uniqueness verified!");
        System.out.println("   Hash 1: " + hash1);
        System.out.println("   Hash 2: " + hash2);
    }
    
    @Test
    public void testVerifyBlockHash() {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("action", "TEST");
        
        LocalDateTime timestamp = LocalDateTime.now();
        
        String hash = hashingService.calculateBlockHash(1L, "prev", eventData, timestamp, 0L);
        
        // Verify correct hash
        boolean isValid = hashingService.verifyBlockHash(hash, 1L, "prev", eventData, timestamp, 0L);
        assertTrue(isValid);
        
        // Verify wrong hash
        boolean isInvalid = hashingService.verifyBlockHash("wrong_hash", 1L, "prev", eventData, timestamp, 0L);
        assertFalse(isInvalid);
        
        System.out.println("✅ Hash verification working!");
    }
    
    @Test
    public void testMerkleRoot() {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("transaction1", "data1");
        eventData.put("transaction2", "data2");
        
        String merkleRoot = hashingService.calculateMerkleRoot(eventData);
        
        assertNotNull(merkleRoot);
        assertEquals(64, merkleRoot.length());
        
        System.out.println("✅ Merkle root calculated: " + merkleRoot);
    }
}