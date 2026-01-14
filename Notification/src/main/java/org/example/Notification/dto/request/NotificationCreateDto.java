package org.example.Notification.dto.request;

import org.example.Notification.enums.NotificationPriority;
import org.example.Notification.enums.NotificationType;

import java.time.LocalDateTime;

public class NotificationCreateDto {

    private Long recipientId;
    private String recipientEmail;
    private String recipientPhone;
    private NotificationType notificationType;
    private String triggerEvent;
    private String subject;
    private String message;
    private String templateId;
    private NotificationPriority priority;
    private LocalDateTime scheduledAt;
    private Long relatedAccountId;
    private Long relatedTransactionId;


    public NotificationCreateDto() {
    }

    public NotificationCreateDto(Long recipientId, String recipientEmail, String recipientPhone,
                                 NotificationType notificationType, String triggerEvent,
                                 String subject, String message, NotificationPriority priority) {
        this.recipientId = recipientId;
        this.recipientEmail = recipientEmail;
        this.recipientPhone = recipientPhone;
        this.notificationType = notificationType;
        this.triggerEvent = triggerEvent;
        this.subject = subject;
        this.message = message;
        this.priority = priority;
    }


    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public String getTriggerEvent() {
        return triggerEvent;
    }

    public void setTriggerEvent(String triggerEvent) {
        this.triggerEvent = triggerEvent;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public NotificationPriority getPriority() {
        return priority;
    }

    public void setPriority(NotificationPriority priority) {
        this.priority = priority;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public Long getRelatedAccountId() {
        return relatedAccountId;
    }

    public void setRelatedAccountId(Long relatedAccountId) {
        this.relatedAccountId = relatedAccountId;
    }

    public Long getRelatedTransactionId() {
        return relatedTransactionId;
    }

    public void setRelatedTransactionId(Long relatedTransactionId) {
        this.relatedTransactionId = relatedTransactionId;
    }
}