package com.campuscross.wallet.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.campuscross.wallet.dto.ConversionRequest;
import com.campuscross.wallet.entity.CurrencyAccount;
import com.campuscross.wallet.entity.Transaction;
import com.campuscross.wallet.entity.User;
import com.campuscross.wallet.entity.Wallet;
import com.campuscross.wallet.repository.CurrencyAccountRepository;
import com.campuscross.wallet.repository.UserRepository;

@SpringBootTest
@Transactional
public class CurrencyConversionServiceTest {
    
    @Autowired
    private CurrencyConversionService conversionService;
    
    @Autowired
    private WalletService walletService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CurrencyAccountRepository currencyAccountRepository;
    
    @Test
    public void testCurrencyConversion_USDtoEUR_Success() {
        // Create user with wallet
        User user = User.builder()
                .email("converter@university.edu")
                .name("Currency Converter")
                .studentId("CONV001")
                .build();
        user = userRepository.save(user);
        Wallet wallet = walletService.createWallet(user.getId());
        
        // Give user $1000 USD
        CurrencyAccount usdAccount = currencyAccountRepository
                .findByWalletIdAndCurrencyCode(wallet.getId(), "USD")
                .orElseThrow();
        usdAccount.setBalance(new BigDecimal("1000.00"));
        currencyAccountRepository.save(usdAccount);
        
        // Create conversion request: Convert $500 USD to EUR at rate 0.92
        ConversionRequest request = ConversionRequest.builder()
                .userId(user.getId())
                .fromCurrency("USD")
                .toCurrency("EUR")
                .amount(new BigDecimal("500.00"))
                .exchangeRate(new BigDecimal("0.92")) // 1 USD = 0.92 EUR
                .idempotencyKey("conversion-test-001")
                .build();
        
        // Execute conversion
        Transaction transaction = conversionService.executeConversion(request);
        
        // Verify transaction
        assertNotNull(transaction);
        assertEquals("CONVERSION", transaction.getTransactionType());
        assertEquals("completed", transaction.getStatus());
        assertEquals(0, new BigDecimal("500.00").compareTo(transaction.getAmount()));
        
        // Verify USD balance reduced (normalize to 2 decimal places)
        BigDecimal usdBalance = walletService.getBalanceByCurrency(wallet.getId(), "USD")
                .setScale(2, RoundingMode.HALF_UP);
        assertEquals(0, new BigDecimal("500.00").compareTo(usdBalance));
        
        // Verify EUR balance increased (normalize to 2 decimal places)
        BigDecimal eurBalance = walletService.getBalanceByCurrency(wallet.getId(), "EUR")
                .setScale(2, RoundingMode.HALF_UP);
        assertEquals(0, new BigDecimal("460.00").compareTo(eurBalance)); // 500 * 0.92 = 460
        
        System.out.println("✅ Currency conversion successful!");
        System.out.println("   USD balance: $" + usdBalance);
        System.out.println("   EUR balance: €" + eurBalance);
        System.out.println("   Transaction ID: " + transaction.getId());
    }
    
    @Test
    public void testCurrencyConversion_InsufficientBalance() {
        // Create user with wallet
        User user = User.builder()
                .email("poor.converter@university.edu")
                .name("Poor Converter")
                .studentId("POOR002")
                .build();
        user = userRepository.save(user);
        Wallet wallet = walletService.createWallet(user.getId());
        
        // Give user only $50 USD
        CurrencyAccount usdAccount = currencyAccountRepository
                .findByWalletIdAndCurrencyCode(wallet.getId(), "USD")
                .orElseThrow();
        usdAccount.setBalance(new BigDecimal("50.00"));
        currencyAccountRepository.save(usdAccount);
        
        // Try to convert $100 USD (more than available)
        ConversionRequest request = ConversionRequest.builder()
                .userId(user.getId())
                .fromCurrency("USD")
                .toCurrency("EUR")
                .amount(new BigDecimal("100.00"))
                .exchangeRate(new BigDecimal("0.92"))
                .build();
        
        // Should throw exception
        Exception exception = assertThrows(RuntimeException.class, () -> {
            conversionService.executeConversion(request);
        });
        
        assertTrue(exception.getMessage().contains("Insufficient balance"));
        System.out.println("✅ Insufficient balance validation working!");
    }
    
    @Test
    public void testCurrencyConversion_SameCurrencyFails() {
        // Create user with wallet
        User user = User.builder()
                .email("same.currency@university.edu")
                .name("Same Currency User")
                .studentId("SAME001")
                .build();
        user = userRepository.save(user);
        walletService.createWallet(user.getId());
        
        // Try to convert USD to USD (invalid)
        ConversionRequest request = ConversionRequest.builder()
                .userId(user.getId())
                .fromCurrency("USD")
                .toCurrency("USD")
                .amount(new BigDecimal("100.00"))
                .exchangeRate(new BigDecimal("1.00"))
                .build();
        
        // Should throw exception
        Exception exception = assertThrows(RuntimeException.class, () -> {
            conversionService.executeConversion(request);
        });
        
        assertTrue(exception.getMessage().contains("Source and target currencies must be different"));
        System.out.println("✅ Same currency validation working!");
    }
    
    @Test
    public void testConversionPreview() {
        // Preview conversion without executing
        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal exchangeRate = new BigDecimal("0.85");
        
        BigDecimal preview = conversionService.previewConversion(amount, exchangeRate)
                .setScale(2, RoundingMode.HALF_UP);
        
        assertEquals(0, new BigDecimal("85.00").compareTo(preview));
        System.out.println("✅ Conversion preview working! $100 USD = €" + preview + " EUR");
    }
    
    @Test
    public void testConversionHistory() {
        // Create user with wallet
        User user = User.builder()
                .email("history@university.edu")
                .name("History User")
                .studentId("HIST001")
                .build();
        user = userRepository.save(user);
        Wallet wallet = walletService.createWallet(user.getId());
        
        // Give user $1000 USD
        CurrencyAccount usdAccount = currencyAccountRepository
                .findByWalletIdAndCurrencyCode(wallet.getId(), "USD")
                .orElseThrow();
        usdAccount.setBalance(new BigDecimal("1000.00"));
        currencyAccountRepository.save(usdAccount);
        
        // Execute two conversions
        ConversionRequest request1 = ConversionRequest.builder()
                .userId(user.getId())
                .fromCurrency("USD")
                .toCurrency("EUR")
                .amount(new BigDecimal("200.00"))
                .exchangeRate(new BigDecimal("0.92"))
                .build();
        conversionService.executeConversion(request1);
        
        ConversionRequest request2 = ConversionRequest.builder()
                .userId(user.getId())
                .fromCurrency("USD")
                .toCurrency("GBP")
                .amount(new BigDecimal("300.00"))
                .exchangeRate(new BigDecimal("0.79"))
                .build();
        conversionService.executeConversion(request2);
        
        // Get conversion history
        List<Transaction> history = conversionService.getConversionHistory(user.getId());
        
        // Verify
        assertNotNull(history);
        assertEquals(2, history.size());
        
        System.out.println("✅ Conversion history working! Found " + history.size() + " conversions");
    }
}