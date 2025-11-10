package com.campuscross.wallet.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campuscross.wallet.dto.TransferRequest;
import com.campuscross.wallet.entity.Transaction;
import com.campuscross.wallet.service.P2PTransferService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
@Slf4j
public class TransferController {
    
    private final P2PTransferService p2pTransferService;
    
    /**
     * Execute P2P transfer
     * POST /api/v1/transfers/p2p
     */
    @PostMapping("/p2p")
    public ResponseEntity<Transaction> executeP2PTransfer(@Valid @RequestBody TransferRequest request) {
        log.info("Executing P2P transfer from user {} to {}", 
                request.getFromUserId(), 
                request.getToStudentId() != null ? request.getToStudentId() : request.getToMobileNumber());
        
        Transaction transaction = p2pTransferService.executeTransfer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }
}