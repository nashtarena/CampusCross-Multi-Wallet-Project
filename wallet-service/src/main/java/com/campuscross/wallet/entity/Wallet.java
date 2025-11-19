package com.campuscross.wallet.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "wallets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(unique = true, nullable = false)
    private String walletAddress;
    
    @Column(name = "wallet_name", nullable = false)
    private String walletName;
    
    @Enumerated(EnumType.STRING)
    private WalletType type;
    
    @Enumerated(EnumType.STRING)
    private WalletStatus status;
    
    @Column(precision = 19, scale = 8, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Column(name = "currency_code", nullable = false)
    private String currencyCode = "USD";
    
    @Column(name = "is_default")
    private Boolean isDefault = false;
    
    @Column(name = "daily_limit")
    private BigDecimal dailyLimit;
    
    @Column(name = "monthly_limit")
    private BigDecimal monthlyLimit;
    
    @Column(name = "daily_spent")
    private BigDecimal dailySpent = BigDecimal.ZERO;
    
    @Column(name = "monthly_spent")
    private BigDecimal monthlySpent = BigDecimal.ZERO;
    
    @Column(name = "last_daily_reset")
    private LocalDateTime lastDailyReset;
    
    @Column(name = "last_monthly_reset")
    private LocalDateTime lastMonthlyReset;
    
    @OneToMany(mappedBy = "sourceWallet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> sentTransactions;
    
    @OneToMany(mappedBy = "targetWallet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> receivedTransactions;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum WalletType {
        PERSONAL, CAMPUS, MERCHANT, SAVINGS
    }
    
    public enum WalletStatus {
        ACTIVE, FROZEN, CLOSED, PENDING_VERIFICATION
    }
    
    public boolean canSpend(BigDecimal amount) {
        if (status != WalletStatus.ACTIVE) return false;
        
        BigDecimal availableBalance = balance;
        
        // Check daily limit
        if (dailyLimit != null) {
            updateDailySpent();
            if (dailySpent.add(amount).compareTo(dailyLimit) > 0) {
                return false;
            }
        }
        
        // Check monthly limit
        if (monthlyLimit != null) {
            updateMonthlySpent();
            if (monthlySpent.add(amount).compareTo(monthlyLimit) > 0) {
                return false;
            }
        }
        
        return availableBalance.compareTo(amount) >= 0;
    }
    
    private void updateDailySpent() {
        LocalDateTime now = LocalDateTime.now();
        if (lastDailyReset == null || lastDailyReset.toLocalDate().isBefore(now.toLocalDate())) {
            dailySpent = BigDecimal.ZERO;
            lastDailyReset = now;
        }
    }
    
    private void updateMonthlySpent() {
        LocalDateTime now = LocalDateTime.now();
        if (lastMonthlyReset == null || 
            lastMonthlyReset.getYear() < now.getYear() || 
            lastMonthlyReset.getMonthValue() < now.getMonthValue()) {
            monthlySpent = BigDecimal.ZERO;
            lastMonthlyReset = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        }
    }
    
    public void deductBalance(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
        updateDailySpent();
        dailySpent = dailySpent.add(amount);
        updateMonthlySpent();
        monthlySpent = monthlySpent.add(amount);
    }
    
    public void addBalance(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }
}
