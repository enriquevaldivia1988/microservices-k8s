package com.codebugs.paymentservice.listener;

import com.codebugs.paymentservice.event.OrderCreatedEvent;
import com.codebugs.paymentservice.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    private final PaymentService paymentService;

    public OrderEventListener(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = "order.created", groupId = "payment-group")
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Received order.created event for order: {}", event.orderId());
        paymentService.processPayment(event);
    }
}
