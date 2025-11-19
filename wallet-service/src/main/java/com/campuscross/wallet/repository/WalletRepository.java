package com.campuscross.wallet.repository;

import com.campuscross.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    
    Optional<Wallet> findByWalletAddress(String walletAddress);
    
    List<Wallet> findByUserId(String userId);
    
    List<Wallet> findByUserIdAndStatus(String userId, Wallet.WalletStatus status);
    
    Optional<Wallet> findByUserIdAndIsDefaultTrue(String userId);
    
    List<Wallet> findByCurrencyCode(String currencyCode);
    
    List<Wallet> findByType(Wallet.WalletType type);
    
    List<Wallet> findByStatus(Wallet.WalletStatus status);
    
    @Query("SELECT w FROM Wallet w WHERE w.user.id = :userId AND w.currencyCode = :currencyCode")
    Optional<Wallet> findByUserIdAndCurrencyCode(@Param("userId") String userId, @Param("currencyCode") String currencyCode);
    
    @Query("SELECT w FROM Wallet w WHERE w.balance > :minBalance")
    List<Wallet> findWalletsWithBalanceGreaterThan(@Param("minBalance") BigDecimal minBalance);
    
    @Query("SELECT SUM(w.balance) FROM Wallet w WHERE w.user.id = :userId AND w.status = 'ACTIVE'")
    BigDecimal getTotalBalanceByUserId(@Param("userId") String userId);
    
    @Query("SELECT w FROM Wallet w WHERE w.dailyLimit IS NOT NULL AND w.dailySpent >= w.dailyLimit")
    List<Wallet> findWalletsWithDailyLimitReached();
    
    @Query("SELECT w FROM Wallet w WHERE w.monthlyLimit IS NOT NULL AND w.monthlySpent >= w.monthlyLimit")
    List<Wallet> findWalletsWithMonthlyLimitReached();
    
    @Query("SELECT COUNT(w) FROM Wallet w WHERE w.status = :status")
    long countByStatus(@Param("status") Wallet.WalletStatus status);
    
    @Query("SELECT COUNT(w) FROM Wallet w WHERE w.type = :type")
    long countByType(@Param("type") Wallet.WalletType type);
}
