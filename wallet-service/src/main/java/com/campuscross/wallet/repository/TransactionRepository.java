package com.campuscross.wallet.repository;

import com.campuscross.wallet.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    Optional<Transaction> findByTransactionId(String transactionId);
    
    List<Transaction> findBySourceWalletId(Long sourceWalletId);
    
    List<Transaction> findByTargetWalletId(Long targetWalletId);
    
    List<Transaction> findBySourceWalletIdOrTargetWalletId(Long walletId, Long walletId2);
    
    Page<Transaction> findBySourceWalletIdOrTargetWalletId(Long walletId, Long walletId2, Pageable pageable);
    
    List<Transaction> findByStatus(Transaction.TransactionStatus status);
    
    List<Transaction> findByType(Transaction.TransactionType type);
    
    List<Transaction> findByMerchantId(String merchantId);
    
    List<Transaction> findByCampusLocation(String campusLocation);
    
    @Query("SELECT t FROM Transaction t WHERE t.sourceWallet.user.id = :userId OR t.targetWallet.user.id = :userId")
    List<Transaction> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT t FROM Transaction t WHERE t.sourceWallet.user.id = :userId OR t.targetWallet.user.id = :userId ORDER BY t.createdAt DESC")
    Page<Transaction> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT t FROM Transaction t WHERE (t.sourceWallet.user.id = :userId OR t.targetWallet.user.id = :userId) AND t.createdAt BETWEEN :startDate AND :endDate")
    List<Transaction> findByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :startDate AND :endDate")
    List<Transaction> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.status = :status")
    long countByStatus(@Param("status") Transaction.TransactionStatus status);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.type = :type")
    long countByType(@Param("type") Transaction.TransactionType type);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.status = 'COMPLETED' AND t.sourceWallet.user.id = :userId")
    BigDecimal getTotalSpentByUserId(@Param("userId") Long userId);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.status = 'COMPLETED' AND t.targetWallet.user.id = :userId")
    BigDecimal getTotalReceivedByUserId(@Param("userId") Long userId);
    
    @Query("SELECT t FROM Transaction t WHERE t.flagged = true")
    List<Transaction> findFlaggedTransactions();
    
    @Query("SELECT t FROM Transaction t WHERE t.failureReason IS NOT NULL AND t.status = 'FAILED'")
    List<Transaction> findFailedTransactions();
    
    @Query("SELECT SUM(t.feeAmount) FROM Transaction t WHERE t.status = 'COMPLETED' AND t.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalFeesCollected(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
