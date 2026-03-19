package com.codebugs.paymentservice.service;

import com.codebugs.paymentservice.dto.PaymentResponse;
import com.codebugs.paymentservice.event.OrderCreatedEvent;
import com.codebugs.paymentservice.event.PaymentProcessedEvent;
import com.codebugs.paymentservice.exception.PaymentNotFoundException;
import com.codebugs.paymentservice.model.Payment;
import com.codebugs.paymentservice.model.PaymentStatus;
import com.codebugs.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private KafkaTemplate<String, PaymentProcessedEvent> kafkaTemplate;

    @InjectMocks
    private PaymentService paymentService;

    private final UUID orderId = UUID.randomUUID();
    private final UUID paymentId = UUID.randomUUID();

    private Payment completedPayment;
    private Payment failedPayment;

    @BeforeEach
    void setUp() {
        completedPayment = buildPayment(paymentId, orderId, new BigDecimal("100.00"), PaymentStatus.COMPLETED, null);
        failedPayment = buildPayment(paymentId, orderId, new BigDecimal("20000.00"), PaymentStatus.FAILED, "Amount exceeds limit");
    }

    @Test
    void processPayment_withinLimit_completesPayment() {
        when(paymentRepository.save(any())).thenReturn(completedPayment);

        OrderCreatedEvent event = new OrderCreatedEvent(orderId, "customer-1", "prod-1", 1, new BigDecimal("100.00"));
        PaymentResponse response = paymentService.processPayment(event);

        assertThat(response.status()).isEqualTo(PaymentStatus.COMPLETED);
        verify(kafkaTemplate).send(eq("payment.processed"), any(), any(PaymentProcessedEvent.class));
    }

    @Test
    void processPayment_exceedsLimit_failsPayment() {
        when(paymentRepository.save(any())).thenReturn(failedPayment);

        OrderCreatedEvent event = new OrderCreatedEvent(orderId, "customer-1", "prod-1", 1, new BigDecimal("20000.00"));
        PaymentResponse response = paymentService.processPayment(event);

        assertThat(response.status()).isEqualTo(PaymentStatus.FAILED);
        assertThat(response.failureReason()).isEqualTo("Amount exceeds limit");
        verify(kafkaTemplate).send(eq("payment.processed"), any(), any(PaymentProcessedEvent.class));
    }

    @Test
    void getPayment_returnsPayment() {
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(completedPayment));

        PaymentResponse response = paymentService.getPayment(paymentId);

        assertThat(response.id()).isEqualTo(paymentId);
        assertThat(response.orderId()).isEqualTo(orderId);
    }

    @Test
    void getPayment_notFound_throws() {
        when(paymentRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPayment(UUID.randomUUID()))
            .isInstanceOf(PaymentNotFoundException.class);
    }

    @Test
    void getPaymentByOrder_returnsPayment() {
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(completedPayment));

        PaymentResponse response = paymentService.getPaymentByOrder(orderId);

        assertThat(response.orderId()).isEqualTo(orderId);
    }

    @Test
    void getPaymentByOrder_notFound_throws() {
        when(paymentRepository.findByOrderId(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPaymentByOrder(UUID.randomUUID()))
            .isInstanceOf(PaymentNotFoundException.class);
    }

    private Payment buildPayment(UUID id, UUID orderId, BigDecimal amount, PaymentStatus status, String reason) {
        Payment p = new Payment();
        p.setOrderId(orderId);
        p.setCustomerId("customer-1");
        p.setAmount(amount);
        p.setStatus(status);
        p.setFailureReason(reason);
        try {
            var f = p.getClass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(p, id);
        } catch (Exception e) { throw new RuntimeException(e); }
        return p;
    }
}
