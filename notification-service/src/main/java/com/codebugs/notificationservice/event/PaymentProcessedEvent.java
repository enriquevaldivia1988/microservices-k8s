package com.codebugs.notificationservice.event;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentProcessedEvent(
    UUID paymentId,
    UUID orderId,
    String customerId,
    String status,
    BigDecimal amount,
    String failureReason
) {}
