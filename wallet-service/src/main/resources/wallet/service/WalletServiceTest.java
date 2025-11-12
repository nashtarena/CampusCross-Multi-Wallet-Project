package com.campuscross.wallet.service;

import com.campuscross.wallet.entity.User;
import com.campuscross.wallet.entity.Wallet;
import com.campuscross.wallet.repository.UserRepository;
import com.campuscross.wallet.service.WalletService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class WalletServiceTest {
    
    @Autowired
    private WalletService walletService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    public void testCreateWallet_Success() {
        // Create a test user first
        User user = User.builder()
                .email("test@university.edu")
                .name("Test User")
                .studentId("TEST001")
                .mobileNumber("+1234567890")
                .build();
        user = userRepository.save(user);
        
        // Create wallet
        Wallet wallet = walletService.createWallet(user.getId());
        
        // Verify wallet was created
        assertNotNull(wallet);
        assertNotNull(wallet.getId());
        assertEquals(user.getId(), wallet.getUserId());
        assertEquals("active", wallet.getStatus());
        
        System.out.println("✅ Wallet created successfully with ID: " + wallet.getId());
    }
    
    @Test
    public void testGetBalance_Success() {
        // Create a test user
        User user = User.builder()
                .email("balance@university.edu")
                .name("Balance Test User")
                .studentId("BAL001")
                .build();
        user = userRepository.save(user);
        
        // Create wallet
        Wallet wallet = walletService.createWallet(user.getId());
        
        // Get all balances
        Map<String, BigDecimal> balances = walletService.getBalance(wallet.getId());
        
        // Verify all currencies are present with zero balance
        assertNotNull(balances);
        assertEquals(4, balances.size()); // USD, EUR, GBP, JPY
        assertTrue(balances.containsKey("USD"));
        assertTrue(balances.containsKey("EUR"));
        assertTrue(balances.containsKey("GBP"));
        assertTrue(balances.containsKey("JPY"));
        
        assertEquals(BigDecimal.ZERO.compareTo(balances.get("USD")), 0);
        
        System.out.println("✅ Balance check successful: " + balances);
    }
    
    @Test
    public void testGetBalanceByCurrency_Success() {
        // Create a test user
        User user = User.builder()
                .email("currency@university.edu")
                .name("Currency Test User")
                .studentId("CUR001")
                .build();
        user = userRepository.save(user);
        
        // Create wallet
        Wallet wallet = walletService.createWallet(user.getId());
        
        // Get USD balance
        BigDecimal usdBalance = walletService.getBalanceByCurrency(wallet.getId(), "USD");
        
        // Verify
        assertNotNull(usdBalance);
        assertEquals(BigDecimal.ZERO.compareTo(usdBalance), 0);
        
        System.out.println("✅ USD balance check successful: " + usdBalance);
    }
}