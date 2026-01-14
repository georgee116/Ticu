package org.example.Notification.controller;

import org.example.Notification.dto.request.NotificationCreateDto;
import org.example.Notification.dto.request.NotificationUpdateDto;
import org.example.Notification.dto.response.NotificationDto;
import org.example.Notification.dto.response.NotificationStatusDto;
import org.example.Notification.entity.Notification;
import org.example.Notification.repository.NotificationRepository;
import org.example.Notification.service.INotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/notifications")
public class NotificationController {

    @Autowired
    private INotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    // ========== ENDPOINTS DE TEST (fără autentificare) ==========

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Notification service is working! ✅");
    }

    @GetMapping("/test-auth")
    public ResponseEntity<Map<String, Object>> testAuth(@AuthenticationPrincipal OidcUser oidcUser) {
        Map<String, Object> response = new HashMap<>();

        if (oidcUser != null) {
            response.put("status", "Authenticated ✅");
            response.put("email", oidcUser.getEmail());
            response.put("name", oidcUser.getFullName());
            response.put("authorities", oidcUser.getAuthorities());
        } else {
            response.put("status", "Not authenticated ❌");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Notification>> getAllNotifications() {
        List<Notification> notifications = notificationRepository.findAll();
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/test-db")
    public ResponseEntity<String> testDatabase() {
        try {
            long count = notificationRepository.count();
            return ResponseEntity.ok("Connected! Found " + count + " notifications in database");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Database error: " + e.getMessage());
        }
    }

    // ========== ENDPOINTS PRINCIPALE ==========

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<NotificationDto> createNotification(@RequestBody NotificationCreateDto notificationCreateDto) {
        NotificationDto createdNotification = notificationService.createNotification(notificationCreateDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdNotification);
    }

    @GetMapping("/get/{notificationId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<NotificationDto> fetchNotification(@PathVariable("notificationId") String notificationId) {
        NotificationDto notification = notificationService.fetchNotification(notificationId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(notification);
    }

    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<NotificationDto> updateNotificationSettings(@RequestBody NotificationUpdateDto notificationUpdateDto) {
        NotificationDto updatedNotification = notificationService.updateNotificationSettings(notificationUpdateDto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(updatedNotification);
    }

    @DeleteMapping("/delete-expired")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteExpiredNotifications(@RequestParam(defaultValue = "30") int retentionDays) {
        boolean deleted = notificationService.deleteExpiredNotifications(retentionDays);
        if (deleted) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("Expired notifications deleted successfully");
        } else {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("No expired notifications found");
        }
    }

    @PostMapping("/resend-failed/{notificationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> resendFailedNotification(@PathVariable("notificationId") String notificationId) {
        String result = notificationService.resendFailedNotification(notificationId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(result);
    }

    @PostMapping("/schedule")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<NotificationDto> scheduleNotification(@RequestBody NotificationCreateDto notificationCreateDto) {
        NotificationDto scheduledNotification = notificationService.scheduleNotification(notificationCreateDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(scheduledNotification);
    }

    @PostMapping("/send-sms/{notificationId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<String> sendSmsNotification(@PathVariable("notificationId") String notificationId) {
        String result = notificationService.sendSmsNotification(notificationId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(result);
    }

    @PostMapping("/send-email/{notificationId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<String> sendEmailNotification(@PathVariable("notificationId") String notificationId) {
        String result = notificationService.sendEmailNotification(notificationId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(result);
    }

    @PatchMapping("/mark-read/{notificationId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<String> markAsRead(@PathVariable("notificationId") String notificationId) {
        String result = notificationService.markAsRead(notificationId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(result);
    }

    @GetMapping("/status/{notificationId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<NotificationStatusDto> getNotificationStatus(@PathVariable("notificationId") String notificationId) {
        NotificationStatusDto status = notificationService.getNotificationStatus(notificationId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(status);
    }

    @GetMapping("/history/{recipientId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<List<NotificationDto>> getNotificationHistory(@PathVariable("recipientId") Long recipientId) {
        List<NotificationDto> history = notificationService.getNotificationHistory(recipientId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(history);
    }

    @PostMapping("/create-for-transaction/{transactionId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<NotificationDto> createNotificationForTransaction(
            @PathVariable("transactionId") String transactionId,
            @RequestBody NotificationCreateDto notificationCreateDto) {
        NotificationDto notification = notificationService.createNotificationForTransaction(transactionId, notificationCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }

    @PostMapping("/create-for-account/{accountNumber}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<NotificationDto> createNotificationForAccount(
            @PathVariable("accountNumber") String accountNumber,
            @RequestBody NotificationCreateDto notificationCreateDto) {
        NotificationDto notification = notificationService.createNotificationForAccount(accountNumber, notificationCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }
}