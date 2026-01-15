package org.example.Notification.dto.request;

import org.example.Notification.enums.NotificationType;
import org.example.Notification.enums.NotificationPriority;
import org.example.Notification.enums.TriggerEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class NotificationCreateDtoTest {

    private Validator validator;
    private NotificationCreateDto notificationCreateDto;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        notificationCreateDto = new NotificationCreateDto();
        notificationCreateDto.setRecipientId(123L);
        notificationCreateDto.setRecipientEmail("test@example.com");
        notificationCreateDto.setRecipientPhone("+40721234567");
        notificationCreateDto.setNotificationType(NotificationType.EMAIL);
        notificationCreateDto.setTriggerEvent(TriggerEvent.ACCOUNT_CREATED.name());
        notificationCreateDto.setSubject("Test Subject");
        notificationCreateDto.setMessage("Test Message");
        notificationCreateDto.setPriority(NotificationPriority.HIGH);
    }

    @Test
    void testValidNotificationCreateDto() {
        // When
        Set<ConstraintViolation<NotificationCreateDto>> violations = validator.validate(notificationCreateDto);

        // Then
        assertTrue(violations.isEmpty(), "Valid DTO should have no violations");
    }

    @Test
    void testGettersAndSetters() {
        // Given
        NotificationCreateDto dto = new NotificationCreateDto();

        // When
        dto.setRecipientId(456L);
        dto.setRecipientEmail("user@test.com");
        dto.setRecipientPhone("+40723456789");
        dto.setNotificationType(NotificationType.SMS);
        dto.setTriggerEvent(TriggerEvent.TRANSACTION_COMPLETED.name());
        dto.setSubject("New Subject");
        dto.setMessage("New Message");
        dto.setPriority(NotificationPriority.MEDIUM);

        // Then
        assertEquals(456L, dto.getRecipientId());
        assertEquals("user@test.com", dto.getRecipientEmail());
        assertEquals("+40723456789", dto.getRecipientPhone());
        assertEquals(NotificationType.SMS, dto.getNotificationType());
        assertEquals(TriggerEvent.TRANSACTION_COMPLETED.name(), dto.getTriggerEvent());
        assertEquals("New Subject", dto.getSubject());
        assertEquals("New Message", dto.getMessage());
        assertEquals(NotificationPriority.MEDIUM, dto.getPriority());
    }








    @Test
    void testWithDifferentNotificationTypes() {
        // Test EMAIL
        notificationCreateDto.setNotificationType(NotificationType.EMAIL);
        assertEquals(NotificationType.EMAIL, notificationCreateDto.getNotificationType());

        // Test SMS
        notificationCreateDto.setNotificationType(NotificationType.SMS);
        assertEquals(NotificationType.SMS, notificationCreateDto.getNotificationType());

        // Test PUSH
        notificationCreateDto.setNotificationType(NotificationType.PUSH);
        assertEquals(NotificationType.PUSH, notificationCreateDto.getNotificationType());
    }

    @Test
    void testWithDifferentPriorities() {
        // Test HIGH
        notificationCreateDto.setPriority(NotificationPriority.HIGH);
        assertEquals(NotificationPriority.HIGH, notificationCreateDto.getPriority());

        // Test MEDIUM
        notificationCreateDto.setPriority(NotificationPriority.MEDIUM);
        assertEquals(NotificationPriority.MEDIUM, notificationCreateDto.getPriority());

        // Test LOW
        notificationCreateDto.setPriority(NotificationPriority.LOW);
        assertEquals(NotificationPriority.LOW, notificationCreateDto.getPriority());
    }

    @Test
    void testWithDifferentTriggerEvents() {
        // Test ACCOUNT_CREATED
        notificationCreateDto.setTriggerEvent(TriggerEvent.ACCOUNT_CREATED.name());
        assertEquals(TriggerEvent.ACCOUNT_CREATED.name(), notificationCreateDto.getTriggerEvent());

        // Test TRANSACTION_COMPLETED
        notificationCreateDto.setTriggerEvent(TriggerEvent.TRANSACTION_COMPLETED.name());
        assertEquals(TriggerEvent.TRANSACTION_COMPLETED.name(), notificationCreateDto.getTriggerEvent());

        // Test PAYMENT_FAILED
        notificationCreateDto.setTriggerEvent(TriggerEvent.PAYMENT_FAILED.name());
        assertEquals(TriggerEvent.PAYMENT_FAILED.name(), notificationCreateDto.getTriggerEvent());
    }

    @Test
    void testRecipientEmailCanBeNull() {
        // Given
        notificationCreateDto.setRecipientEmail(null);

        // When
        Set<ConstraintViolation<NotificationCreateDto>> violations = validator.validate(notificationCreateDto);

        // Then - Email poate fi null (optional pentru SMS/PUSH)
        assertTrue(violations.isEmpty() || violations.stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("recipientEmail")));
    }

    @Test
    void testRecipientPhoneCanBeNull() {
        // Given
        notificationCreateDto.setRecipientPhone(null);

        // When
        Set<ConstraintViolation<NotificationCreateDto>> violations = validator.validate(notificationCreateDto);

        // Then - Phone poate fi null (optional pentru EMAIL/PUSH)
        assertTrue(violations.isEmpty() || violations.stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("recipientPhone")));
    }

    @Test
    void testSubjectCanBeNull() {
        // Given
        notificationCreateDto.setSubject(null);

        // When
        Set<ConstraintViolation<NotificationCreateDto>> violations = validator.validate(notificationCreateDto);

        // Then - Subject poate fi null (optional pentru SMS)
        assertTrue(violations.isEmpty() || violations.stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("subject")));
    }



    @Test
    void testNotEquals() {
        // Given
        NotificationCreateDto dto1 = new NotificationCreateDto();
        dto1.setRecipientId(123L);

        NotificationCreateDto dto2 = new NotificationCreateDto();
        dto2.setRecipientId(456L);

        // Then
        assertNotEquals(dto1, dto2);
    }
}