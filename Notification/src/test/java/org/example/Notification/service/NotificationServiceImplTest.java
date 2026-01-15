package org.example.Notification.service;

import feign.FeignException;
import org.example.Notification.client.AccountClient;
import org.example.Notification.client.TransactionClient;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private TransactionClient transactionClient;

    @Mock
    private AccountClient accountClient;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private NotificationCreateDto notificationCreateDto;
    private NotificationUpdateDto notificationUpdateDto;
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

        // Setup Notification Entity
        notification = new Notification();
        notification.setId(1L);
        notification.setNotificationId("NOTIF-123");
        notification.setRecipientId(123L);
        notification.setRecipientEmail("test@example.com");
        notification.setRecipientPhone("+40721234567");
        notification.setNotificationType(NotificationType.EMAIL);
        notification.setTriggerEvent(TriggerEvent.ACCOUNT_CREATED.name()); // ✅ String
        notification.setSubject("Test Subject");
        notification.setMessage("Test Message");
        notification.setPriority(NotificationPriority.HIGH);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRetryCount(0);
        notification.setMaxRetries(3);
    }

    // ========== CREATE NOTIFICATION ==========

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
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    // ========== CREATE FOR TRANSACTION ==========

    @Test
    void testCreateNotificationForTransaction_Success() {
        // Given
        ResponseEntity<Object> transactionResponse = ResponseEntity.ok().body(new Object());
        when(transactionClient.getTransaction("TXN-123")).thenReturn(transactionResponse);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // When
        NotificationDto result = notificationService.createNotificationForTransaction("TXN-123", notificationCreateDto);

        // Then
        assertNotNull(result);
        verify(transactionClient, times(1)).getTransaction("TXN-123");
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testCreateNotificationForTransaction_TransactionNotFound() {
        // Given
        ResponseEntity<Object> transactionResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        when(transactionClient.getTransaction("TXN-999")).thenReturn(transactionResponse);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> notificationService.createNotificationForTransaction("TXN-999", notificationCreateDto));

        assertTrue(exception.getMessage().contains("Transaction not found"));
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void testCreateNotificationForTransaction_FeignException() {
        // Given
        when(transactionClient.getTransaction(anyString()))
                .thenThrow(FeignException.class);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> notificationService.createNotificationForTransaction("TXN-123", notificationCreateDto));

        assertTrue(exception.getMessage().contains("Error communicating with Transaction service"));
    }

    // ========== CREATE FOR ACCOUNT ==========

    @Test
    void testCreateNotificationForAccount_Success() {
        // Given
        ResponseEntity<Object> accountResponse = ResponseEntity.ok().body(new Object());
        when(accountClient.getAccount("ACC-123")).thenReturn(accountResponse);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // When
        NotificationDto result = notificationService.createNotificationForAccount("ACC-123", notificationCreateDto);

        // Then
        assertNotNull(result);
        verify(accountClient, times(1)).getAccount("ACC-123");
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testCreateNotificationForAccount_AccountNotFound() {
        // Given
        ResponseEntity<Object> accountResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        when(accountClient.getAccount("ACC-999")).thenReturn(accountResponse);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> notificationService.createNotificationForAccount("ACC-999", notificationCreateDto));

        assertTrue(exception.getMessage().contains("Account verification failed"));
    }

    @Test
    void testCreateNotificationForAccount_FeignException() {
        // Given
        when(accountClient.getAccount(anyString()))
                .thenThrow(FeignException.class);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> notificationService.createNotificationForAccount("ACC-123", notificationCreateDto));

        assertTrue(exception.getMessage().contains("Error communicating with Account service"));
    }

    // ========== CALCULATE FEES AND NOTIFY ==========

    @Test
    void testCalculateFeesAndNotify_Success() {
        // Given
        ResponseEntity<String> feesResponse = ResponseEntity.ok("Fee: $5.00");
        when(transactionClient.calculateFees("TXN-123")).thenReturn(feesResponse);

        notification.setNotificationType(NotificationType.EMAIL);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationRepository.findByNotificationId(anyString())).thenReturn(Optional.of(notification));

        // When
        String result = notificationService.calculateFeesAndNotify("TXN-123", notificationCreateDto);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Fees calculated"));
        verify(transactionClient, times(1)).calculateFees("TXN-123");
    }

    @Test
    void testCalculateFeesAndNotify_FeesCalculationFailed() {
        // Given
        ResponseEntity<String> feesResponse = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        when(transactionClient.calculateFees("TXN-999")).thenReturn(feesResponse);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> notificationService.calculateFeesAndNotify("TXN-999", notificationCreateDto));

        assertTrue(exception.getMessage().contains("Failed to calculate fees"));
    }

    @Test
    void testCalculateFeesAndNotify_FeignException() {
        // Given
        when(transactionClient.calculateFees(anyString()))
                .thenThrow(FeignException.class);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> notificationService.calculateFeesAndNotify("TXN-123", notificationCreateDto));

        assertTrue(exception.getMessage().contains("Error communicating with Transaction service"));
    }

    // ========== FRAUD CHECK AND NOTIFY ==========

    @Test
    void testCheckFraudAndNotify_Success() {
        // Given
        ResponseEntity<String> fraudResponse = ResponseEntity.ok("Fraud score: 0.1");
        when(transactionClient.antiFraudCheck("TXN-123")).thenReturn(fraudResponse);

        notification.setNotificationType(NotificationType.EMAIL);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationRepository.findByNotificationId(anyString())).thenReturn(Optional.of(notification));

        // When
        String result = notificationService.checkFraudAndNotify("TXN-123", notificationCreateDto);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Fraud check completed"));
        verify(transactionClient, times(1)).antiFraudCheck("TXN-123");
    }

    @Test
    void testCheckFraudAndNotify_FraudCheckFailed() {
        // Given
        ResponseEntity<String> fraudResponse = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        when(transactionClient.antiFraudCheck("TXN-999")).thenReturn(fraudResponse);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> notificationService.checkFraudAndNotify("TXN-999", notificationCreateDto));

        assertTrue(exception.getMessage().contains("Fraud check failed"));
    }

    @Test
    void testCheckFraudAndNotify_FeignException() {
        // Given
        when(transactionClient.antiFraudCheck(anyString()))
                .thenThrow(FeignException.class);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> notificationService.checkFraudAndNotify("TXN-123", notificationCreateDto));

        assertTrue(exception.getMessage().contains("Error communicating with Transaction service"));
    }

    // ========== FETCH NOTIFICATION ==========

    @Test
    void testFetchNotification_Success() {
        // Given
        when(notificationRepository.findByNotificationId("NOTIF-123"))
                .thenReturn(Optional.of(notification));

        // When
        NotificationDto result = notificationService.fetchNotification("NOTIF-123");

        // Then
        assertNotNull(result);
        assertEquals("NOTIF-123", result.getNotificationId());
        verify(notificationRepository, times(1)).findByNotificationId("NOTIF-123");
    }

    @Test
    void testFetchNotification_NotFound() {
        // Given
        when(notificationRepository.findByNotificationId("NOTIF-999"))
                .thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> notificationService.fetchNotification("NOTIF-999"));

        assertTrue(exception.getMessage().contains("Notification not found"));
    }

    // ========== UPDATE NOTIFICATION ==========

    @Test
    void testUpdateNotificationSettings_Success() {
        // Given
        when(notificationRepository.findByNotificationId("NOTIF-123"))
                .thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(notification);

        // When
        NotificationDto result = notificationService.updateNotificationSettings(notificationUpdateDto);

        // Then
        assertNotNull(result);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    // ========== DELETE EXPIRED ==========

    @Test
    void testDeleteExpiredNotifications_Success() {
        // Given
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        List<Notification> oldNotifications = Arrays.asList(notification);
        when(notificationRepository.findByCreatedAtBefore(any(LocalDateTime.class)))
                .thenReturn(oldNotifications);
        doNothing().when(notificationRepository).deleteOldNotifications(any(LocalDateTime.class));

        // When
        boolean result = notificationService.deleteExpiredNotifications(30);

        // Then
        assertTrue(result);
        verify(notificationRepository, times(1)).deleteOldNotifications(any(LocalDateTime.class));
    }

    @Test
    void testDeleteExpiredNotifications_NoExpiredNotifications() {
        // Given
        when(notificationRepository.findByCreatedAtBefore(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // When
        boolean result = notificationService.deleteExpiredNotifications(30);

        // Then
        assertFalse(result);
        verify(notificationRepository, never()).deleteOldNotifications(any(LocalDateTime.class));
    }

    // ========== RESEND FAILED ==========

    @Test
    void testResendFailedNotification_Success() {
        // Given
        notification.setStatus(NotificationStatus.FAILED);
        notification.setRetryCount(1);
        notification.setMaxRetries(3);
        when(notificationRepository.findByNotificationId("NOTIF-123"))
                .thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(notification);

        // When
        String result = notificationService.resendFailedNotification("NOTIF-123");

        // Then
        assertTrue(result.contains("resent successfully"));
        assertEquals(NotificationStatus.PENDING, notification.getStatus());
        verify(notificationRepository, times(1)).save(notification);
    }

    @Test
    void testResendFailedNotification_NotInFailedStatus() {
        // Given
        notification.setStatus(NotificationStatus.SENT);
        when(notificationRepository.findByNotificationId("NOTIF-123"))
                .thenReturn(Optional.of(notification));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> notificationService.resendFailedNotification("NOTIF-123"));

        assertTrue(exception.getMessage().contains("not in FAILED status"));
    }

    @Test
    void testResendFailedNotification_MaxRetriesReached() {
        // Given
        notification.setStatus(NotificationStatus.FAILED);
        notification.setRetryCount(3);
        notification.setMaxRetries(3);
        when(notificationRepository.findByNotificationId("NOTIF-123"))
                .thenReturn(Optional.of(notification));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> notificationService.resendFailedNotification("NOTIF-123"));

        assertTrue(exception.getMessage().contains("Maximum retry attempts"));
    }

    // ========== SCHEDULE NOTIFICATION ==========

    @Test
    void testScheduleNotification_Success() {
        // Given
        notificationCreateDto.setScheduledAt(LocalDateTime.now().plusHours(1));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // When
        NotificationDto result = notificationService.scheduleNotification(notificationCreateDto);

        // Then
        assertNotNull(result);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testScheduleNotification_NoScheduledTime() {
        // Given
        notificationCreateDto.setScheduledAt(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> notificationService.scheduleNotification(notificationCreateDto));

        assertTrue(exception.getMessage().contains("Scheduled time must be provided"));
    }

    @Test
    void testScheduleNotification_PastTime() {
        // Given
        notificationCreateDto.setScheduledAt(LocalDateTime.now().minusHours(1));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> notificationService.scheduleNotification(notificationCreateDto));

        assertTrue(exception.getMessage().contains("must be in the future"));
    }

    // ========== SEND SMS ==========

    @Test
    void testSendSmsNotification_Success() {
        // Given
        notification.setNotificationType(NotificationType.SMS);
        notification.setRecipientPhone("+40721234567");
        when(notificationRepository.findByNotificationId("NOTIF-123"))
                .thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(notification);

        // When
        String result = notificationService.sendSmsNotification("NOTIF-123");

        // Then
        assertTrue(result.contains("SMS sent successfully"));
        assertEquals(NotificationStatus.SENT, notification.getStatus());
    }

    @Test
    void testSendSmsNotification_WrongType() {
        // Given
        notification.setNotificationType(NotificationType.EMAIL);
        when(notificationRepository.findByNotificationId("NOTIF-123"))
                .thenReturn(Optional.of(notification));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> notificationService.sendSmsNotification("NOTIF-123"));

        assertTrue(exception.getMessage().contains("not of type SMS"));
    }

    @Test
    void testSendSmsNotification_MissingPhone() {
        // Given
        notification.setNotificationType(NotificationType.SMS);
        notification.setRecipientPhone(null);
        when(notificationRepository.findByNotificationId("NOTIF-123"))
                .thenReturn(Optional.of(notification));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> notificationService.sendSmsNotification("NOTIF-123"));

        assertTrue(exception.getMessage().contains("phone number is missing"));
    }

    // ========== SEND EMAIL ==========

    @Test
    void testSendEmailNotification_Success() {
        // Given
        notification.setNotificationType(NotificationType.EMAIL);
        notification.setRecipientEmail("test@example.com");
        when(notificationRepository.findByNotificationId("NOTIF-123"))
                .thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(notification);

        // When
        String result = notificationService.sendEmailNotification("NOTIF-123");

        // Then
        assertTrue(result.contains("Email sent successfully"));
        assertEquals(NotificationStatus.SENT, notification.getStatus());
    }

    @Test
    void testSendEmailNotification_WrongType() {
        // Given
        notification.setNotificationType(NotificationType.SMS);
        when(notificationRepository.findByNotificationId("NOTIF-123"))
                .thenReturn(Optional.of(notification));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> notificationService.sendEmailNotification("NOTIF-123"));

        assertTrue(exception.getMessage().contains("not of type EMAIL"));
    }

    @Test
    void testSendEmailNotification_MissingEmail() {
        // Given
        notification.setNotificationType(NotificationType.EMAIL);
        notification.setRecipientEmail(null);
        when(notificationRepository.findByNotificationId("NOTIF-123"))
                .thenReturn(Optional.of(notification));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> notificationService.sendEmailNotification("NOTIF-123"));

        assertTrue(exception.getMessage().contains("email is missing"));
    }

    // ========== MARK AS READ ==========

    @Test
    void testMarkAsRead_Success() {
        // Given
        notification.setStatus(NotificationStatus.SENT);
        when(notificationRepository.findByNotificationId("NOTIF-123"))
                .thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(notification);

        // When
        String result = notificationService.markAsRead("NOTIF-123");

        // Then
        assertTrue(result.contains("marked as read successfully"));
        assertEquals(NotificationStatus.READ, notification.getStatus());
    }

    @Test
    void testMarkAsRead_AlreadyRead() {
        // Given
        notification.setStatus(NotificationStatus.READ);
        when(notificationRepository.findByNotificationId("NOTIF-123"))
                .thenReturn(Optional.of(notification));

        // When
        String result = notificationService.markAsRead("NOTIF-123");

        // Then
        assertTrue(result.contains("already marked as read"));
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    // ========== GET STATUS ==========

    @Test
    void testGetNotificationStatus_Success() {
        // Given
        when(notificationRepository.findByNotificationId("NOTIF-123"))
                .thenReturn(Optional.of(notification));

        // When
        NotificationStatusDto result = notificationService.getNotificationStatus("NOTIF-123");

        // Then
        assertNotNull(result);
        verify(notificationRepository, times(1)).findByNotificationId("NOTIF-123");
    }

    // ========== GET HISTORY ==========

    @Test
    void testGetNotificationHistory_Success() {
        // Given
        List<Notification> notifications = Arrays.asList(notification);
        when(notificationRepository.findByRecipientId(123L))
                .thenReturn(notifications);

        // When
        List<NotificationDto> result = notificationService.getNotificationHistory(123L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(notificationRepository, times(1)).findByRecipientId(123L);
    }

    @Test
    void testGetNotificationHistory_Empty() {
        // Given
        when(notificationRepository.findByRecipientId(999L))
                .thenReturn(Collections.emptyList());

        // When
        List<NotificationDto> result = notificationService.getNotificationHistory(999L);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}