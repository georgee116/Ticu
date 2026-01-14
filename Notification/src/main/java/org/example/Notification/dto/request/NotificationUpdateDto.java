package org.example.Notification.dto.request;

import org.example.Notification.enums.NotificationPriority;

import java.time.LocalDateTime;

public class NotificationUpdateDto {

    private String notificationId;
    private String recipientEmail;
    private String recipientPhone;
    private String subject;
    private String message;
    private NotificationPriority priority;
    private LocalDateTime scheduledAt;

    public NotificationUpdateDto() {
    }

    public NotificationUpdateDto(String notificationId, String recipientEmail,
                                 String recipientPhone, String subject, String message) {
        this.notificationId = notificationId;
        this.recipientEmail = recipientEmail;
        this.recipientPhone = recipientPhone;
        this.subject = subject;
        this.message = message;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
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
}