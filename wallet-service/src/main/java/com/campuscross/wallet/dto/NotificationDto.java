package com.campuscross.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private String id;
    private String userId;
    private NotificationType type;
    private String title;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;
    private Map<String, Object> data;

    public enum NotificationType {
        TRANSACTION,
        WALLET,
        KYC,
        RATE_ALERT,
        SECURITY,
        SYSTEM
    }
}
