package com.campuscross.wallet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class HashingService {
    
    private final ObjectMapper objectMapper;
    
    /**
     * Calculate SHA-256 hash for an audit block
     * Hash includes: blockNumber + previousHash + eventData + timestamp + nonce
     */
    public String calculateBlockHash(
            Long blockNumber,
            String previousHash,
            Map<String, Object> eventData,
            LocalDateTime timestamp,
            Long nonce
    ) {
        try {
            // Build the data string to hash
            String dataToHash = buildHashInput(blockNumber, previousHash, eventData, timestamp, nonce);
            
            // Calculate SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(dataToHash.getBytes(StandardCharsets.UTF_8));
            
            // Convert to hexadecimal string
            return bytesToHex(hashBytes);
            
        } catch (Exception e) {
            log.error("Error calculating block hash", e);
            throw new RuntimeException("Failed to calculate block hash: " + e.getMessage());
        }
    }
    
    /**
     * Build the input string for hashing
     */
    private String buildHashInput(
            Long blockNumber,
            String previousHash,
            Map<String, Object> eventData,
            LocalDateTime timestamp,
            Long nonce
    ) {
        try {
            StringBuilder input = new StringBuilder();
            
            // Block number
            input.append("BLOCK:").append(blockNumber).append("|");
            
            // Previous hash
            input.append("PREV:").append(previousHash).append("|");
            
            // Event data (serialized to JSON)
            String eventDataJson = objectMapper.writeValueAsString(eventData);
            input.append("DATA:").append(eventDataJson).append("|");
            
            // Timestamp (ISO format)
            String timestampStr = timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            input.append("TIME:").append(timestampStr).append("|");
            
            // Nonce
            input.append("NONCE:").append(nonce);
            
            return input.toString();
            
        } catch (Exception e) {
            log.error("Error building hash input", e);
            throw new RuntimeException("Failed to build hash input: " + e.getMessage());
        }
    }
    
    /**
     * Calculate Merkle root hash from multiple data items
     * Used to create a single hash representing all transactions in a block
     */
    public String calculateMerkleRoot(Map<String, Object> eventData) {
        try {
            // For simplicity, we'll hash the entire event data
            // In a real blockchain, this would be a tree of hashes
            String dataJson = objectMapper.writeValueAsString(eventData);
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(dataJson.getBytes(StandardCharsets.UTF_8));
            
            return bytesToHex(hashBytes);
            
        } catch (Exception e) {
            log.error("Error calculating merkle root", e);
            throw new RuntimeException("Failed to calculate merkle root: " + e.getMessage());
        }
    }
    
    /**
     * Verify if a block's hash is correct
     */
    public boolean verifyBlockHash(
            String expectedHash,
            Long blockNumber,
            String previousHash,
            Map<String, Object> eventData,
            LocalDateTime timestamp,
            Long nonce
    ) {
        String calculatedHash = calculateBlockHash(blockNumber, previousHash, eventData, timestamp, nonce);
        return expectedHash.equals(calculatedHash);
    }
    
    /**
     * Calculate hash with proof-of-work (optional - for advanced security)
     * Finds a nonce that makes the hash start with a certain number of zeros
     */
    public NonceResult calculateHashWithProofOfWork(
            Long blockNumber,
            String previousHash,
            Map<String, Object> eventData,
            LocalDateTime timestamp,
            int difficulty
    ) {
        long nonce = 0;
        String hash;
        String target = "0".repeat(difficulty); // e.g., "000" for difficulty 3
        
        long startTime = System.currentTimeMillis();
        
        do {
            hash = calculateBlockHash(blockNumber, previousHash, eventData, timestamp, nonce);
            nonce++;
            
            // Prevent infinite loop
            if (nonce > 1000000) {
                log.warn("Proof of work took too long, stopping at nonce: {}", nonce);
                break;
            }
            
        } while (!hash.startsWith(target));
        
        long timeTaken = System.currentTimeMillis() - startTime;
        
        log.info("Proof of work completed: difficulty={}, nonce={}, time={}ms", 
                difficulty, nonce - 1, timeTaken);
        
        return new NonceResult(nonce - 1, hash, timeTaken);
    }
    
    /**
     * Convert byte array to hexadecimal string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
    /**
     * Result of proof-of-work calculation
     */
    public static class NonceResult {
        public final Long nonce;
        public final String hash;
        public final Long timeTakenMs;
        
        public NonceResult(Long nonce, String hash, Long timeTakenMs) {
            this.nonce = nonce;
            this.hash = hash;
            this.timeTakenMs = timeTakenMs;
        }
    }
}