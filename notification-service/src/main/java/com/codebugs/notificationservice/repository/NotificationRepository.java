package com.codebugs.notificationservice.repository;

import com.codebugs.notificationservice.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByRecipientId(String recipientId);
    List<Notification> findByOrderId(UUID orderId);
}
