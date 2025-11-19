package com.campuscross.wallet.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreference {
    @Id
    private String userId;
    
    @Column(name = "transaction_notifications", nullable = false)
    private boolean transactionNotifications;
    
    @Column(name = "wallet_notifications", nullable = false)
    private boolean walletNotifications;
    
    @Column(name = "kyc_notifications", nullable = false)
    private boolean kycNotifications;
    
    @Column(name = "rate_alert_notifications", nullable = false)
    private boolean rateAlertNotifications;
    
    @Column(name = "security_notifications", nullable = false)
    private boolean securityNotifications;
    
    @Column(name = "system_notifications", nullable = false)
    private boolean systemNotifications;
    
    @Column(name = "email_notifications", nullable = false)
    private boolean emailNotifications;
    
    @Column(name = "push_notifications", nullable = false)
    private boolean pushNotifications;
}
