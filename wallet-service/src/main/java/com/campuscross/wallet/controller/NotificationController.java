package com.campuscross.wallet.controller;

import com.campuscross.wallet.dto.NotificationDto;
import com.campuscross.wallet.dto.NotificationPreferenceDto;
import com.campuscross.wallet.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER') or (hasRole('ADMIN') and #userId == authentication.principal.userId)")
    public ResponseEntity<List<NotificationDto>> getUserNotifications(@PathVariable String userId) {
        try {
            List<NotificationDto> notifications = notificationService.getUserNotifications(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            log.error("Failed to fetch notifications for user: {}", userId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{notificationId}/read")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> markNotificationAsRead(@PathVariable String notificationId) {
        try {
            notificationService.markNotificationAsRead(notificationId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to mark notification as read: {}", notificationId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/user/{userId}/read-all")
    @PreAuthorize("hasRole('USER') or (hasRole('ADMIN') and #userId == authentication.principal.userId)")
    public ResponseEntity<Void> markAllNotificationsAsRead(@PathVariable String userId) {
        try {
            notificationService.markAllNotificationsAsRead(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to mark all notifications as read for user: {}", userId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{notificationId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteNotification(@PathVariable String notificationId) {
        try {
            notificationService.deleteNotification(notificationId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to delete notification: {}", notificationId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/preferences/{userId}")
    @PreAuthorize("hasRole('USER') or (hasRole('ADMIN') and #userId == authentication.principal.userId)")
    public ResponseEntity<NotificationPreferenceDto> getNotificationPreferences(@PathVariable String userId) {
        try {
            NotificationPreferenceDto preferences = notificationService.getNotificationPreferences(userId);
            return ResponseEntity.ok(preferences);
        } catch (Exception e) {
            log.error("Failed to fetch notification preferences for user: {}", userId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/preferences")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> updateNotificationPreferences(@RequestBody NotificationPreferenceDto preferences) {
        try {
            notificationService.updateNotificationPreferences(preferences);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to update notification preferences", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> createNotification(@RequestBody NotificationDto notification) {
        try {
            notificationService.createNotification(notification);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to create notification", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
