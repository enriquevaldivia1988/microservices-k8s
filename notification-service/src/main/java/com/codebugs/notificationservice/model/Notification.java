package com.codebugs.notificationservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.UUID;

@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;
    private String type;
    private String recipientId;
    private UUID orderId;
    private String message;
    private Instant createdAt;

    public Notification() {}

    public Notification(String type, String recipientId, UUID orderId, String message) {
        this.type = type;
        this.recipientId = recipientId;
        this.orderId = orderId;
        this.message = message;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public String getRecipientId() { return recipientId; }
    public UUID getOrderId() { return orderId; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }
}
