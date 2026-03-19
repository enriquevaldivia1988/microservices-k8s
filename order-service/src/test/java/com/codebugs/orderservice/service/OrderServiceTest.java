package com.codebugs.orderservice.service;

import com.codebugs.orderservice.dto.CreateOrderRequest;
import com.codebugs.orderservice.dto.OrderResponse;
import com.codebugs.orderservice.event.OrderCreatedEvent;
import com.codebugs.orderservice.exception.OrderNotFoundException;
import com.codebugs.orderservice.model.Order;
import com.codebugs.orderservice.model.OrderStatus;
import com.codebugs.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    @InjectMocks
    private OrderService orderService;

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        setField(order, "id", UUID.randomUUID());
        order.setCustomerId("customer-1");
        order.setProductId("product-1");
        order.setQuantity(2);
        order.setAmount(new BigDecimal("99.99"));
        order.setStatus(OrderStatus.PENDING);
    }

    @Test
    void createOrder_savesAndPublishesEvent() {
        when(orderRepository.save(any())).thenReturn(order);

        CreateOrderRequest request = new CreateOrderRequest(
            "customer-1", "product-1", 2, new BigDecimal("99.99"));

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.customerId()).isEqualTo("customer-1");
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
        verify(kafkaTemplate).send(eq("order.created"), any(), any(OrderCreatedEvent.class));
    }

    @Test
    void getOrder_returnsOrder() {
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrder(order.getId());

        assertThat(response.id()).isEqualTo(order.getId());
        assertThat(response.productId()).isEqualTo("product-1");
    }

    @Test
    void getOrder_throwsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrder(id))
            .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void getOrdersByCustomer_returnsCustomerOrders() {
        when(orderRepository.findByCustomerId("customer-1")).thenReturn(List.of(order));

        List<OrderResponse> responses = orderService.getOrdersByCustomer("customer-1");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).customerId()).isEqualTo("customer-1");
    }

    @Test
    void cancelOrder_pendingOrder_cancels() {
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.cancelOrder(order.getId());

        assertThat(response.status()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void cancelOrder_nonPendingOrder_throwsIllegalState() {
        order.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(order.getId()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("CONFIRMED");
    }

    @Test
    void cancelOrder_notFound_throwsOrderNotFoundException() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.cancelOrder(id))
            .isInstanceOf(OrderNotFoundException.class);
    }

    // Helper to set private fields via reflection
    private static void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
