package com.codebugs.orderservice.event;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderCreatedEvent(
    UUID orderId,
    String customerId,
    String productId,
    Integer quantity,
    BigDecimal amount
) {}
