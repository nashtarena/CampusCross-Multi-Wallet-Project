package com.campuscross.wallet.service;

import com.campuscross.wallet.dto.DisbursementRequest;
import com.campuscross.wallet.entity.*;
import com.campuscross.wallet.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkDisbursementService {
    
    private final DisbursementBatchRepository batchRepository;
    private final DisbursementItemRepository itemRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final CurrencyAccountRepository currencyAccountRepository;
    private final TransactionRepository transactionRepository;
    
    /**
     * Create a new disbursement batch
     */
    @Transactional
    public DisbursementBatch createDisbursementBatch(DisbursementRequest request) {
        log.info("Creating disbursement batch with {} recipients", request.getRecipients().size());
        
        // Validate request
        validateDisbursementRequest(request);
        
        // Calculate total amount
        BigDecimal totalAmount = request.getRecipients().stream()
                .map(DisbursementRequest.DisbursementRecipient::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Create batch
        DisbursementBatch batch = DisbursementBatch.builder()
                .batchId(UUID.randomUUID().toString())
                .createdBy(request.getAdminUserId())
                .totalCount(request.getRecipients().size())
                .totalAmount(totalAmount)
                .currency(request.getCurrency())
                .status("PENDING")
                .description(request.getDescription())
                .build();
        
        batch = batchRepository.save(batch);
        
        // Create individual items
        for (DisbursementRequest.DisbursementRecipient recipient : request.getRecipients()) {
            DisbursementItem item = DisbursementItem.builder()
                    .batchId(batch.getId())
                    .studentId(recipient.getStudentId())
                    .amount(recipient.getAmount())
                    .currency(request.getCurrency())
                    .status("PENDING")
                    .build();
            itemRepository.save(item);
        }
        
        log.info("Disbursement batch created with ID: {}", batch.getBatchId());
        return batch;
    }
    
    /**
     * Process a disbursement batch (execute all transfers)
     */
    @Transactional
    public void processDisbursementBatch(Long batchId) {
        log.info("Processing disbursement batch: {}", batchId);
        
        DisbursementBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found: " + batchId));
        
        if (!"PENDING".equals(batch.getStatus())) {
            throw new RuntimeException("Batch is not in PENDING status");
        }
        
        // Update batch status to PROCESSING
        batch.setStatus("PROCESSING");
        batchRepository.save(batch);
        
        // Get all items
        List<DisbursementItem> items = itemRepository.findByBatchId(batchId);
        
        int successCount = 0;
        int failedCount = 0;
        
        // Process each item
        for (DisbursementItem item : items) {
            try {
                processDisbursementItem(item, batch.getCurrency());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to process disbursement item {}: {}", item.getId(), e.getMessage());
                item.setStatus("FAILED");
                item.setErrorMessage(e.getMessage());
                item.setProcessedAt(LocalDateTime.now());
                itemRepository.save(item);
                failedCount++;
            }
        }
        
        // Update batch with results
        batch.setSuccessCount(successCount);
        batch.setFailedCount(failedCount);
        batch.setStatus(failedCount == 0 ? "COMPLETED" : "COMPLETED");
        batch.setCompletedAt(LocalDateTime.now());
        batchRepository.save(batch);
        
        log.info("Batch processing complete. Success: {}, Failed: {}", successCount, failedCount);
    }
    
    /**
     * Process a single disbursement item
     */
    private void processDisbursementItem(DisbursementItem item, String currency) {
        // Find recipient by student ID
        User recipient = userRepository.findByStudentId(item.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found: " + item.getStudentId()));
        
        // Get recipient's wallet
        Wallet wallet = walletRepository.findByUserId(recipient.getId())
                .orElseThrow(() -> new RuntimeException("Wallet not found for student: " + item.getStudentId()));
        
        // Get recipient's currency account
        CurrencyAccount account = currencyAccountRepository
                .findByWalletIdAndCurrencyCode(wallet.getId(), currency)
                .orElseThrow(() -> new RuntimeException("Currency account not found: " + currency));
        
        // Credit the amount
        account.setBalance(account.getBalance().add(item.getAmount()));
        currencyAccountRepository.save(account);
        
        // Create transaction record
        Transaction transaction = Transaction.builder()
                .transactionType("DISBURSEMENT")
                .toAccountId(account.getId())
                .amount(item.getAmount())
                .currencyCode(currency)
                .status("completed")
                .referenceId("DISB-" + item.getId())
                .description("Disbursement to " + item.getStudentId())
                .completedAt(LocalDateTime.now())
                .build();
        
        transaction = transactionRepository.save(transaction);
        
        // Update item
        item.setStatus("SUCCESS");
        item.setTransactionId(transaction.getId());
        item.setProcessedAt(LocalDateTime.now());
        itemRepository.save(item);
        
        log.info("Disbursement item processed: {} - {} {}", 
                item.getStudentId(), item.getAmount(), currency);
    }
    
    /**
     * Get batch status and progress
     */
    @Transactional(readOnly = true)
    public DisbursementBatch getBatchStatus(String batchId) {
        return batchRepository.findByBatchId(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found: " + batchId));
    }
    
    /**
     * Get all items in a batch
     */
    @Transactional(readOnly = true)
    public List<DisbursementItem> getBatchItems(Long batchId) {
        return itemRepository.findByBatchId(batchId);
    }
    
    /**
     * Get all batches created by an admin
     */
    @Transactional(readOnly = true)
    public List<DisbursementBatch> getAdminBatches(Long adminUserId) {
        return batchRepository.findByCreatedBy(adminUserId);
    }
    
    /**
     * Validate disbursement request
     */
    private void validateDisbursementRequest(DisbursementRequest request) {
        if (request.getAdminUserId() == null) {
            throw new RuntimeException("Admin user ID is required");
        }
        
        if (request.getCurrency() == null || request.getCurrency().isEmpty()) {
            throw new RuntimeException("Currency is required");
        }
        
        if (request.getRecipients() == null || request.getRecipients().isEmpty()) {
            throw new RuntimeException("At least one recipient is required");
        }
        
        // Validate each recipient
        for (DisbursementRequest.DisbursementRecipient recipient : request.getRecipients()) {
            if (recipient.getStudentId() == null || recipient.getStudentId().isEmpty()) {
                throw new RuntimeException("Student ID is required for all recipients");
            }
            
            if (recipient.getAmount() == null || recipient.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Amount must be greater than zero");
            }
        }
    }
}