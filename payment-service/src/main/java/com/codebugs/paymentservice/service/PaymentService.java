package com.codebugs.paymentservice.service;

import com.codebugs.paymentservice.dto.PaymentResponse;
import com.codebugs.paymentservice.event.OrderCreatedEvent;
import com.codebugs.paymentservice.event.PaymentProcessedEvent;
import com.codebugs.paymentservice.exception.PaymentNotFoundException;
import com.codebugs.paymentservice.model.Payment;
import com.codebugs.paymentservice.model.PaymentStatus;
import com.codebugs.paymentservice.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final String TOPIC = "payment.processed";

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, PaymentProcessedEvent> kafkaTemplate;

    public PaymentService(PaymentRepository paymentRepository,
                          KafkaTemplate<String, PaymentProcessedEvent> kafkaTemplate) {
        this.paymentRepository = paymentRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public PaymentResponse processPayment(OrderCreatedEvent event) {
        Payment payment = new Payment();
        payment.setOrderId(event.orderId());
        payment.setCustomerId(event.customerId());
        payment.setAmount(event.amount());

        // Simulate payment processing: amounts > 10000 are declined
        if (event.amount().intValue() > 10000) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Amount exceeds limit");
        } else {
            payment.setStatus(PaymentStatus.COMPLETED);
        }

        Payment saved = paymentRepository.save(payment);
        log.info("Payment {} processed for order {}: {}", saved.getId(), event.orderId(), saved.getStatus());

        PaymentProcessedEvent processedEvent = new PaymentProcessedEvent(
            saved.getId(), saved.getOrderId(), saved.getCustomerId(),
            saved.getStatus(), saved.getAmount(), saved.getFailureReason());

        kafkaTemplate.send(TOPIC, saved.getOrderId().toString(), processedEvent);
        return PaymentResponse.from(saved);
    }

    public PaymentResponse getPayment(UUID id) {
        return paymentRepository.findById(id)
            .map(PaymentResponse::from)
            .orElseThrow(() -> new PaymentNotFoundException(id));
    }

    public PaymentResponse getPaymentByOrder(UUID orderId) {
        return paymentRepository.findByOrderId(orderId)
            .map(PaymentResponse::from)
            .orElseThrow(() -> new PaymentNotFoundException(orderId));
    }
}
