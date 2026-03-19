package com.codebugs.notificationservice.service;

import com.codebugs.notificationservice.event.OrderCreatedEvent;
import com.codebugs.notificationservice.event.PaymentProcessedEvent;
import com.codebugs.notificationservice.model.Notification;
import com.codebugs.notificationservice.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void handleOrderCreated(OrderCreatedEvent event) {
        String message = String.format(
            "Your order %s has been placed for %d item(s) totaling $%s.",
            event.orderId(), event.quantity(), event.amount());

        Notification notification = new Notification(
            "ORDER_CREATED", event.customerId(), event.orderId(), message);

        notificationRepository.save(notification);
        log.info("Saved ORDER_CREATED notification for customer: {}", event.customerId());
    }

    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        String message = "COMPLETED".equals(event.status())
            ? String.format("Payment for order %s was successful. Amount: $%s.", event.orderId(), event.amount())
            : String.format("Payment for order %s failed: %s.", event.orderId(), event.failureReason());

        Notification notification = new Notification(
            "PAYMENT_PROCESSED", event.customerId(), event.orderId(), message);

        notificationRepository.save(notification);
        log.info("Saved PAYMENT_PROCESSED notification for customer: {} — status: {}",
            event.customerId(), event.status());
    }
}
