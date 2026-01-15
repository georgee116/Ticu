package org.example.Notification.controller;

import org.example.Notification.dto.request.NotificationCreateDto;
import org.example.Notification.dto.request.NotificationUpdateDto;
import org.example.Notification.dto.response.NotificationDto;
import org.example.Notification.dto.response.NotificationStatusDto;
import org.example.Notification.entity.Notification;
import org.example.Notification.enums.NotificationStatus;
import org.example.Notification.enums.NotificationType;
import org.example.Notification.enums.NotificationPriority;
import org.example.Notification.enums.TriggerEvent;
import org.example.Notification.repository.NotificationRepository;
import org.example.Notification.service.INotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private INotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private OidcUser oidcUser;

    @InjectMocks
    private NotificationController notificationController;

    private NotificationCreateDto notificationCreateDto;
    private NotificationUpdateDto notificationUpdateDto;
    private NotificationDto notificationDto;
    private NotificationStatusDto notificationStatusDto;
    private Notification notification;

    @BeforeEach
    void setUp() {
        // Setup NotificationCreateDto
        notificationCreateDto = new NotificationCreateDto();
        notificationCreateDto.setRecipientId(123L);
        notificationCreateDto.setRecipientEmail("test@example.com");
        notificationCreateDto.setRecipientPhone("+40721234567");
        notificationCreateDto.setNotificationType(NotificationType.EMAIL);
        notificationCreateDto.setTriggerEvent(TriggerEvent.ACCOUNT_CREATED.name());
        notificationCreateDto.setSubject("Test Subject");
        notificationCreateDto.setMessage("Test Message");
        notificationCreateDto.setPriority(NotificationPriority.HIGH);

        // Setup NotificationUpdateDto
        notificationUpdateDto = new NotificationUpdateDto();
        notificationUpdateDto.setNotificationId("NOTIF-123");
        notificationUpdateDto.setRecipientEmail("updated@example.com");

        // Setup NotificationDto
        notificationDto = new NotificationDto();
        notificationDto.setNotificationId("NOTIF-123");
        notificationDto.setRecipientId(123L);
        notificationDto.setRecipientEmail("test@example.com");
        notificationDto.setStatus(NotificationStatus.PENDING);
        notificationDto.setCreatedAt(LocalDateTime.now());

        // Setup NotificationStatusDto
        notificationStatusDto = new NotificationStatusDto();
        notificationStatusDto.setNotificationId("NOTIF-123");
        notificationStatusDto.setStatus(NotificationStatus.SENT);

        // Setup Notification Entity
        notification = new Notification();
        notification.setNotificationId("NOTIF-123");
        notification.setRecipientId(123L);
        notification.setStatus(NotificationStatus.PENDING);
    }

    // ========== TEST ENDPOINTS ==========

    @Test
    void testTestEndpoint() {
        // When
        ResponseEntity<String> response = notificationController.test();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("working"));
    }

    @Test
    void testTestAuthWithAuthenticatedUser() {
        // Given
        when(oidcUser.getEmail()).thenReturn("test@example.com");
        when(oidcUser.getFullName()).thenReturn("Test User");
        when(oidcUser.getAuthorities()).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<Map<String, Object>> response = notificationController.testAuth(oidcUser);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().containsKey("status"));
        assertTrue(response.getBody().get("status").toString().contains("Authenticated"));
    }

    @Test
    void testTestAuthWithoutAuthentication() {
        // When
        ResponseEntity<Map<String, Object>> response = notificationController.testAuth(null);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().containsKey("status"));
        assertTrue(response.getBody().get("status").toString().contains("Not authenticated"));
    }

    @Test
    void testGetAllNotifications() {
        // Given
        List<Notification> notifications = Arrays.asList(notification);
        when(notificationRepository.findAll()).thenReturn(notifications);

        // When
        ResponseEntity<List<Notification>> response = notificationController.getAllNotifications();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(notificationRepository, times(1)).findAll();
    }

    @Test
    void testGetAllNotificationsEmpty() {
        // Given
        when(notificationRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<List<Notification>> response = notificationController.getAllNotifications();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void testTestDatabaseSuccess() {
        // Given
        when(notificationRepository.count()).thenReturn(5L);

        // When
        ResponseEntity<String> response = notificationController.testDatabase();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Connected"));
        assertTrue(response.getBody().contains("5"));
    }

    @Test
    void testTestDatabaseError() {
        // Given
        when(notificationRepository.count()).thenThrow(new RuntimeException("Connection failed"));

        // When
        ResponseEntity<String> response = notificationController.testDatabase();

        // Then
        assertEquals(500, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Database error"));
    }

    // ========== MAIN ENDPOINTS ==========

    @Test
    void testCreateNotification() {
        // Given
        when(notificationService.createNotification(any(NotificationCreateDto.class)))
                .thenReturn(notificationDto);

        // When
        ResponseEntity<NotificationDto> response = notificationController.createNotification(notificationCreateDto);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("NOTIF-123", response.getBody().getNotificationId());
        verify(notificationService, times(1)).createNotification(any(NotificationCreateDto.class));
    }

    @Test
    void testFetchNotification() {
        // Given
        when(notificationService.fetchNotification("NOTIF-123"))
                .thenReturn(notificationDto);

        // When
        ResponseEntity<NotificationDto> response = notificationController.fetchNotification("NOTIF-123");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("NOTIF-123", response.getBody().getNotificationId());
        verify(notificationService, times(1)).fetchNotification("NOTIF-123");
    }

    @Test
    void testUpdateNotificationSettings() {
        // Given
        when(notificationService.updateNotificationSettings(any(NotificationUpdateDto.class)))
                .thenReturn(notificationDto);

        // When
        ResponseEntity<NotificationDto> response = notificationController.updateNotificationSettings(notificationUpdateDto);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(notificationService, times(1)).updateNotificationSettings(any(NotificationUpdateDto.class));
    }

    @Test
    void testDeleteExpiredNotificationsSuccess() {
        // Given
        when(notificationService.deleteExpiredNotifications(30)).thenReturn(true);

        // When
        ResponseEntity<String> response = notificationController.deleteExpiredNotifications(30);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("deleted successfully"));
        verify(notificationService, times(1)).deleteExpiredNotifications(30);
    }

    @Test
    void testDeleteExpiredNotificationsNoNotificationsFound() {
        // Given
        when(notificationService.deleteExpiredNotifications(30)).thenReturn(false);

        // When
        ResponseEntity<String> response = notificationController.deleteExpiredNotifications(30);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("No expired notifications"));
        verify(notificationService, times(1)).deleteExpiredNotifications(30);
    }

    @Test
    void testResendFailedNotification() {
        // Given
        when(notificationService.resendFailedNotification("NOTIF-123"))
                .thenReturn("Notification resent successfully");

        // When
        ResponseEntity<String> response = notificationController.resendFailedNotification("NOTIF-123");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("successfully"));
        verify(notificationService, times(1)).resendFailedNotification("NOTIF-123");
    }

    @Test
    void testScheduleNotification() {
        // Given
        when(notificationService.scheduleNotification(any(NotificationCreateDto.class)))
                .thenReturn(notificationDto);

        // When
        ResponseEntity<NotificationDto> response = notificationController.scheduleNotification(notificationCreateDto);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(notificationService, times(1)).scheduleNotification(any(NotificationCreateDto.class));
    }

    @Test
    void testSendSmsNotification() {
        // Given
        when(notificationService.sendSmsNotification("NOTIF-123"))
                .thenReturn("SMS sent successfully");

        // When
        ResponseEntity<String> response = notificationController.sendSmsNotification("NOTIF-123");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("SMS"));
        verify(notificationService, times(1)).sendSmsNotification("NOTIF-123");
    }

    @Test
    void testSendEmailNotification() {
        // Given
        when(notificationService.sendEmailNotification("NOTIF-123"))
                .thenReturn("Email sent successfully");

        // When
        ResponseEntity<String> response = notificationController.sendEmailNotification("NOTIF-123");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Email"));
        verify(notificationService, times(1)).sendEmailNotification("NOTIF-123");
    }

    @Test
    void testMarkAsRead() {
        // Given
        when(notificationService.markAsRead("NOTIF-123"))
                .thenReturn("Marked as read successfully");

        // When
        ResponseEntity<String> response = notificationController.markAsRead("NOTIF-123");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("read"));
        verify(notificationService, times(1)).markAsRead("NOTIF-123");
    }

    @Test
    void testGetNotificationStatus() {
        // Given
        when(notificationService.getNotificationStatus("NOTIF-123"))
                .thenReturn(notificationStatusDto);

        // When
        ResponseEntity<NotificationStatusDto> response = notificationController.getNotificationStatus("NOTIF-123");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("NOTIF-123", response.getBody().getNotificationId());
        verify(notificationService, times(1)).getNotificationStatus("NOTIF-123");
    }

    @Test
    void testGetNotificationHistory() {
        // Given
        List<NotificationDto> history = Arrays.asList(notificationDto);
        when(notificationService.getNotificationHistory(123L))
                .thenReturn(history);

        // When
        ResponseEntity<List<NotificationDto>> response = notificationController.getNotificationHistory(123L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(notificationService, times(1)).getNotificationHistory(123L);
    }

    @Test
    void testGetNotificationHistoryEmpty() {
        // Given
        when(notificationService.getNotificationHistory(999L))
                .thenReturn(Collections.emptyList());

        // When
        ResponseEntity<List<NotificationDto>> response = notificationController.getNotificationHistory(999L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void testCreateNotificationForTransaction() {
        // Given
        when(notificationService.createNotificationForTransaction(eq("TXN-123"), any(NotificationCreateDto.class)))
                .thenReturn(notificationDto);

        // When
        ResponseEntity<NotificationDto> response = notificationController
                .createNotificationForTransaction("TXN-123", notificationCreateDto);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(notificationService, times(1))
                .createNotificationForTransaction(eq("TXN-123"), any(NotificationCreateDto.class));
    }

    @Test
    void testCreateNotificationForAccount() {
        // Given
        when(notificationService.createNotificationForAccount(eq("ACC-123"), any(NotificationCreateDto.class)))
                .thenReturn(notificationDto);

        // When
        ResponseEntity<NotificationDto> response = notificationController
                .createNotificationForAccount("ACC-123", notificationCreateDto);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(notificationService, times(1))
                .createNotificationForAccount(eq("ACC-123"), any(NotificationCreateDto.class));
    }

    // ========== ADDITIONAL EDGE CASES ==========

    @Test
    void testDeleteExpiredNotificationsWithDifferentRetentionDays() {
        // Given
        when(notificationService.deleteExpiredNotifications(60)).thenReturn(true);

        // When
        ResponseEntity<String> response = notificationController.deleteExpiredNotifications(60);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(notificationService, times(1)).deleteExpiredNotifications(60);
    }

    @Test
    void testFetchNotificationWithDifferentId() {
        // Given
        NotificationDto anotherDto = new NotificationDto();
        anotherDto.setNotificationId("NOTIF-999");
        when(notificationService.fetchNotification("NOTIF-999"))
                .thenReturn(anotherDto);

        // When
        ResponseEntity<NotificationDto> response = notificationController.fetchNotification("NOTIF-999");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("NOTIF-999", response.getBody().getNotificationId());
    }

    @Test
    void testMarkAsReadWithDifferentNotificationId() {
        // Given
        when(notificationService.markAsRead("NOTIF-456"))
                .thenReturn("Success");

        // When
        ResponseEntity<String> response = notificationController.markAsRead("NOTIF-456");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(notificationService, times(1)).markAsRead("NOTIF-456");
    }

    @Test
    void testGetNotificationHistoryMultipleNotifications() {

        NotificationDto dto1 = new NotificationDto();
        dto1.setNotificationId("NOTIF-1");
        NotificationDto dto2 = new NotificationDto();
        dto2.setNotificationId("NOTIF-2");

        List<NotificationDto> history = Arrays.asList(dto1, dto2);
        when(notificationService.getNotificationHistory(123L))
                .thenReturn(history);


        ResponseEntity<List<NotificationDto>> response = notificationController.getNotificationHistory(123L);


        assertEquals(2, response.getBody().size());
    }
}