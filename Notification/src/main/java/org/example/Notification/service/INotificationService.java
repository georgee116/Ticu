package org.example.Notification.service;

import org.example.Notification.dto.request.NotificationCreateDto;
import org.example.Notification.dto.request.NotificationUpdateDto;
import org.example.Notification.dto.response.NotificationDto;
import org.example.Notification.dto.response.NotificationStatusDto;

import java.util.List;

/**
 * Service interface for managing notifications in the banking system.
 *
 * <p>This interface defines operations for creating, sending, and managing notifications
 * across multiple channels (EMAIL, SMS, PUSH). It also provides integration with other
 * microservices like Transaction and Account Management through Feign clients.</p>
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Multi-channel notification delivery (Email, SMS, Push)</li>
 *   <li>Integration with Transaction and Account microservices</li>
 *   <li>Notification scheduling and retry logic</li>
 *   <li>Notification history and status tracking</li>
 *   <li>Anti-fraud and fee calculation notifications</li>
 * </ul>
 *
 * @author Stanga George
 * @version 1.0
 * @since 2025-01-15
 */
public interface INotificationService {

    /**
     * Creates a new notification with PENDING status.
     *
     * <p>This method initializes a notification from the provided DTO,
     * sets its initial status to PENDING, and persists it to the database.</p>
     *
     * @param notificationCreateDto the notification data containing recipient information,
     *                              message content, notification type, and priority
     * @return NotificationDto the created notification with generated ID and timestamp
     * @throws RuntimeException if the notification cannot be saved to the database
     * @author Stanga George
     * @since 1.0
     */
    NotificationDto createNotification(NotificationCreateDto notificationCreateDto);

    /**
     * Retrieves a notification by its unique identifier.
     *
     * @param notificationId the unique notification identifier (format: NOTIF-XXX)
     * @return NotificationDto the notification details including status and timestamps
     * @throws RuntimeException if no notification exists with the given ID
     * @author Stanga George
     * @since 1.0
     */
    NotificationDto fetchNotification(String notificationId);

    /**
     * Updates notification settings for an existing notification.
     *
     * <p>Allows modification of recipient information, message content,
     * and other mutable notification properties while preserving the
     * notification ID and audit timestamps.</p>
     *
     * @param notificationUpdateDto the update data containing notification ID
     *                              and fields to be updated
     * @return NotificationDto the updated notification
     * @throws RuntimeException if notification with the given ID is not found
     * @author Stanga George
     * @since 1.0
     */
    NotificationDto updateNotificationSettings(NotificationUpdateDto notificationUpdateDto);

    /**
     * Deletes notifications older than the specified retention period.
     *
     * <p>This method performs cleanup operations by removing notifications
     * that were created before the cutoff date (current date minus retention days).
     * Typically used as a scheduled maintenance task.</p>
     *
     * @param retentionDays the number of days to retain notifications (e.g., 30, 60, 90)
     * @return boolean true if at least one notification was deleted, false if none were found
     * @author Stanga George
     * @since 1.0
     */
    boolean deleteExpiredNotifications(int retentionDays);

    /**
     * Attempts to resend a notification that previously failed.
     *
     * <p>This method implements retry logic for failed notifications. It validates
     * that the notification is in FAILED status and hasn't exceeded the maximum
     * retry attempts before resetting it to PENDING status.</p>
     *
     * @param notificationId the ID of the failed notification to resend
     * @return String success message including the current retry count
     * @throws RuntimeException if notification is not found, not in FAILED status,
     *                          or maximum retry attempts (3) have been reached
     * @author Stanga George
     * @since 1.0
     */
    String resendFailedNotification(String notificationId);

    /**
     * Schedules a notification for future delivery.
     *
     * <p>The notification will be created with SCHEDULED status and will be
     * processed by a background job at the specified scheduled time.</p>
     *
     * @param notificationCreateDto notification data including the scheduledAt field
     * @return NotificationDto the scheduled notification
     * @throws RuntimeException if scheduledAt is null or is in the past
     * @author Stanga George
     * @since 1.0
     */
    NotificationDto scheduleNotification(NotificationCreateDto notificationCreateDto);

    /**
     * Sends an SMS notification to the recipient's phone number.
     *
     * <p>Validates that the notification type is SMS and that a valid phone number
     * is present before attempting to send. Updates the notification status
     * to SENT on success or FAILED on error.</p>
     *
     * @param notificationId the ID of the notification to send via SMS
     * @return String success message containing the recipient's phone number
     * @throws RuntimeException if notification is not found, is not of type SMS,
     *                          phone number is missing, or SMS service fails
     * @author Stanga George
     * @since 1.0
     */
    String sendSmsNotification(String notificationId);

    /**
     * Sends an email notification to the recipient's email address.
     *
     * <p>Validates that the notification type is EMAIL and that a valid email address
     * is present. On successful sending, updates status to SENT with timestamp.
     * On failure, updates status to FAILED with error details.</p>
     *
     * @param notificationId the ID of the notification to send via email
     * @return String success message containing the recipient's email address
     * @throws RuntimeException if notification is not found, is not of type EMAIL,
     *                          email address is missing, or email service fails
     * @author Stanga George
     * @since 1.0
     */
    String sendEmailNotification(String notificationId);

    /**
     * Marks a notification as read by the recipient.
     *
     * <p>This method is typically called when a user views or acknowledges
     * the notification in their notification center.</p>
     *
     * @param notificationId the ID of the notification to mark as read
     * @return String success message or info message if already marked as read
     * @throws RuntimeException if notification is not found
     * @author Stanga George
     * @since 1.0
     */
    String markAsRead(String notificationId);

    /**
     * Retrieves the current status of a notification.
     *
     * <p>Returns lightweight status information including notification ID,
     * current status, and relevant timestamps.</p>
     *
     * @param notificationId the ID of the notification to check
     * @return NotificationStatusDto containing notification ID and current status
     * @throws RuntimeException if notification is not found
     * @author Stanga George
     * @since 1.0
     */
    NotificationStatusDto getNotificationStatus(String notificationId);

    /**
     * Retrieves the complete notification history for a specific recipient.
     *
     * <p>Returns all notifications sent to the given recipient, ordered by
     * creation date (newest first). Useful for displaying notification history
     * in user interfaces.</p>
     *
     * @param recipientId the unique identifier of the recipient/user
     * @return List&lt;NotificationDto&gt; all notifications for the recipient,
     *         empty list if no notifications exist
     * @author Stanga George
     * @since 1.0
     */
    List<NotificationDto> getNotificationHistory(Long recipientId);

    /**
     * Creates a notification after verifying a transaction exists.
     *
     * <p>This method integrates with the Transaction microservice via Feign Client.
     * It verifies that the transaction exists before creating the notification,
     * ensuring data consistency across services. The notification message is
     * enhanced with transaction context.</p>
     *
     * <p><strong>Microservice Integration:</strong> Uses TransactionClient to call
     * the Transaction service's GET /api/transactions/{id} endpoint.</p>
     *
     * @param transactionId the unique identifier of the transaction
     * @param notificationCreateDto the notification data to be created
     * @return NotificationDto the created notification with transaction context
     * @throws RuntimeException if transaction is not found (404) or verification fails
     * @throws feign.FeignException if communication with Transaction service fails
     *                              (network error, service unavailable, etc.)
     * @author Stanga George
     * @since 1.0
     */
    NotificationDto createNotificationForTransaction(String transactionId,
                                                     NotificationCreateDto notificationCreateDto);

    /**
     * Calculates transaction fees and sends a notification to the recipient.
     *
     * <p>This method performs a two-step operation:</p>
     * <ol>
     *   <li>Calls the Transaction service to calculate fees for the given transaction</li>
     *   <li>Creates and sends an email notification with the fee calculation results</li>
     * </ol>
     *
     * <p><strong>Microservice Integration:</strong> Uses TransactionClient to call
     * the Transaction service's POST /api/transactions/{id}/calculate-fees endpoint.</p>
     *
     * @param transactionId the unique identifier of the transaction
     * @param notificationCreateDto the notification recipient and delivery preferences
     * @return String success message including the calculated fees information
     * @throws RuntimeException if fee calculation fails or Transaction service returns error
     * @throws feign.FeignException if communication with Transaction service fails
     * @author Stanga George
     * @since 1.0
     */
    String calculateFeesAndNotify(String transactionId,
                                  NotificationCreateDto notificationCreateDto);

    /**
     * Performs anti-fraud verification and notifies the recipient of results.
     *
     * <p>This method implements a security workflow:</p>
     * <ol>
     *   <li>Requests anti-fraud analysis from the Transaction service</li>
     *   <li>Receives fraud risk score/assessment</li>
     *   <li>Creates a security alert notification with the fraud check results</li>
     *   <li>Sends the notification via email to alert the account holder</li>
     * </ol>
     *
     * <p><strong>Microservice Integration:</strong> Uses TransactionClient to call
     * the Transaction service's POST /api/transactions/{id}/anti-fraud-check endpoint.</p>
     *
     * <p><strong>Security Note:</strong> This notification is critical for fraud prevention
     * and should be sent with high priority.</p>
     *
     * @param transactionId the unique identifier of the transaction to verify
     * @param notificationCreateDto the notification recipient details
     * @return String success message including the fraud check result/score
     * @throws RuntimeException if fraud check fails or returns suspicious activity
     * @throws feign.FeignException if communication with Transaction service fails
     * @author Stanga George
     * @since 1.0
     */
    String checkFraudAndNotify(String transactionId,
                               NotificationCreateDto notificationCreateDto);

    /**
     * Creates a notification after verifying an account exists.
     *
     * <p>This method integrates with the Account Management microservice via Feign Client.
     * It validates that the account exists and is accessible before creating the
     * notification, ensuring referential integrity across the banking system.</p>
     *
     * <p><strong>Microservice Integration:</strong> Uses AccountClient to call
     * the Account Management service's GET /api/accounts/{accountNumber} endpoint.</p>
     *
     * <p>Common use cases:</p>
     * <ul>
     *   <li>Account creation confirmation</li>
     *   <li>Account status change notifications</li>
     *   <li>Account security alerts</li>
     *   <li>Account statement availability</li>
     * </ul>
     *
     * @param accountNumber the unique account number (e.g., ACC-123456)
     * @param notificationCreateDto the notification data including message and delivery channel
     * @return NotificationDto the created notification with account context
     * @throws RuntimeException if account is not found (404), verification fails,
     *                          or account is in invalid state
     * @throws feign.FeignException if communication with Account Management service fails
     *                              (network error, service unavailable, timeout, etc.)
     * @author Stanga George
     * @since 1.0
     */
    NotificationDto createNotificationForAccount(String accountNumber,
                                                 NotificationCreateDto notificationCreateDto);
}