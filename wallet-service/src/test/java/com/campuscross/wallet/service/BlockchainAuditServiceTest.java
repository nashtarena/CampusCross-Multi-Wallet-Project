package com.campuscross.wallet.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.campuscross.wallet.entity.BlockchainAuditChain;
import com.campuscross.wallet.entity.User;
import com.campuscross.wallet.repository.UserRepository;

@SpringBootTest
@Transactional
public class BlockchainAuditServiceTest {

    @Autowired
    private BlockchainAuditService auditService;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    public void setup() {
        testUser = User.builder()
                .email("audit.test@university.edu")
                .name("Audit Test User")
                .studentId("AUDIT_TEST_001")
                .mobileNumber("+1234567890")
                .build();
        testUser = userRepository.save(testUser);
    }

    // Helper to quickly make maps
    private Map<String, Object> mkMap(Object... keyVals) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < keyVals.length; i += 2) {
            map.put((String) keyVals[i], keyVals[i + 1]);
        }
        return map;
    }

    @Test
    public void testCreateAuditBlock() {
        Map<String, Object> eventData = mkMap(
                "walletId", 123L,
                "action", "CREATED",
                "currencies", List.of("USD", "EUR", "GBP")
        );

        BlockchainAuditChain block = auditService.createAuditBlock(
                BlockchainAuditService.EVENT_WALLET_CREATED,
                "WALLET",
                123L,
                testUser.getId(),
                eventData
        );

        assertNotNull(block);
        assertNotNull(block.getId());
        assertTrue(block.getBlockNumber() > 0);
        assertEquals("WALLET_CREATED", block.getEventType());
        assertEquals(64, block.getCurrentHash().length());
        assertEquals(64, block.getPreviousHash().length());
        assertEquals(64, block.getMerkleRoot().length());
    }

    @Test
    public void testChainLinking() {
        BlockchainAuditChain block1 = auditService.createAuditBlock("TEST_EVENT", "TEST", 1L, testUser.getId(), mkMap("event", "first"));
        BlockchainAuditChain block2 = auditService.createAuditBlock("TEST_EVENT", "TEST", 2L, testUser.getId(), mkMap("event", "second"));

        assertEquals(block1.getBlockNumber() + 1, block2.getBlockNumber());
        assertEquals(block1.getCurrentHash(), block2.getPreviousHash());
    }

    @Test
    public void testAuditWalletCreation() {
        BlockchainAuditChain block = auditService.auditWalletCreation(999L, testUser.getId(), mkMap("initialCurrencies", List.of("USD","EUR"), "status","ACTIVE"));

        assertNotNull(block);
        assertEquals("WALLET_CREATED", block.getEventType());
        assertEquals("WALLET", block.getEntityType());
        assertEquals(999L, block.getEntityId());
    }

    @Test
    public void testAuditTransaction() {
        BlockchainAuditChain block = auditService.auditTransaction(555L, testUser.getId(), "P2P", "100.00", "USD", "COMPLETED");

        assertNotNull(block);
        assertEquals("TRANSACTION_CREATED", block.getEventType());
        assertEquals("P2P", block.getEventData().get("transactionType"));
    }

    @Test
    public void testGetAuditTrail() {
        for (int i = 0; i < 3; i++)
            auditService.createAuditBlock("TEST_EVENT", "WALLET", 777L, testUser.getId(), mkMap("action", "update_" + i));

        List<BlockchainAuditChain> trail = auditService.getAuditTrail("WALLET", 777L);
        assertEquals(3, trail.size());
    }

    @Test
    public void testGetChainStatistics() {
        for (int i = 0; i < 5; i++)
            auditService.createAuditBlock("TEST_EVENT", "TEST", (long)i, testUser.getId(), mkMap("test", "block_" + i));

        Map<String,Object> stats = auditService.getChainStatistics();
        assertNotNull(stats);
        assertTrue((Long) stats.get("totalBlocks") >= 5);
        assertEquals("HEALTHY", stats.get("chainHealth"));
    }

    @Test
    public void testGetLatestBlock() {
        BlockchainAuditChain latest = auditService.getLatestBlock();
        assertNotNull(latest);
        assertTrue(latest.getBlockNumber() >= 0);
    }

    @Test
    public void testProofOfWork() {
        BlockchainAuditChain block = auditService.createAuditBlockWithProofOfWork("TEST_POW", "TEST", 999L, testUser.getId(), mkMap("test", "proof_of_work"), 2);

        assertNotNull(block);
        assertTrue(block.getCurrentHash().startsWith("00"));
        assertTrue(block.getNonce() > 0);
        assertTrue(block.getMetadata().containsKey("miningTimeMs"));
    }

    @Test
    public void testBalanceUpdateAudit() {
        BlockchainAuditChain block = auditService.auditBalanceUpdate(100L, testUser.getId(), "USD", "100.00", "150.00", "Deposit");

        assertNotNull(block);
        assertEquals("BALANCE_UPDATED", block.getEventType());
        assertEquals("CURRENCY_ACCOUNT", block.getEntityType());
        assertEquals(100L, block.getEntityId());
        assertTrue(block.getEventData().containsKey("oldBalance"));
        assertTrue(block.getEventData().containsKey("newBalance"));
    }

    @Test
    public void testConversionAudit() {
        BlockchainAuditChain block = auditService.auditConversion(200L, testUser.getId(), "USD", "EUR", "100.00", "92.00", "0.92");

        assertNotNull(block);
        assertEquals("CONVERSION_EXECUTED", block.getEventType());
        assertTrue(block.getEventData().containsKey("fromCurrency"));
        assertTrue(block.getEventData().containsKey("toCurrency"));
        assertTrue(block.getEventData().containsKey("exchangeRate"));
    }

    @Test
    public void testDisbursementAudit() {
        BlockchainAuditChain block = auditService.auditDisbursement(300L, testUser.getId(), "COMPLETED", 50, "10000.00", "USD");

        assertNotNull(block);
        assertEquals("DISBURSEMENT_CREATED", block.getEventType());
        assertEquals("DISBURSEMENT_BATCH", block.getEntityType());
        assertTrue(block.getEventData().containsKey("totalCount"));
        assertTrue(block.getEventData().containsKey("totalAmount"));
    }

    @Test
    public void testRiskScoreAudit() {
        BlockchainAuditChain block = auditService.auditRiskScore(400L, 500L, testUser.getId(), "HIGH", "75.50", "Large amount; High velocity");

        assertNotNull(block);
        assertEquals("RISK_SCORE_CREATED", block.getEventType());
        assertTrue(block.getEventData().containsKey("riskLevel"));
        assertTrue(block.getEventData().containsKey("factors"));
    }

    @Test
    public void testGetUserAuditTrail() {
        for (int i = 0; i < 3; i++)
            auditService.createAuditBlock("USER_ACTION", "USER", testUser.getId(), testUser.getId(), mkMap("userAction", "action_" + i));

        List<BlockchainAuditChain> trail = auditService.getUserAuditTrail(testUser.getId());
        assertTrue(trail.size() >= 3);
    }
}
