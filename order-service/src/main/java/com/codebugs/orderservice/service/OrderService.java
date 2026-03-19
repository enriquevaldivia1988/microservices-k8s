package com.codebugs.orderservice.service;

import com.codebugs.orderservice.dto.CreateOrderRequest;
import com.codebugs.orderservice.dto.OrderResponse;
import com.codebugs.orderservice.event.OrderCreatedEvent;
import com.codebugs.orderservice.exception.OrderNotFoundException;
import com.codebugs.orderservice.model.Order;
import com.codebugs.orderservice.model.OrderStatus;
import com.codebugs.orderservice.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final String TOPIC = "order.created";

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public OrderService(OrderRepository orderRepository,
                        KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = new Order();
        order.setCustomerId(request.customerId());
        order.setProductId(request.productId());
        order.setQuantity(request.quantity());
        order.setAmount(request.amount());

        Order saved = orderRepository.save(order);
        log.info("Order created: {}", saved.getId());

        OrderCreatedEvent event = new OrderCreatedEvent(
            saved.getId(),
            saved.getCustomerId(),
            saved.getProductId(),
            saved.getQuantity(),
            saved.getAmount()
        );
        kafkaTemplate.send(TOPIC, saved.getId().toString(), event);
        log.info("Published order.created event for order: {}", saved.getId());

        return OrderResponse.from(saved);
    }

    public OrderResponse getOrder(UUID id) {
        return orderRepository.findById(id)
            .map(OrderResponse::from)
            .orElseThrow(() -> new OrderNotFoundException(id));
    }

    public List<OrderResponse> getOrdersByCustomer(String customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
            .map(OrderResponse::from)
            .toList();
    }

    @Transactional
    public OrderResponse cancelOrder(UUID id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(id));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException(
                "Cannot cancel order in status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        log.info("Order cancelled: {}", saved.getId());
        return OrderResponse.from(saved);
    }
}
