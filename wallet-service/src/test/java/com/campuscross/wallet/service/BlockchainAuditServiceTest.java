package com.campuscross.wallet.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.campuscross.wallet.entity.BlockchainAuditChain;
import com.campuscross.wallet.repository.UserRepository;

@SpringBootTest
@Transactional
public class BlockchainAuditServiceTest {
    
    @Autowired
    private BlockchainAuditService auditService;

    @Autowired
    private UserRepository userRepository;

    
    
    @Test
    public void testCreateAuditBlock() {
        // Create test event data
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("walletId", 123L);
        eventData.put("action", "CREATED");
        eventData.put("currencies", List.of("USD", "EUR", "GBP"));
        
        // Create audit block
        BlockchainAuditChain block = auditService.createAuditBlock(
                BlockchainAuditService.EVENT_WALLET_CREATED,
                "WALLET",
                123L,
                456L,
                eventData
        );
        
        // Verify
        assertNotNull(block);
        assertNotNull(block.getId());
        assertTrue(block.getBlockNumber() > 0); // Should be after genesis block
        assertEquals("WALLET_CREATED", block.getEventType());
        assertEquals(64, block.getCurrentHash().length()); // SHA-256
        assertEquals(64, block.getPreviousHash().length());
        assertEquals(64, block.getMerkleRoot().length());
        
        System.out.println("✅ Audit block created!");
        System.out.println("   Block Number: " + block.getBlockNumber());
        System.out.println("   Hash: " + block.getCurrentHash());
        System.out.println("   Previous Hash: " + block.getPreviousHash());
    }
    
    @Test
    public void testChainLinking() {
        // Create first block
        Map<String, Object> data1 = new HashMap<>();
        data1.put("event", "first");
        
        BlockchainAuditChain block1 = auditService.createAuditBlock(
                "TEST_EVENT",
                "TEST",
                1L,
                1L,
                data1
        );
        
        // Create second block
        Map<String, Object> data2 = new HashMap<>();
        data2.put("event", "second");
        
        BlockchainAuditChain block2 = auditService.createAuditBlock(
                "TEST_EVENT",
                "TEST",
                2L,
                1L,
                data2
        );
        
        // Verify chain linking
        assertEquals(block1.getBlockNumber() + 1, block2.getBlockNumber());
        assertEquals(block1.getCurrentHash(), block2.getPreviousHash());
        
        System.out.println("✅ Chain linking verified!");
        System.out.println("   Block 1: " + block1.getBlockNumber() + " → " + block1.getCurrentHash());
        System.out.println("   Block 2: " + block2.getBlockNumber() + " → " + block2.getCurrentHash());
        System.out.println("   Block 2 links to Block 1: " + block2.getPreviousHash().equals(block1.getCurrentHash()));
    }
    
    @Test
    public void testAuditWalletCreation() {
        Map<String, Object> details = new HashMap<>();
        details.put("initialCurrencies", List.of("USD", "EUR"));
        details.put("status", "ACTIVE");
        
        BlockchainAuditChain block = auditService.auditWalletCreation(999L, 123L, details);
        
        assertNotNull(block);
        assertEquals("WALLET_CREATED", block.getEventType());
        assertEquals("WALLET", block.getEntityType());
        assertEquals(999L, block.getEntityId());
        
        System.out.println("✅ Wallet creation audited!");
    }
    
    @Test
    public void testAuditTransaction() {
        BlockchainAuditChain block = auditService.auditTransaction(
                555L,
                123L,
                "P2P",
                "100.00",
                "USD",
                "COMPLETED"
        );
        
        assertNotNull(block);
        assertEquals("TRANSACTION_CREATED", block.getEventType());
        assertTrue(block.getEventData().containsKey("transactionType"));
        assertEquals("P2P", block.getEventData().get("transactionType"));
        
        System.out.println("✅ Transaction audited!");
    }
    
    @Test
    public void testGetAuditTrail() {
        // Create multiple audit blocks for same entity
        for (int i = 0; i < 3; i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("action", "update_" + i);
            
            auditService.createAuditBlock(
                    "TEST_EVENT",
                    "WALLET",
                    777L, // Same entity ID
                    123L,
                    data
            );
        }
        
        // Get audit trail
        List<BlockchainAuditChain> trail = auditService.getAuditTrail("WALLET", 777L);
        
        assertEquals(3, trail.size());
        
        System.out.println("✅ Audit trail retrieved: " + trail.size() + " blocks");
    }
    
    @Test
    public void testGetChainStatistics() {
        // Create a few blocks
        for (int i = 0; i < 5; i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("test", "block_" + i);
            
            auditService.createAuditBlock(
                    "TEST_EVENT",
                    "TEST",
                    (long) i,
                    1L,
                    data
            );
        }
        
        // Get statistics
        Map<String, Object> stats = auditService.getChainStatistics();
        
        assertNotNull(stats);
        assertTrue((Long) stats.get("totalBlocks") >= 5);
        assertEquals("HEALTHY", stats.get("chainHealth"));
        
        System.out.println("✅ Chain statistics:");
        stats.forEach((key, value) -> System.out.println("   " + key + ": " + value));
    }
    
    @Test
    public void testGetLatestBlock() {
        BlockchainAuditChain latest = auditService.getLatestBlock();
        
        assertNotNull(latest);
        assertTrue(latest.getBlockNumber() >= 0);
        
        System.out.println("✅ Latest block: #" + latest.getBlockNumber());
    }
}