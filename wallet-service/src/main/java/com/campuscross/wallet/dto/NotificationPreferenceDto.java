package com.campuscross.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceDto {
    private String userId;
    private boolean transactionNotifications;
    private boolean walletNotifications;
    private boolean kycNotifications;
    private boolean rateAlertNotifications;
    private boolean securityNotifications;
    private boolean systemNotifications;
    private boolean emailNotifications;
    private boolean pushNotifications;
}
