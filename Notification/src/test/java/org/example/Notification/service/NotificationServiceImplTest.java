package org.example.Notification.service;

import org.example.Notification.dto.request.NotificationCreateDto;
import org.example.Notification.dto.response.NotificationDto;
import org.example.Notification.entity.Notification;
import org.example.Notification.enums.NotificationStatus;
import org.example.Notification.enums.NotificationType;
import org.example.Notification.enums.NotificationPriority;
import org.example.Notification.enums.TriggerEvent;
import org.example.Notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private NotificationCreateDto notificationCreateDto;
    private Notification notification;

    @BeforeEach
    void setUp() {
        // Pregătim datele de test
        notificationCreateDto = new NotificationCreateDto();
        notificationCreateDto.setRecipientId(123L);
        notificationCreateDto.setRecipientEmail("test@example.com");
        notificationCreateDto.setRecipientPhone("+40721234567");
        notificationCreateDto.setNotificationType(NotificationType.EMAIL);
        notificationCreateDto.setTriggerEvent(TriggerEvent.ACCOUNT_CREATED.name());
        notificationCreateDto.setSubject("Test Subject");
        notificationCreateDto.setMessage("Test Message");
        notificationCreateDto.setPriority(NotificationPriority.HIGH);

        notification = new Notification();
        notification.setId(1L);
        notification.setNotificationId("NOTIF-123");
        notification.setRecipientId(123L);
        notification.setRecipientEmail("test@example.com");
        notification.setStatus(NotificationStatus.PENDING);
        notification.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateNotification_Success() {
        // Given
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // When
        NotificationDto result = notificationService.createNotification(notificationCreateDto);

        // Then
        assertNotNull(result);
        assertEquals("NOTIF-123", result.getNotificationId());
        assertEquals(123L, result.getRecipientId());
        assertEquals("test@example.com", result.getRecipientEmail());
        assertEquals(NotificationStatus.PENDING, result.getStatus());

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testFetchNotification_Success() {
        // Given
        String notificationId = "NOTIF-123";
        when(notificationRepository.findByNotificationId(notificationId))
                .thenReturn(Optional.of(notification));

        // When
        NotificationDto result = notificationService.fetchNotification(notificationId);

        // Then
        assertNotNull(result);
        assertEquals(notificationId, result.getNotificationId());
        verify(notificationRepository, times(1)).findByNotificationId(notificationId);
    }

    @Test
    void testFetchNotification_NotFound() {
        // Given
        String notificationId = "NOTIF-999";
        when(notificationRepository.findByNotificationId(notificationId))
                .thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> notificationService.fetchNotification(notificationId));

        assertTrue(exception.getMessage().contains("Notification not found"));
        verify(notificationRepository, times(1)).findByNotificationId(notificationId);
    }

    @Test
    void testGetNotificationHistory_Success() {
        // Given
        Long recipientId = 123L;
        List<Notification> notifications = Arrays.asList(notification, notification);
        when(notificationRepository.findByRecipientId(recipientId))
                .thenReturn(notifications);

        // When
        List<NotificationDto> result = notificationService.getNotificationHistory(recipientId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(notificationRepository, times(1)).findByRecipientId(recipientId);
    }

    @Test
    void testMarkAsRead_Success() {
        // Given
        String notificationId = "NOTIF-123";
        notification.setStatus(NotificationStatus.SENT);
        when(notificationRepository.findByNotificationId(notificationId))
                .thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(notification);

        // When
        String result = notificationService.markAsRead(notificationId);

        // Then
        assertEquals("Notification marked as read successfully", result);
        assertEquals(NotificationStatus.READ, notification.getStatus());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }
}