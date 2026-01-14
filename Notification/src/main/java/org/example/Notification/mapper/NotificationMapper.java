package org.example.Notification.mapper;

import org.example.Notification.dto.request.NotificationCreateDto;
import org.example.Notification.dto.request.NotificationUpdateDto;
import org.example.Notification.dto.response.NotificationDto;
import org.example.Notification.dto.response.NotificationStatusDto;
import org.example.Notification.entity.Notification;

import java.util.UUID;

public class NotificationMapper {

    public static Notification toEntity(NotificationCreateDto dto) {
        Notification notification = new Notification(
                generateNotificationId(),
                dto.getRecipientId(),
                dto.getRecipientEmail(),
                dto.getRecipientPhone(),
                dto.getNotificationType(),
                dto.getTriggerEvent(),
                dto.getSubject(),
                dto.getMessage(),
                dto.getPriority() != null ? dto.getPriority() : org.example.Notification.enums.NotificationPriority.MEDIUM
        );

        notification.setTemplateId(dto.getTemplateId());
        notification.setScheduledAt(dto.getScheduledAt());
        notification.setRelatedAccountId(dto.getRelatedAccountId());
        notification.setRelatedTransactionId(dto.getRelatedTransactionId());

        return notification;
    }

    public static NotificationDto toDto(Notification notification) {
        NotificationDto dto = new NotificationDto();

        dto.setId(notification.getId());
        dto.setNotificationId(notification.getNotificationId());
        dto.setRecipientId(notification.getRecipientId());
        dto.setRecipientEmail(notification.getRecipientEmail());
        dto.setRecipientPhone(notification.getRecipientPhone());
        dto.setNotificationType(notification.getNotificationType());
        dto.setTriggerEvent(notification.getTriggerEvent());
        dto.setSubject(notification.getSubject());
        dto.setMessage(notification.getMessage());
        dto.setTemplateId(notification.getTemplateId());
        dto.setStatus(notification.getStatus());
        dto.setPriority(notification.getPriority());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setScheduledAt(notification.getScheduledAt());
        dto.setSentAt(notification.getSentAt());
        dto.setDeliveredAt(notification.getDeliveredAt());
        dto.setFailedAt(notification.getFailedAt());
        dto.setFailureReason(notification.getFailureReason());
        dto.setRetryCount(notification.getRetryCount());
        dto.setMaxRetries(notification.getMaxRetries());
        dto.setRelatedAccountId(notification.getRelatedAccountId());
        dto.setRelatedTransactionId(notification.getRelatedTransactionId());

        return dto;
    }

    public static NotificationStatusDto toStatusDto(Notification notification) {
        NotificationStatusDto dto = new NotificationStatusDto();

        dto.setNotificationId(notification.getNotificationId());
        dto.setStatus(notification.getStatus());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setScheduledAt(notification.getScheduledAt());
        dto.setSentAt(notification.getSentAt());
        dto.setDeliveredAt(notification.getDeliveredAt());
        dto.setFailedAt(notification.getFailedAt());
        dto.setFailureReason(notification.getFailureReason());
        dto.setRetryCount(notification.getRetryCount());

        return dto;
    }

    public static void updateEntityFromDto(Notification notification, NotificationUpdateDto dto) {
        if (dto.getRecipientEmail() != null) {
            notification.setRecipientEmail(dto.getRecipientEmail());
        }
        if (dto.getRecipientPhone() != null) {
            notification.setRecipientPhone(dto.getRecipientPhone());
        }
        if (dto.getSubject() != null) {
            notification.setSubject(dto.getSubject());
        }
        if (dto.getMessage() != null) {
            notification.setMessage(dto.getMessage());
        }
        if (dto.getPriority() != null) {
            notification.setPriority(dto.getPriority());
        }
        if (dto.getScheduledAt() != null) {
            notification.setScheduledAt(dto.getScheduledAt());
        }
    }

    private static String generateNotificationId() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "NOTIF-" + timestamp.substring(timestamp.length() - 10) + "-" + random;
    }
}
