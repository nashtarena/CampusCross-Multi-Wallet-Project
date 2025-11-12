package com.campuscross.wallet.service;

import com.campuscross.wallet.dto.DisbursementRequest;
import com.campuscross.wallet.entity.DisbursementBatch;
import com.campuscross.wallet.entity.DisbursementItem;
import com.campuscross.wallet.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class BulkDisbursementServiceTest {
    
    @Autowired
    private BulkDisbursementService disbursementService;
    
    @Autowired
    private WalletService walletService;
    
    @Autowired
    private com.campuscross.wallet.repository.UserRepository userRepository;
    
    @Test
    public void testCreateDisbursementBatch() {
        // Create admin user
        User admin = User.builder()
                .email("admin@university.edu")
                .name("Admin User")
                .studentId("ADMIN001")
                .build();
        admin = userRepository.save(admin);
        
        // Create recipients
        List<DisbursementRequest.DisbursementRecipient> recipients = new ArrayList<>();
        recipients.add(DisbursementRequest.DisbursementRecipient.builder()
                .studentId("STU001")
                .amount(new BigDecimal("500.00"))
                .build());
        recipients.add(DisbursementRequest.DisbursementRecipient.builder()
                .studentId("STU002")
                .amount(new BigDecimal("750.00"))
                .build());
        
        // Create disbursement request
        DisbursementRequest request = DisbursementRequest.builder()
                .adminUserId(admin.getId())
                .currency("USD")
                .description("Scholarship disbursement Q1")
                .recipients(recipients)
                .build();
        
        // Create batch
        DisbursementBatch batch = disbursementService.createDisbursementBatch(request);
        
        // Verify batch
        assertNotNull(batch);
        assertNotNull(batch.getBatchId());
        assertEquals(2, batch.getTotalCount());
        assertEquals(0, new BigDecimal("1250.00").compareTo(batch.getTotalAmount()));
        assertEquals("PENDING", batch.getStatus());
        assertEquals("USD", batch.getCurrency());
        
        System.out.println("✅ Disbursement batch created!");
        System.out.println("   Batch ID: " + batch.getBatchId());
        System.out.println("   Total: $" + batch.getTotalAmount());
        System.out.println("   Recipients: " + batch.getTotalCount());
    }
    
    @Test
    public void testProcessDisbursementBatch() {
        // Create admin user
        User admin = User.builder()
                .email("admin2@university.edu")
                .name("Admin User 2")
                .studentId("ADMIN002")
                .build();
        admin = userRepository.save(admin);
        
        // Create 3 student recipients with wallets
        List<User> students = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            User student = User.builder()
                    .email("student" + i + "@university.edu")
                    .name("Student " + i)
                    .studentId("BATCH_STU00" + i)
                    .build();
            student = userRepository.save(student);
            walletService.createWallet(student.getId());
            students.add(student);
        }
        
        // Create disbursement request
        List<DisbursementRequest.DisbursementRecipient> recipients = new ArrayList<>();
        recipients.add(DisbursementRequest.DisbursementRecipient.builder()
                .studentId("BATCH_STU001")
                .amount(new BigDecimal("100.00"))
                .build());
        recipients.add(DisbursementRequest.DisbursementRecipient.builder()
                .studentId("BATCH_STU002")
                .amount(new BigDecimal("200.00"))
                .build());
        recipients.add(DisbursementRequest.DisbursementRecipient.builder()
                .studentId("BATCH_STU003")
                .amount(new BigDecimal("300.00"))
                .build());
        
        DisbursementRequest request = DisbursementRequest.builder()
                .adminUserId(admin.getId())
                .currency("USD")
                .description("Test batch disbursement")
                .recipients(recipients)
                .build();
        
        // Create and process batch
        DisbursementBatch batch = disbursementService.createDisbursementBatch(request);
        disbursementService.processDisbursementBatch(batch.getId());
        
        // Verify batch completed
        DisbursementBatch completedBatch = disbursementService.getBatchStatus(batch.getBatchId());
        assertEquals("COMPLETED", completedBatch.getStatus());
        assertEquals(3, completedBatch.getSuccessCount());
        assertEquals(0, completedBatch.getFailedCount());
        
        // Verify students received funds
        BigDecimal balance1 = walletService.getBalanceByCurrency(
                walletService.getWalletByUserId(students.get(0).getId()).getId(), 
                "USD").setScale(2, RoundingMode.HALF_UP);
        assertEquals(0, new BigDecimal("100.00").compareTo(balance1));
        
        BigDecimal balance2 = walletService.getBalanceByCurrency(
                walletService.getWalletByUserId(students.get(1).getId()).getId(), 
                "USD").setScale(2, RoundingMode.HALF_UP);
        assertEquals(0, new BigDecimal("200.00").compareTo(balance2));
        
        BigDecimal balance3 = walletService.getBalanceByCurrency(
                walletService.getWalletByUserId(students.get(2).getId()).getId(), 
                "USD").setScale(2, RoundingMode.HALF_UP);
        assertEquals(0, new BigDecimal("300.00").compareTo(balance3));
        
        System.out.println("✅ Bulk disbursement processed successfully!");
        System.out.println("   Success: " + completedBatch.getSuccessCount());
        System.out.println("   Failed: " + completedBatch.getFailedCount());
        System.out.println("   Student 1 balance: $" + balance1);
        System.out.println("   Student 2 balance: $" + balance2);
        System.out.println("   Student 3 balance: $" + balance3);
    }
    
    @Test
    public void testDisbursementWithPartialFailure() {
        // Create admin
        User admin = User.builder()
                .email("admin3@university.edu")
                .name("Admin User 3")
                .studentId("ADMIN003")
                .build();
        admin = userRepository.save(admin);
        
        // Create only 1 valid student
        User validStudent = User.builder()
                .email("valid@university.edu")
                .name("Valid Student")
                .studentId("VALID001")
                .build();
        validStudent = userRepository.save(validStudent);
        walletService.createWallet(validStudent.getId());
        
        // Create disbursement with 1 valid and 1 invalid student
        List<DisbursementRequest.DisbursementRecipient> recipients = new ArrayList<>();
        recipients.add(DisbursementRequest.DisbursementRecipient.builder()
                .studentId("VALID001")
                .amount(new BigDecimal("100.00"))
                .build());
        recipients.add(DisbursementRequest.DisbursementRecipient.builder()
                .studentId("INVALID999") // This student doesn't exist
                .amount(new BigDecimal("200.00"))
                .build());
        
        DisbursementRequest request = DisbursementRequest.builder()
                .adminUserId(admin.getId())
                .currency("USD")
                .description("Partial failure test")
                .recipients(recipients)
                .build();
        
        // Create and process batch
        DisbursementBatch batch = disbursementService.createDisbursementBatch(request);
        disbursementService.processDisbursementBatch(batch.getId());
        
        // Verify partial success
        DisbursementBatch completedBatch = disbursementService.getBatchStatus(batch.getBatchId());
        assertEquals("COMPLETED", completedBatch.getStatus());
        assertEquals(1, completedBatch.getSuccessCount());
        assertEquals(1, completedBatch.getFailedCount());
        
        // Check items
        List<DisbursementItem> items = disbursementService.getBatchItems(batch.getId());
        assertEquals(2, items.size());
        
        long successItems = items.stream().filter(i -> "SUCCESS".equals(i.getStatus())).count();
        long failedItems = items.stream().filter(i -> "FAILED".equals(i.getStatus())).count();
        
        assertEquals(1, successItems);
        assertEquals(1, failedItems);
        
        System.out.println("✅ Partial failure handling working!");
        System.out.println("   Success: " + completedBatch.getSuccessCount());
        System.out.println("   Failed: " + completedBatch.getFailedCount());
    }
    
    @Test
    public void testGetAdminBatches() {
        // Create admin
        User admin = User.builder()
                .email("admin4@university.edu")
                .name("Admin User 4")
                .studentId("ADMIN004")
                .build();
        admin = userRepository.save(admin);
        
        // Create 2 batches
        for (int i = 1; i <= 2; i++) {
            List<DisbursementRequest.DisbursementRecipient> recipients = new ArrayList<>();
            recipients.add(DisbursementRequest.DisbursementRecipient.builder()
                    .studentId("TEST" + i)
                    .amount(new BigDecimal("100.00"))
                    .build());
            
            DisbursementRequest request = DisbursementRequest.builder()
                    .adminUserId(admin.getId())
                    .currency("USD")
                    .description("Batch " + i)
                    .recipients(recipients)
                    .build();
            
            disbursementService.createDisbursementBatch(request);
        }
        
        // Get admin's batches
        List<DisbursementBatch> batches = disbursementService.getAdminBatches(admin.getId());
        
        assertEquals(2, batches.size());
        
        System.out.println("✅ Admin batch retrieval working!");
        System.out.println("   Total batches: " + batches.size());
    }
}