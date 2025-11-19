package com.campuscross.wallet.service;

import com.campuscross.wallet.dto.NotificationDto;
import com.campuscross.wallet.dto.NotificationPreferenceDto;
import com.campuscross.wallet.entity.Notification;
import com.campuscross.wallet.entity.NotificationPreference;
import com.campuscross.wallet.repository.NotificationRepository;
import com.campuscross.wallet.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;

    @Transactional(readOnly = true)
    public List<NotificationDto> getUserNotifications(String userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markNotificationAsRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        
        notification.setRead(true);
        notificationRepository.save(notification);
        log.info("Marked notification as read: {}", notificationId);
    }

    @Transactional
    public void markAllNotificationsAsRead(String userId) {
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndReadFalse(userId);
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
        log.info("Marked {} notifications as read for user: {}", unreadNotifications.size(), userId);
    }

    @Transactional
    public void deleteNotification(String notificationId) {
        if (!notificationRepository.existsById(notificationId)) {
            throw new RuntimeException("Notification not found: " + notificationId);
        }
        
        notificationRepository.deleteById(notificationId);
        log.info("Deleted notification: {}", notificationId);
    }

    @Transactional(readOnly = true)
    public NotificationPreferenceDto getNotificationPreferences(String userId) {
        NotificationPreference preference = notificationPreferenceRepository.findByUserId(userId)
                .orElse(createDefaultPreferences(userId));
        
        return convertToDto(preference);
    }

    @Transactional
    public void updateNotificationPreferences(NotificationPreferenceDto preferenceDto) {
        NotificationPreference preference = notificationPreferenceRepository.findByUserId(preferenceDto.getUserId())
                .orElse(NotificationPreference.builder()
                        .userId(preferenceDto.getUserId())
                        .build());
        
        preference.setTransactionNotifications(preferenceDto.isTransactionNotifications());
        preference.setWalletNotifications(preferenceDto.isWalletNotifications());
        preference.setKycNotifications(preferenceDto.isKycNotifications());
        preference.setRateAlertNotifications(preferenceDto.isRateAlertNotifications());
        preference.setSecurityNotifications(preferenceDto.isSecurityNotifications());
        preference.setSystemNotifications(preferenceDto.isSystemNotifications());
        preference.setEmailNotifications(preferenceDto.isEmailNotifications());
        preference.setPushNotifications(preferenceDto.isPushNotifications());
        
        notificationPreferenceRepository.save(preference);
        log.info("Updated notification preferences for user: {}", preferenceDto.getUserId());
    }

    @Transactional
    public void createNotification(NotificationDto notificationDto) {
        Notification notification = convertToEntity(notificationDto);
        notification.setId(UUID.randomUUID().toString());
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        
        notificationRepository.save(notification);
        log.info("Created notification for user: {}", notificationDto.getUserId());
    }

    @Transactional
    public void createTransactionNotification(String userId, String title, String message) {
        NotificationPreference preferences = notificationPreferenceRepository.findByUserId(userId)
                .orElse(createDefaultPreferences(userId));
        
        if (preferences.isTransactionNotifications()) {
            NotificationDto notification = NotificationDto.builder()
                    .userId(userId)
                    .type(NotificationDto.NotificationType.TRANSACTION)
                    .title(title)
                    .message(message)
                    .build();
            
            createNotification(notification);
        }
    }

    @Transactional
    public void createWalletNotification(String userId, String title, String message) {
        NotificationPreference preferences = notificationPreferenceRepository.findByUserId(userId)
                .orElse(createDefaultPreferences(userId));
        
        if (preferences.isWalletNotifications()) {
            NotificationDto notification = NotificationDto.builder()
                    .userId(userId)
                    .type(NotificationDto.NotificationType.WALLET)
                    .title(title)
                    .message(message)
                    .build();
            
            createNotification(notification);
        }
    }

    @Transactional
    public void createKycNotification(String userId, String title, String message) {
        NotificationPreference preferences = notificationPreferenceRepository.findByUserId(userId)
                .orElse(createDefaultPreferences(userId));
        
        if (preferences.isKycNotifications()) {
            NotificationDto notification = NotificationDto.builder()
                    .userId(userId)
                    .type(NotificationDto.NotificationType.KYC)
                    .title(title)
                    .message(message)
                    .build();
            
            createNotification(notification);
        }
    }

    @Transactional
    public void createSecurityNotification(String userId, String title, String message) {
        NotificationPreference preferences = notificationPreferenceRepository.findByUserId(userId)
                .orElse(createDefaultPreferences(userId));
        
        if (preferences.isSecurityNotifications()) {
            NotificationDto notification = NotificationDto.builder()
                    .userId(userId)
                    .type(NotificationDto.NotificationType.SECURITY)
                    .title(title)
                    .message(message)
                    .build();
            
            createNotification(notification);
        }
    }

    private NotificationPreference createDefaultPreferences(String userId) {
        NotificationPreference preference = NotificationPreference.builder()
                .userId(userId)
                .transactionNotifications(true)
                .walletNotifications(true)
                .kycNotifications(true)
                .rateAlertNotifications(true)
                .securityNotifications(true)
                .systemNotifications(true)
                .emailNotifications(true)
                .pushNotifications(true)
                .build();
        
        return notificationPreferenceRepository.save(preference);
    }

    private NotificationDto convertToDto(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(NotificationDto.NotificationType.valueOf(notification.getType().name()))
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .data(notification.getData())
                .build();
    }

    private Notification convertToEntity(NotificationDto dto) {
        return Notification.builder()
                .userId(dto.getUserId())
                .type(Notification.NotificationType.valueOf(dto.getType().name()))
                .title(dto.getTitle())
                .message(dto.getMessage())
                .read(dto.isRead())
                .data(dto.getData())
                .build();
    }

    private NotificationPreferenceDto convertToDto(NotificationPreference preference) {
        return NotificationPreferenceDto.builder()
                .userId(preference.getUserId())
                .transactionNotifications(preference.isTransactionNotifications())
                .walletNotifications(preference.isWalletNotifications())
                .kycNotifications(preference.isKycNotifications())
                .rateAlertNotifications(preference.isRateAlertNotifications())
                .securityNotifications(preference.isSecurityNotifications())
                .systemNotifications(preference.isSystemNotifications())
                .emailNotifications(preference.isEmailNotifications())
                .pushNotifications(preference.isPushNotifications())
                .build();
    }
}
