package org.example.Notification.service;

import org.example.Notification.client.TransactionClient;
import org.example.Notification.dto.request.NotificationCreateDto;
import org.example.Notification.dto.request.NotificationUpdateDto;
import org.example.Notification.dto.response.NotificationDto;
import org.example.Notification.dto.response.NotificationStatusDto;
import org.example.Notification.entity.Notification;
import org.example.Notification.enums.NotificationStatus;
import org.example.Notification.mapper.NotificationMapper;
import org.example.Notification.repository.NotificationRepository;
import feign.FeignException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.example.Notification.client.AccountClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the Notification Service for a banking microservices system.
 *
 * <p>This service handles all notification operations including creation, delivery,
 * and management across multiple channels (EMAIL, SMS, PUSH). It integrates with
 * other microservices using Feign clients for cross-service communication.</p>
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 *   <li>Multi-channel notification delivery (Email, SMS, Push)</li>
 *   <li>Integration with Transaction microservice for transaction-related notifications</li>
 *   <li>Integration with Account Management microservice for account notifications</li>
 *   <li>Notification scheduling with future delivery support</li>
 *   <li>Retry logic for failed notifications (max 3 attempts)</li>
 *   <li>Notification history tracking and status management</li>
 *   <li>Anti-fraud alert notifications</li>
 *   <li>Transaction fee calculation notifications</li>
 * </ul>
 *
 * <p><strong>Database Operations:</strong></p>
 * <ul>
 *   <li>All create/update/delete operations are transactional</li>
 *   <li>Notifications are persisted with full audit trail (created_at, sent_at, etc.)</li>
 *   <li>Status transitions: PENDING → SENT/FAILED/READ</li>
 * </ul>
 *
 * @author Stanga George
 * @version 1.0
 * @since 2025-01-15
 * @see INotificationService
 * @see NotificationRepository
 * @see TransactionClient
 * @see AccountClient
 */
@Service
public class NotificationServiceImpl implements INotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TransactionClient transactionClient;

    @Autowired
    private AccountClient accountClient;

    /**
     * {@inheritDoc}
     *
     * <p><strong>Implementation Details:</strong></p>
     * <ul>
     *   <li>Converts DTO to entity using NotificationMapper</li>
     *   <li>Sets initial status to PENDING</li>
     *   <li>Generates unique notification ID</li>
     *   <li>Sets creation timestamp</li>
     *   <li>Persists to database</li>
     *   <li>Returns DTO representation</li>
     * </ul>
     */
    @Override
    @Transactional
    public NotificationDto createNotification(NotificationCreateDto notificationCreateDto) {
        Notification notification = NotificationMapper.toEntity(notificationCreateDto);
        notification.setStatus(NotificationStatus.PENDING);
        Notification savedNotification = notificationRepository.save(notification);
        return NotificationMapper.toDto(savedNotification);
    }

    /**
     * {@inheritDoc}
     *
     * <p><strong>Implementation Details:</strong></p>
     * <ol>
     *   <li>Makes HTTP GET request to Transaction service via Feign Client</li>
     *   <li>Endpoint called: GET /api/transactions/{transactionId}</li>
     *   <li>Validates HTTP 2xx response (transaction exists)</li>
     *   <li>Creates notification with enhanced message including transaction ID</li>
     *   <li>Sets status to PENDING for async processing</li>
     *   <li>Persists notification to database</li>
     * </ol>
     *
     * <p><strong>Error Handling:</strong></p>
     * <ul>
     *   <li>Non-2xx response → throws RuntimeException with "Transaction not found"</li>
     *   <li>FeignException (network/service error) → throws RuntimeException with error details</li>
     * </ul>
     *
     * <p><strong>Example Usage:</strong></p>
     * <pre>
     * NotificationCreateDto dto = new NotificationCreateDto();
     * dto.setRecipientId(123L);
     * dto.setMessage("Transaction completed");
     * NotificationDto result = service.createNotificationForTransaction("TXN-001", dto);
     * </pre>
     */
    @Transactional
    public NotificationDto createNotificationForTransaction(String transactionId, NotificationCreateDto notificationCreateDto) {
        try {
            // 1. Verifică dacă tranzacția există apelând microserviciul Transactions
            ResponseEntity<Object> transactionResponse = transactionClient.getTransaction(transactionId);

            if (transactionResponse.getStatusCode().is2xxSuccessful()) {
                System.out.println("Transaction found: " + transactionResponse.getBody());

                // 2. Creează notificarea
                Notification notification = NotificationMapper.toEntity(notificationCreateDto);
                notification.setStatus(NotificationStatus.PENDING);
                notification.setMessage("Notification for transaction: " + transactionId + " - " + notification.getMessage());

                Notification savedNotification = notificationRepository.save(notification);
                return NotificationMapper.toDto(savedNotification);
            } else {
                throw new RuntimeException("Transaction not found: " + transactionId);
            }
        } catch (FeignException e) {
            throw new RuntimeException("Error communicating with Transaction service: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p><strong>Implementation Details:</strong></p>
     * <ol>
     *   <li>Makes HTTP GET request to Account Management service via Feign Client</li>
     *   <li>Endpoint called: GET /api/accounts/{accountNumber}</li>
     *   <li>Validates HTTP 2xx response (account exists and is accessible)</li>
     *   <li>Creates notification with account context in message</li>
     *   <li>Preserves original message while adding account number</li>
     *   <li>Sets PENDING status for async delivery</li>
     * </ol>
     *
     * <p><strong>Message Format:</strong></p>
     * <pre>
     * "Notification for account: {accountNumber} - {originalMessage}"
     * </pre>
     *
     * <p><strong>Error Handling:</strong></p>
     * <ul>
     *   <li>Non-2xx response → throws RuntimeException "Account verification failed"</li>
     *   <li>FeignException → throws RuntimeException with communication error details</li>
     * </ul>
     */
    @Override
    @Transactional
    public NotificationDto createNotificationForAccount(String accountNumber, NotificationCreateDto notificationCreateDto) {
        try {
            // Verifică dacă contul există în AccountManagement
            ResponseEntity<Object> accountResponse = accountClient.getAccount(accountNumber);

            if (accountResponse.getStatusCode().is2xxSuccessful()) {
                System.out.println("Account found: " + accountResponse.getBody());

                // Creează notificarea folosind mapper-ul existent
                Notification notification = NotificationMapper.toEntity(notificationCreateDto);
                notification.setStatus(NotificationStatus.PENDING);

                // Modifică mesajul pentru a include numărul contului
                String originalMessage = notification.getMessage();
                notification.setMessage("Notification for account: " + accountNumber + " - " + originalMessage);

                Notification savedNotification = notificationRepository.save(notification);
                return NotificationMapper.toDto(savedNotification);
            } else {
                throw new RuntimeException("Account verification failed");
            }
        } catch (FeignException e) {
            throw new RuntimeException("Error communicating with Account service: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p><strong>Implementation Details:</strong></p>
     * <ol>
     *   <li>Calls Transaction service to calculate fees via Feign Client</li>
     *   <li>Endpoint: POST /api/transactions/{transactionId}/calculate-fees</li>
     *   <li>Receives fee calculation result (e.g., "$5.00")</li>
     *   <li>Creates notification with fees information</li>
     *   <li>Sets predefined subject: "Transaction Fees Notification"</li>
     *   <li>Automatically sends notification via email</li>
     * </ol>
     *
     * <p><strong>Notification Content:</strong></p>
     * <ul>
     *   <li>Subject: "Transaction Fees Notification"</li>
     *   <li>Message: "Transaction fees: {feesCalculationResult}"</li>
     *   <li>Type: EMAIL (automatically sent)</li>
     * </ul>
     *
     * <p><strong>Return Value Format:</strong></p>
     * <pre>"Fees calculated and notification sent: $5.00"</pre>
     */
    @Transactional
    public String calculateFeesAndNotify(String transactionId, NotificationCreateDto notificationCreateDto) {
        try {
            // 1. Calculează fees din microserviciul Transactions
            ResponseEntity<String> feesResponse = transactionClient.calculateFees(transactionId);

            if (feesResponse.getStatusCode().is2xxSuccessful()) {
                String feesMessage = feesResponse.getBody();
                System.out.println("Calculated fees: " + feesMessage);

                // 2. Creează notificare cu informații despre fees
                Notification notification = NotificationMapper.toEntity(notificationCreateDto);
                notification.setStatus(NotificationStatus.PENDING);
                notification.setMessage("Transaction fees: " + feesMessage);
                notification.setSubject("Transaction Fees Notification");

                Notification savedNotification = notificationRepository.save(notification);

                // 3. Trimite notificarea
                sendEmailNotification(savedNotification.getNotificationId());

                return "Fees calculated and notification sent: " + feesMessage;
            } else {
                throw new RuntimeException("Failed to calculate fees for transaction: " + transactionId);
            }
        } catch (FeignException e) {
            throw new RuntimeException("Error communicating with Transaction service: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p><strong>Implementation Details:</strong></p>
     * <ol>
     *   <li>Requests anti-fraud analysis from Transaction service via Feign Client</li>
     *   <li>Endpoint: POST /api/transactions/{transactionId}/anti-fraud-check</li>
     *   <li>Receives fraud risk score/assessment (e.g., "Fraud score: 0.85")</li>
     *   <li>Creates security alert notification with fraud results</li>
     *   <li>Sets subject: "Security Alert - Transaction {transactionId}"</li>
     *   <li>Automatically sends email notification to account holder</li>
     * </ol>
     *
     * <p><strong>Security Notification Details:</strong></p>
     * <ul>
     *   <li>Subject: "Security Alert - Transaction {transactionId}"</li>
     *   <li>Message: "Anti-fraud check result: {fraudScore}"</li>
     *   <li>Priority: Should be set to HIGH in the DTO</li>
     *   <li>Type: EMAIL (for audit trail)</li>
     * </ul>
     *
     * <p><strong>Fraud Score Interpretation:</strong></p>
     * <ul>
     *   <li>0.0 - 0.3: Low risk</li>
     *   <li>0.3 - 0.7: Medium risk</li>
     *   <li>0.7 - 1.0: High risk (immediate attention required)</li>
     * </ul>
     *
     * <p><strong>Return Value Example:</strong></p>
     * <pre>"Fraud check completed and notification sent: Fraud score: 0.15"</pre>
     */
    @Transactional
    public String checkFraudAndNotify(String transactionId, NotificationCreateDto notificationCreateDto) {
        try {
            // 1. Verifică anti-fraud din microserviciul Transactions
            ResponseEntity<String> fraudCheckResponse = transactionClient.antiFraudCheck(transactionId);

            if (fraudCheckResponse.getStatusCode().is2xxSuccessful()) {
                String fraudScore = fraudCheckResponse.getBody();
                System.out.println("Anti-fraud check result: " + fraudScore);

                // 2. Creează notificare bazată pe scorul de fraud
                Notification notification = NotificationMapper.toEntity(notificationCreateDto);
                notification.setStatus(NotificationStatus.PENDING);
                notification.setMessage("Anti-fraud check result: " + fraudScore);
                notification.setSubject("Security Alert - Transaction " + transactionId);

                Notification savedNotification = notificationRepository.save(notification);

                // 3. Trimite notificarea
                sendEmailNotification(savedNotification.getNotificationId());

                return "Fraud check completed and notification sent: " + fraudScore);
            } else {
                throw new RuntimeException("Fraud check failed for transaction: " + transactionId);
            }
        } catch (FeignException e) {
            throw new RuntimeException("Error communicating with Transaction service: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p><strong>Implementation Details:</strong></p>
     * <ul>
     *   <li>Queries database by unique notification ID</li>
     *   <li>Converts entity to DTO using NotificationMapper</li>
     *   <li>Returns complete notification details</li>
     * </ul>
     */
    @Override
    public NotificationDto fetchNotification(String notificationId) {
        Notification notification = notificationRepository.findByNotificationId(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        return NotificationMapper.toDto(notification);
    }

    /**
     * {@inheritDoc}
     *
     * <p><strong>Updatable Fields:</strong></p>
     * <ul>
     *   <li>recipientEmail</li>
     *   <li>recipientPhone</li>
     *   <li>message</li>
     *   <li>subject</li>
     * </ul>
     *
     * <p><strong>Immutable Fields:</strong></p>
     * <ul>
     *   <li>notificationId</li>
     *   <li>status</li>
     *   <li>createdAt</li>
     *   <li>recipientId</li>
     * </ul>
     */
    @Override
    @Transactional
    public NotificationDto updateNotificationSettings(NotificationUpdateDto notificationUpdateDto) {
        Notification notification = notificationRepository.findByNotificationId(notificationUpdateDto.getNotificationId())
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationUpdateDto.getNotificationId()));
        NotificationMapper.updateEntityFromDto(notification, notificationUpdateDto);
        Notification updatedNotification = notificationRepository.save(notification);
        return NotificationMapper.toDto(updatedNotification);
    }

    /**
     * {@inheritDoc}
     *
     * <p><strong>Implementation Details:</strong></p>
     * <ol>
     *   <li>Calculates cutoff date: current date - retention days</li>
     *   <li>Queries for notifications created before cutoff date</li>
     *   <li>If none found, returns false (no deletion needed)</li>
     *   <li>If found, executes batch delete operation</li>
     *   <li>Returns true on successful deletion</li>
     * </ol>
     *
     * <p><strong>Typical Usage:</strong></p>
     * <pre>
     * // Delete notifications older than 30 days
     * boolean deleted = service.deleteExpiredNotifications(30);
     * </pre>
     */
    @Override
    @Transactional
    public boolean deleteExpiredNotifications(int retentionDays) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        List<Notification> oldNotifications = notificationRepository.findByCreatedAtBefore(cutoffDate);
        if (oldNotifications.isEmpty()) {
            return false;
        }
        notificationRepository.deleteOldNotifications(cutoffDate);
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * <p><strong>Retry Logic:</strong></p>
     * <ul>
     *   <li>Maximum retry attempts: 3</li>
     *   <li>Each resend increments retry counter</li>
     *   <li>Notification must be in FAILED status</li>
     *   <li>Status changes: FAILED → PENDING</li>
     *   <li>Clears previous failure reason and timestamp</li>
     * </ul>
     *
     * <p><strong>Example Return:</strong></p>
     * <pre>"Notification resent successfully. Retry count: 2"</pre>
     */
    @Override
    @Transactional
    public String resendFailedNotification(String notificationId) {
        Notification notification = notificationRepository.findByNotificationId(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));

        if (notification.getStatus() != NotificationStatus.FAILED) {
            throw new RuntimeException("Notification is not in FAILED status");
        }

        if (notification.getRetryCount() >= notification.getMaxRetries()) {
            throw new RuntimeException("Maximum retry attempts reached");
        }

        notification.setRetryCount(notification.getRetryCount() + 1);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setFailureReason(null);
        notification.setFailedAt(null);

        notificationRepository.save(notification);

        return "Notification resent successfully. Retry count: " + notification.getRetryCount();
    }

    /**
     * {@inheritDoc}
     *
     * <p><strong>Validation Rules:</strong></p>
     * <ul>
     *   <li>scheduledAt field must not be null</li>
     *   <li>scheduledAt must be in the future</li>
     *   <li>Minimum schedule time: current time + 1 minute (recommended)</li>
     * </ul>
     *
     * <p><strong>Status Transitions:</strong></p>
     * <ul>
     *   <li>On schedule: SCHEDULED</li>
     *   <li>On delivery time: SCHEDULED → PENDING → SENT</li>
     * </ul>
     */
    @Override
    @Transactional
    public NotificationDto scheduleNotification(NotificationCreateDto notificationCreateDto) {
        if (notificationCreateDto.getScheduledAt() == null) {
            throw new RuntimeException("Scheduled time must be provided");
        }

        if (notificationCreateDto.getScheduledAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Scheduled time must be in the future");
        }

        Notification notification = NotificationMapper.toEntity(notificationCreateDto);
        notification.setStatus(NotificationStatus.SCHEDULED);
        Notification savedNotification = notificationRepository.save(notification);
        return NotificationMapper.toDto(savedNotification);
    }

    /**
     * {@inheritDoc}
     *
     * <p><strong>Pre-send Validation:</strong></p>
     * <ol>
     *   <li>Checks notification exists</li>
     *   <li>Validates type is SMS</li>
     *   <li>Validates phone number is present and non-empty</li>
     * </ol>
     *
     * <p><strong>On Success:</strong></p>
     * <ul>
     *   <li>Status: PENDING → SENT</li>
     *   <li>Sets sentAt timestamp</li>
     *   <li>Sets deliveredAt timestamp</li>
     *   <li>Returns success message with phone number</li>
     * </ul>
     *
     * <p><strong>On Failure:</strong></p>
     * <ul>
     *   <li>Status: PENDING → FAILED</li>
     *   <li>Sets failedAt timestamp</li>
     *   <li>Records failure reason</li>
     *   <li>Throws RuntimeException with error details</li>
     * </ul>
     */
    @Override
    @Transactional
    public String sendSmsNotification(String notificationId) {
        Notification notification = notificationRepository.findByNotificationId(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));

        if (notification.getNotificationType() != org.example.Notification.enums.NotificationType.SMS) {
            throw new RuntimeException("Notification is not of type SMS");
        }

        if (notification.getRecipientPhone() == null || notification.getRecipientPhone().isEmpty()) {
            throw new RuntimeException("Recipient phone number is missing");
        }

        try {
            System.out.println("Sending SMS to: " + notification.getRecipientPhone());
            System.out.println("Message: " + notification.getMessage());

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notification.setDeliveredAt(LocalDateTime.now());

            notificationRepository.save(notification);

            return "SMS sent successfully to " + notification.getRecipientPhone();

        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailedAt(LocalDateTime.now());
            notification.setFailureReason("SMS sending failed: " + e.getMessage());
            notificationRepository.save(notification);

            throw new RuntimeException("Failed to send SMS: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p><strong>Pre-send Validation:</strong></p>
     * <ol>
     *   <li>Checks notification exists</li>
     *   <li>Validates type is EMAIL</li>
     *   <li>Validates email address is present and non-empty</li>
     * </ol>
     *
     * <p><strong>Email Content:</strong></p>
     * <ul>
     *   <li>To: notification.recipientEmail</li>
     *   <li>Subject: notification.subject</li>
     *   <li>Body: notification.message</li>
     * </ul>
     *
     * <p><strong>Status Updates:</strong></p>
     * <ul>
     *   <li>Success: PENDING → SENT (with timestamps)</li>
     *   <li>Failure: PENDING → FAILED (with error details)</li>
     * </ul>
     */
    @Override
    @Transactional
    public String sendEmailNotification(String notificationId) {
        Notification notification = notificationRepository.findByNotificationId(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));

        if (notification.getNotificationType() != org.example.Notification.enums.NotificationType.EMAIL) {
            throw new RuntimeException("Notification is not of type EMAIL");
        }

        if (notification.getRecipientEmail() == null || notification.getRecipientEmail().isEmpty()) {
            throw new RuntimeException("Recipient email is missing");
        }

        try {
            System.out.println("Sending Email to: " + notification.getRecipientEmail());
            System.out.println("Subject: " + notification.getSubject());
            System.out.println("Message: " + notification.getMessage());

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notification.setDeliveredAt(LocalDateTime.now());

            notificationRepository.save(notification);

            return "Email sent successfully to " + notification.getRecipientEmail();

        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailedAt(LocalDateTime.now());
            notification.setFailureReason("Email sending failed: " + e.getMessage());
            notificationRepository.save(notification);

            throw new RuntimeException("Failed to send Email: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p><strong>Behavior:</strong></p>
     * <ul>
     *   <li>If already READ: Returns info message, no database update</li>
     *   <li>If not READ: Updates status to READ and saves</li>
     *   <li>Idempotent operation (safe to call multiple times)</li>
     * </ul>
     */
    @Override
    @Transactional
    public String markAsRead(String notificationId) {
        Notification notification = notificationRepository.findByNotificationId(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));

        if (notification.getStatus() == NotificationStatus.READ) {
            return "Notification already marked as read";
        }

        notification.setStatus(NotificationStatus.READ);
        notificationRepository.save(notification);

        return "Notification marked as read successfully";
    }

    /**
     * {@inheritDoc}
     *
     * <p><strong>Returned Information:</strong></p>
     * <ul>
     *   <li>notificationId</li>
     *   <li>current status (PENDING/SENT/FAILED/READ/SCHEDULED)</li>
     *   <li>relevant timestamps</li>
     * </ul>
     */
    @Override
    public NotificationStatusDto getNotificationStatus(String notificationId) {
        Notification notification = notificationRepository.findByNotificationId(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        return NotificationMapper.toStatusDto(notification);
    }

    /**
     * {@inheritDoc}
     *
     * <p><strong>Query Details:</strong></p>
     * <ul>
     *   <li>Searches by recipient ID</li>
     *   <li>Returns all statuses (PENDING, SENT, FAILED, READ)</li>
     *   <li>Ordered by creation date (newest first)</li>
     *   <li>Includes all notification types (EMAIL, SMS, PUSH)</li>
     * </ul>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Displaying notification center in UI</li>
     *   <li>Generating notification reports</li>
     *   <li>Auditing notification delivery</li>
     * </ul>
     */
    @Override
    public List<NotificationDto> getNotificationHistory(Long recipientId) {
        List<Notification> notifications = notificationRepository.findByRecipientId(recipientId);
        return notifications.stream()
                .map(NotificationMapper::toDto)
                .collect(Collectors.toList());
    }
}