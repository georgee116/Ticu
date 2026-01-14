package org.example.Notification.controller;

import org.example.Notification.dto.request.NotificationCreateDto;
import org.example.Notification.dto.response.NotificationDto;
import org.example.Notification.enums.NotificationStatus;
import org.example.Notification.enums.NotificationType;
import org.example.Notification.enums.NotificationPriority;
import org.example.Notification.enums.TriggerEvent;
import org.example.Notification.service.INotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private INotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private NotificationCreateDto notificationCreateDto;
    private NotificationDto notificationDto;

    @BeforeEach
    void setUp() {
        notificationCreateDto = new NotificationCreateDto();
        notificationCreateDto.setRecipientId(123L);
        notificationCreateDto.setRecipientEmail("test@example.com");
        notificationCreateDto.setRecipientPhone("+40721234567");
        notificationCreateDto.setNotificationType(NotificationType.EMAIL);
        notificationCreateDto.setTriggerEvent(TriggerEvent.ACCOUNT_CREATED.name());
        notificationCreateDto.setSubject("Test Subject");
        notificationCreateDto.setMessage("Test Message");
        notificationCreateDto.setPriority(NotificationPriority.HIGH);

        notificationDto = new NotificationDto();
        notificationDto.setNotificationId("NOTIF-123");
        notificationDto.setRecipientId(123L);
        notificationDto.setRecipientEmail("test@example.com");
        notificationDto.setStatus(NotificationStatus.PENDING);
        notificationDto.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateNotification_Success() {
        // Given
        when(notificationService.createNotification(any(NotificationCreateDto.class)))
                .thenReturn(notificationDto);

        // When
        ResponseEntity<NotificationDto> response = notificationController.createNotification(notificationCreateDto);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("NOTIF-123", response.getBody().getNotificationId());
        assertEquals("test@example.com", response.getBody().getRecipientEmail());

        verify(notificationService, times(1)).createNotification(any(NotificationCreateDto.class));
    }

    @Test
    void testFetchNotification_Success() {
        // Given
        String notificationId = "NOTIF-123";
        when(notificationService.fetchNotification(eq(notificationId)))
                .thenReturn(notificationDto);

        // When
        ResponseEntity<NotificationDto> response = notificationController.fetchNotification(notificationId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("NOTIF-123", response.getBody().getNotificationId());

        verify(notificationService, times(1)).fetchNotification(notificationId);
    }

    @Test
    void testGetNotificationHistory_Success() {
        // Given
        Long recipientId = 123L;
        List<NotificationDto> notifications = Arrays.asList(notificationDto, notificationDto);
        when(notificationService.getNotificationHistory(eq(recipientId)))
                .thenReturn(notifications);

        // When
        ResponseEntity<List<NotificationDto>> response = notificationController.getNotificationHistory(recipientId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());

        verify(notificationService, times(1)).getNotificationHistory(recipientId);
    }

    @Test
    void testDeleteExpiredNotifications_Success() {
        // Given
        when(notificationService.deleteExpiredNotifications(30))
                .thenReturn(true);

        // When
        ResponseEntity<String> response = notificationController.deleteExpiredNotifications(30);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Expired notifications deleted successfully", response.getBody());

        verify(notificationService, times(1)).deleteExpiredNotifications(30);
    }
}