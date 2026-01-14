package org.example.Notification.service;

import org.example.Notification.dto.request.NotificationCreateDto;
import org.example.Notification.dto.request.NotificationUpdateDto;
import org.example.Notification.dto.response.NotificationDto;
import org.example.Notification.dto.response.NotificationStatusDto;

import java.util.List;

public interface INotificationService {

    // Metodele existente
    NotificationDto createNotification(NotificationCreateDto notificationCreateDto);
    NotificationDto fetchNotification(String notificationId);
    NotificationDto updateNotificationSettings(NotificationUpdateDto notificationUpdateDto);
    boolean deleteExpiredNotifications(int retentionDays);
    String resendFailedNotification(String notificationId);
    NotificationDto scheduleNotification(NotificationCreateDto notificationCreateDto);
    String sendSmsNotification(String notificationId);
    String sendEmailNotification(String notificationId);
    String markAsRead(String notificationId);
    NotificationStatusDto getNotificationStatus(String notificationId);
    List<NotificationDto> getNotificationHistory(Long recipientId);

    // Metodele noi cu TransactionClient
    NotificationDto createNotificationForTransaction(String transactionId, NotificationCreateDto notificationCreateDto);
    String calculateFeesAndNotify(String transactionId, NotificationCreateDto notificationCreateDto);
    String checkFraudAndNotify(String transactionId, NotificationCreateDto notificationCreateDto);
    NotificationDto createNotificationForAccount(String accountNumber, NotificationCreateDto notificationCreateDto);
}