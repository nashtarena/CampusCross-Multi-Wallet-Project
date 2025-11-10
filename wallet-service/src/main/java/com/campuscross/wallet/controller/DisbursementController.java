package com.campuscross.wallet.controller;

import com.campuscross.wallet.dto.DisbursementRequest;
import com.campuscross.wallet.entity.DisbursementBatch;
import com.campuscross.wallet.entity.DisbursementItem;
import com.campuscross.wallet.service.BulkDisbursementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/disbursements")
@RequiredArgsConstructor
@Slf4j
public class DisbursementController {
    
    private final BulkDisbursementService disbursementService;
    
    /**
     * Create disbursement batch
     * POST /api/v1/disbursements/batch
     */
    @PostMapping("/batch")
    public ResponseEntity<DisbursementBatch> createDisbursementBatch(
            @Valid @RequestBody DisbursementRequest request) {
        log.info("Creating disbursement batch with {} recipients", request.getRecipients().size());
        DisbursementBatch batch = disbursementService.createDisbursementBatch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(batch);
    }
    
    /**
     * Process disbursement batch
     * POST /api/v1/disbursements/batch/{batchId}/process
     */
    @PostMapping("/batch/{batchId}/process")
    public ResponseEntity<Void> processBatch(@PathVariable Long batchId) {
        log.info("Processing disbursement batch: {}", batchId);
        disbursementService.processDisbursementBatch(batchId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Get batch status
     * GET /api/v1/disbursements/batch/{batchId}
     */
    @GetMapping("/batch/{batchId}")
    public ResponseEntity<DisbursementBatch> getBatchStatus(@PathVariable String batchId) {
        log.info("Fetching batch status: {}", batchId);
        DisbursementBatch batch = disbursementService.getBatchStatus(batchId);
        return ResponseEntity.ok(batch);
    }
    
    /**
     * Get batch items
     * GET /api/v1/disbursements/batch/{batchId}/items
     */
    @GetMapping("/batch/{batchId}/items")
    public ResponseEntity<List<DisbursementItem>> getBatchItems(@PathVariable Long batchId) {
        log.info("Fetching items for batch: {}", batchId);
        List<DisbursementItem> items = disbursementService.getBatchItems(batchId);
        return ResponseEntity.ok(items);
    }
    
    /**
     * Get admin's batches
     * GET /api/v1/disbursements/admin/{adminId}/batches
     */
    @GetMapping("/admin/{adminId}/batches")
    public ResponseEntity<List<DisbursementBatch>> getAdminBatches(@PathVariable Long adminId) {
        log.info("Fetching batches for admin: {}", adminId);
        List<DisbursementBatch> batches = disbursementService.getAdminBatches(adminId);
        return ResponseEntity.ok(batches);
    }
}