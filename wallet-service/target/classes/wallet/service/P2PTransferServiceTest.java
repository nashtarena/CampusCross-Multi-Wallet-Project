package com.campuscross.wallet.service;

import com.campuscross.wallet.dto.TransferRequest;
import com.campuscross.wallet.entity.CurrencyAccount;
import com.campuscross.wallet.entity.Transaction;
import com.campuscross.wallet.entity.User;
import com.campuscross.wallet.entity.Wallet;
import com.campuscross.wallet.repository.CurrencyAccountRepository;
import com.campuscross.wallet.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class P2PTransferServiceTest {
    
    @Autowired
    private P2PTransferService p2pTransferService;
    
    @Autowired
    private WalletService walletService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CurrencyAccountRepository currencyAccountRepository;
    
    @Test
    public void testP2PTransfer_Success() {
        // Create sender
        User sender = User.builder()
                .email("sender@university.edu")
                .name("John Sender")
                .studentId("SEND001")
                .mobileNumber("+1111111111")
                .build();
        sender = userRepository.save(sender);
        Wallet senderWallet = walletService.createWallet(sender.getId());
        
        // Create recipient
        User recipient = User.builder()
                .email("recipient@university.edu")
                .name("Jane Recipient")
                .studentId("RECV001")
                .mobileNumber("+2222222222")
                .build();
        recipient = userRepository.save(recipient);
        Wallet recipientWallet = walletService.createWallet(recipient.getId());
        
        // Give sender some USD balance
        CurrencyAccount senderUsdAccount = currencyAccountRepository
                .findByWalletIdAndCurrencyCode(senderWallet.getId(), "USD")
                .orElseThrow();
        senderUsdAccount.setBalance(new BigDecimal("1000.00"));
        currencyAccountRepository.save(senderUsdAccount);
        
        // Create transfer request
        TransferRequest request = TransferRequest.builder()
                .fromUserId(sender.getId())
                .toStudentId(recipient.getStudentId())
                .amount(new BigDecimal("250.00"))
                .currencyCode("USD")
                .description("Test transfer")
                .idempotencyKey("test-transfer-001")
                .build();
        
        // Execute transfer
        Transaction transaction = p2pTransferService.executeTransfer(request);
        
        // Verify transaction
        assertNotNull(transaction);
        assertEquals("P2P", transaction.getTransactionType());
        assertEquals("completed", transaction.getStatus());
        assertEquals(0, new BigDecimal("250.00").compareTo(transaction.getAmount()));
        
        // Verify sender balance (normalize to 2 decimal places)
        BigDecimal senderBalance = walletService.getBalanceByCurrency(senderWallet.getId(), "USD")
                .setScale(2, RoundingMode.HALF_UP);
        assertEquals(0, new BigDecimal("750.00").compareTo(senderBalance));
        
        // Verify recipient balance (normalize to 2 decimal places)
        BigDecimal recipientBalance = walletService.getBalanceByCurrency(recipientWallet.getId(), "USD")
                .setScale(2, RoundingMode.HALF_UP);
        assertEquals(0, new BigDecimal("250.00").compareTo(recipientBalance));
        
        System.out.println("✅ P2P Transfer successful!");
        System.out.println("   Sender balance: $" + senderBalance);
        System.out.println("   Recipient balance: $" + recipientBalance);
        System.out.println("   Transaction ID: " + transaction.getId());
    }
    
    @Test
    public void testP2PTransfer_InsufficientBalance() {
        // Create sender with low balance
        User sender = User.builder()
                .email("pooruser@university.edu")
                .name("Poor User")
                .studentId("POOR001")
                .build();
        sender = userRepository.save(sender);
        Wallet senderWallet = walletService.createWallet(sender.getId());
        
        // Create recipient
        User recipient = User.builder()
                .email("richuser@university.edu")
                .name("Rich User")
                .studentId("RICH001")
                .build();
        recipient = userRepository.save(recipient);
        walletService.createWallet(recipient.getId());
        
        // Sender has only $10
        CurrencyAccount senderUsdAccount = currencyAccountRepository
                .findByWalletIdAndCurrencyCode(senderWallet.getId(), "USD")
                .orElseThrow();
        senderUsdAccount.setBalance(new BigDecimal("10.00"));
        currencyAccountRepository.save(senderUsdAccount);
        
        // Try to transfer $100 (more than available)
        TransferRequest request = TransferRequest.builder()
                .fromUserId(sender.getId())
                .toStudentId(recipient.getStudentId())
                .amount(new BigDecimal("100.00"))
                .currencyCode("USD")
                .description("Should fail")
                .build();
        
        // Should throw exception
        Exception exception = assertThrows(RuntimeException.class, () -> {
            p2pTransferService.executeTransfer(request);
        });
        
        assertTrue(exception.getMessage().contains("Insufficient balance"));
        System.out.println("✅ Insufficient balance validation working!");
    }
    
    @Test
    public void testP2PTransfer_Idempotency() {
        // Create sender and recipient
        User sender = User.builder()
                .email("idem.sender@university.edu")
                .name("Idem Sender")
                .studentId("IDEM001")
                .build();
        sender = userRepository.save(sender);
        Wallet senderWallet = walletService.createWallet(sender.getId());
        
        User recipient = User.builder()
                .email("idem.recipient@university.edu")
                .name("Idem Recipient")
                .studentId("IDEM002")
                .build();
        recipient = userRepository.save(recipient);
        Wallet recipientWallet = walletService.createWallet(recipient.getId());
        
        // Give sender balance
        CurrencyAccount senderUsdAccount = currencyAccountRepository
                .findByWalletIdAndCurrencyCode(senderWallet.getId(), "USD")
                .orElseThrow();
        senderUsdAccount.setBalance(new BigDecimal("1000.00"));
        currencyAccountRepository.save(senderUsdAccount);
        
        // Create transfer request with idempotency key
        TransferRequest request = TransferRequest.builder()
                .fromUserId(sender.getId())
                .toStudentId(recipient.getStudentId())
                .amount(new BigDecimal("100.00"))
                .currencyCode("USD")
                .idempotencyKey("unique-key-123")
                .build();
        
        // Execute first transfer
        Transaction firstTransaction = p2pTransferService.executeTransfer(request);
        
        // Execute same transfer again (duplicate)
        Transaction secondTransaction = p2pTransferService.executeTransfer(request);
        
        // Should return the same transaction
        assertEquals(firstTransaction.getId(), secondTransaction.getId());
        
        // Balance should only be deducted once (normalize to 2 decimal places)
        BigDecimal senderBalance = walletService.getBalanceByCurrency(senderWallet.getId(), "USD")
                .setScale(2, RoundingMode.HALF_UP);
        assertEquals(0, new BigDecimal("900.00").compareTo(senderBalance));
        
        System.out.println("✅ Idempotency check working! Duplicate prevented.");
    }
}