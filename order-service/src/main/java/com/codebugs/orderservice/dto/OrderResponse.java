package com.codebugs.orderservice.dto;

import com.codebugs.orderservice.model.Order;
import com.codebugs.orderservice.model.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderResponse(
    UUID id,
    String customerId,
    String productId,
    Integer quantity,
    BigDecimal amount,
    OrderStatus status,
    Instant createdAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
            order.getId(),
            order.getCustomerId(),
            order.getProductId(),
            order.getQuantity(),
            order.getAmount(),
            order.getStatus(),
            order.getCreatedAt()
        );
    }
}
