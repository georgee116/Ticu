package org.example.Notification.repository;

import org.example.Notification.entity.Notification;
import org.example.Notification.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByNotificationId(String notificationId);

    List<Notification> findByRecipientId(Long recipientId);

    List<Notification> findByCreatedAtBefore(LocalDateTime dateTime);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :dateTime")
    void deleteOldNotifications(LocalDateTime dateTime);
}