package com.codebugs.notificationservice.service;

import com.codebugs.notificationservice.event.OrderCreatedEvent;
import com.codebugs.notificationservice.event.PaymentProcessedEvent;
import com.codebugs.notificationservice.model.Notification;
import com.codebugs.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private final UUID orderId = UUID.randomUUID();

    @Test
    void handleOrderCreated_savesOrderCreatedNotification() {
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderCreatedEvent event = new OrderCreatedEvent(orderId, "customer-1", "prod-1", 2, new BigDecimal("99.99"));
        notificationService.handleOrderCreated(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getType()).isEqualTo("ORDER_CREATED");
        assertThat(saved.getRecipientId()).isEqualTo("customer-1");
        assertThat(saved.getOrderId()).isEqualTo(orderId);
        assertThat(saved.getMessage()).contains("placed");
    }

    @Test
    void handlePaymentProcessed_completed_savesSuccessNotification() {
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PaymentProcessedEvent event = new PaymentProcessedEvent(
            UUID.randomUUID(), orderId, "customer-1", "COMPLETED", new BigDecimal("99.99"), null);
        notificationService.handlePaymentProcessed(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getType()).isEqualTo("PAYMENT_PROCESSED");
        assertThat(saved.getMessage()).contains("successful");
    }

    @Test
    void handlePaymentProcessed_failed_savesFailureNotification() {
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PaymentProcessedEvent event = new PaymentProcessedEvent(
            UUID.randomUUID(), orderId, "customer-1", "FAILED", new BigDecimal("20000.00"), "Amount exceeds limit");
        notificationService.handlePaymentProcessed(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getMessage()).contains("failed");
        assertThat(saved.getMessage()).contains("Amount exceeds limit");
    }

    @Test
    void handleOrderCreated_messageContainsOrderDetails() {
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderCreatedEvent event = new OrderCreatedEvent(orderId, "customer-2", "prod-5", 3, new BigDecimal("150.00"));
        notificationService.handleOrderCreated(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        assertThat(captor.getValue().getMessage()).contains("3").contains("150.00");
    }
}
