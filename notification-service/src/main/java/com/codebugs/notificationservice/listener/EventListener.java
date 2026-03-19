package com.codebugs.notificationservice.listener;

import com.codebugs.notificationservice.event.OrderCreatedEvent;
import com.codebugs.notificationservice.event.PaymentProcessedEvent;
import com.codebugs.notificationservice.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EventListener {

    private static final Logger log = LoggerFactory.getLogger(EventListener.class);

    private final NotificationService notificationService;

    public EventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "order.created", groupId = "notification-group")
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Received order.created event: {}", event.orderId());
        notificationService.handleOrderCreated(event);
    }

    @KafkaListener(topics = "payment.processed", groupId = "notification-group")
    public void onPaymentProcessed(PaymentProcessedEvent event) {
        log.info("Received payment.processed event: orderId={}, status={}", event.orderId(), event.status());
        notificationService.handlePaymentProcessed(event);
    }
}
