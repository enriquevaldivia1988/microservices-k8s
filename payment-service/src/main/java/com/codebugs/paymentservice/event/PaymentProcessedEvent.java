package com.codebugs.paymentservice.event;

import com.codebugs.paymentservice.model.PaymentStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record PaymentProcessedEvent(
    UUID paymentId,
    UUID orderId,
    String customerId,
    PaymentStatus status,
    BigDecimal amount,
    String failureReason
) {}
